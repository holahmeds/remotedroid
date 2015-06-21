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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/*
 * TODO: DNS lookup
 */

public class RemoteDroid extends Activity {
    private static final String TAG = "RemoteDroid";
    private static final String SAVED_HOSTS_FILE = "saved_hosts";

    private EditText tbIp;
    private ListView lvHosts;

    private DiscoverThread discover;
    private ArrayList<InetAddress> hostlist;

    private ArrayList<SavedHost> savedHosts;

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
                    new ConnectAsync().execute(InetAddress.getByName(tbIp.getText().toString()));
                } catch (UnknownHostException e) {
                    Toast.makeText(RemoteDroid.this, getText(R.string.toast_invalidIP), Toast.LENGTH_LONG).show();
                }
            }
        });

        // discover some servers
        this.hostlist = new ArrayList<InetAddress>();
        lvHosts = (ListView) findViewById(R.id.lvHosts);
        lvHosts.setAdapter(
                new ArrayAdapter<InetAddress>(this, R.layout.savedhost, R.id.hostEntry, hostlist));
        lvHosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                new ConnectAsync().execute(hostlist.get(position));
            }
        });

        // Read saved hosts
        try {
            ObjectInputStream ois = new ObjectInputStream(openFileInput(SAVED_HOSTS_FILE));
            savedHosts = (ArrayList<SavedHost>) ois.readObject();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "No saved hosts file found. Will create one when exiting");
            savedHosts = new ArrayList<SavedHost>();
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error reading saved hosts.", e);
        }
    }

    /**
     * App starts displaying things
     */
    public void onResume() {
        super.onResume();
        this.discover = new DiscoverThread(new DiscoverThread.DiscoverListener() {
            @Override
            public void onAddressReceived(InetAddress address) {
                if (!hostlist.contains(address)) {
                    hostlist.add(address);
                    Log.d(TAG, "Got host back, " + address);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ArrayAdapter) lvHosts.getAdapter()).notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        this.discover.start();
    }


    /**
     * App goes into background
     */
    public void onPause() {
        super.onPause();
        this.discover.closeSocket();
    }

    @Override
    protected void onDestroy() {
        // Write saved hosts
        try {
            File f = new File(getFilesDir(), SAVED_HOSTS_FILE);
            if (!f.exists()) f.createNewFile();

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(savedHosts);
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

        @Override
        protected Boolean doInBackground(InetAddress... params) {
            addr = params[0];

            try {
                if (addr.isReachable(500)) {
                    savedHosts.add(new SavedHost(addr.getHostName(), addr));
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
                Intent i = new Intent(RemoteDroid.this, PadActivity.class);
                i.putExtra(PadActivity.CONNECT_IP, addr);
                RemoteDroid.this.startActivity(i);
            } else {
                Toast.makeText(getParent(), "Unable to reach host", Toast.LENGTH_LONG).show();
            }
        }
    }

}
