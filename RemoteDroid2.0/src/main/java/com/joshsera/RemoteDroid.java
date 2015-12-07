package com.joshsera;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class RemoteDroid extends AppCompatActivity {
    private static final String TAG = "RemoteDroid";
    private static final String SAVED_HOSTS_FILE = "saved_hosts";

    private static final int EDIT_HOST_REQUEST = 0;

    ArrayList<Host> hosts = null;
    private ArrayAdapter<Host> hostAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

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

        // set some listeners
        Button but = (Button) this.findViewById(R.id.btnConnect);
        final EditText tbIp = (EditText) findViewById(R.id.etIp);
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RemoteDroid.this, EditHostActivity.class);
                startActivityForResult(i, EDIT_HOST_REQUEST);
            }
        });

        final ListView hostListView = (ListView) findViewById(R.id.lvHosts);
        hostListView.setAdapter(hostAdapter);
        hostListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new ConnectAsync().execute(hostAdapter.getItem(position).getAddress());
            }
        });
        final String[] longClickOptions = {"Delete", "Edit"};
        hostListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog dialog = new AlertDialog.Builder(RemoteDroid.this).setItems(longClickOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Host h = hostAdapter.getItem(position);
                        switch (which) {
                            case 0:
                                hostAdapter.remove(h);
                                break;
                            case 1:
                                Intent i = new Intent(RemoteDroid.this, EditHostActivity.class);
                                i.putExtra(EditHostActivity.HOST, h);
                                startActivityForResult(i, EDIT_HOST_REQUEST);
                                break;
                        }
                    }
                }).create();
                dialog.show();
                return true;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_HOST_REQUEST) {
            if (resultCode == RESULT_OK) {
                Host host = (Host) data.getSerializableExtra(EditHostActivity.HOST);
                hostAdapter.remove(host);
                Host editedHost = (Host) data.getSerializableExtra(EditHostActivity.EDITED_HOST);
                hostAdapter.add(editedHost);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
