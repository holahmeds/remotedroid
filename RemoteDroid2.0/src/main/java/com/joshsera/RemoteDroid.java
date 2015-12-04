package com.joshsera;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class RemoteDroid extends Activity {
    private static final String TAG = "RemoteDroid";
    private static final String SAVED_HOSTS_FILE = "saved_hosts";

    private EditText tbIp;

    ArrayList<Host> hosts = null;
    private ArrayAdapter<Host> hostAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tbIp = (EditText) findViewById(R.id.etIp);

        // set some listeners
        Button but = (Button) this.findViewById(R.id.btnConnect);
        but.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    ConnectAsync task = new ConnectAsync();
                    task.execute(InetAddress.getByName(tbIp.getText().toString()));
                } catch (UnknownHostException e) {
                    Toast.makeText(RemoteDroid.this, getText(R.string.toast_invalidIP), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Read saved hosts
        try {
            ObjectInputStream ois = new ObjectInputStream(openFileInput(SAVED_HOSTS_FILE));
            hosts = (ArrayList<Host>) ois.readObject();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "No saved hosts file found. Will create one when exiting");
        } catch (Exception e) {
            Log.e(TAG, "Error reading saved hosts.", e);
        } finally {
            if (hosts == null) {
                hosts = new ArrayList<>();
            }
        }

        this.hostAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hosts);

        ListView hostListView = (ListView) findViewById(R.id.lvHosts);
        hostListView.setAdapter(hostAdapter);
        hostListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new ConnectAsync().execute(hostAdapter.getItem(position).getAddress());
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Write saved hosts
        try {
            ObjectOutputStream oos = new ObjectOutputStream(openFileOutput(SAVED_HOSTS_FILE, MODE_PRIVATE));
            oos.writeObject(hosts);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    /**
     * Starts PadActivity with given InetAddress if it is reachable.
     * If more than one address is given, only the first is used and the rest are ignored.
     */
    private class ConnectAsync extends AsyncTask<InetAddress, Void, Boolean> {
        private InetAddress addr;
        private String hostName;

        @Override
        protected Boolean doInBackground(InetAddress... params) {
            addr = params[0];

            try {
                if (addr.isReachable(500)) {
                    hostName = addr.getHostName();
                    return true;
                }
            } catch (IOException e) {
                Log.d(TAG, "IOException when connecting to host", e);
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean reachable) {
            if (reachable) {
                Host h = new Host(hostName, addr);
                if (!hosts.contains(h)) {
                    hosts.add(h);
                    hostAdapter.add(h);
                }

                Intent i = new Intent(RemoteDroid.this, PadActivity.class);
                i.putExtra(PadActivity.CONNECT_IP, addr);
                RemoteDroid.this.startActivity(i);
            } else {
                Toast.makeText(getParent(), "Unable to reach host", Toast.LENGTH_LONG).show();
            }
        }
    }

}
