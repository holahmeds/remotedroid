package com.holahmeds.remotedroid.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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

    private ArrayList<Host> hosts;
    private ArrayAdapter<Host> hostAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        this.setActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        // Read saved hosts
        try (ObjectInputStream ois = new ObjectInputStream(this.openFileInput(RemoteDroid.SAVED_HOSTS_FILE))) {
            // Reading from a private file we created. We know it contains only an ArrayList<Host>
            @SuppressWarnings("unchecked")
            ArrayList<Host> h = (ArrayList<Host>) ois.readObject();

            this.hosts = h;
        } catch (FileNotFoundException e) {
            Log.d(RemoteDroid.TAG, "No saved hosts file found. Will create one when exiting", e);
        } catch (ClassNotFoundException | IOException e) {
            Log.e(RemoteDroid.TAG, "Error reading saved hosts.", e);
        } finally {
            if (this.hosts == null) {
                this.hosts = new ArrayList<>();
            }
        }

        this.hostAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, this.hosts);

        // set some listeners
        Button but = (Button) this.findViewById(R.id.btnConnect);
        final EditText tbIp = (EditText) this.findViewById(R.id.etIp);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String addr = tbIp.getText().toString();
                    Host h = new Host(addr, InetAddress.getByName(addr));

                    ConnectAsync task = new ConnectAsync();
                    task.execute(h);
                } catch (UnknownHostException e) {
                    Toast.makeText(RemoteDroid.this, R.string.toast_invalidIP, Toast.LENGTH_LONG).show();
                    Log.d(RemoteDroid.TAG, "Unknown host", e);
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) this.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RemoteDroid.this, EditHostActivity.class);
                RemoteDroid.this.startActivityForResult(intent, RemoteDroid.EDIT_HOST_REQUEST);
            }
        });

        final ListView hostListView = (ListView) this.findViewById(R.id.lvHosts);
        hostListView.setAdapter(this.hostAdapter);
        hostListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new ConnectAsync().execute(RemoteDroid.this.hostAdapter.getItem(position));
            }
        });
        final String[] longClickOptions = {"Delete", "Edit"};
        hostListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog dialog = new AlertDialog.Builder(RemoteDroid.this).setItems(longClickOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        Host h = RemoteDroid.this.hostAdapter.getItem(position);
                        switch (which) {
                            case 0:
                                RemoteDroid.this.hostAdapter.remove(h);
                                break;
                            case 1:
                                Intent intent = new Intent(RemoteDroid.this, EditHostActivity.class);
                                intent.putExtra(EditHostActivity.HOST, h);
                                RemoteDroid.this.startActivityForResult(intent, RemoteDroid.EDIT_HOST_REQUEST);
                                break;
                            default:
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
        try (ObjectOutputStream oos = new ObjectOutputStream(this.openFileOutput(RemoteDroid.SAVED_HOSTS_FILE, Context.MODE_PRIVATE))) {
            oos.writeObject(this.hosts);
        } catch (IOException e) {
            Log.d(RemoteDroid.TAG, "Unable to write to saved host file", e);
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RemoteDroid.EDIT_HOST_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Host host = (Host) data.getSerializableExtra(EditHostActivity.HOST);
                this.hostAdapter.remove(host);
                Host editedHost = (Host) data.getSerializableExtra(EditHostActivity.EDITED_HOST);
                this.hostAdapter.add(editedHost);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Starts PadActivity with given Host if it is reachable.
     * If more than one address is given, only the first is used and the rest are ignored.
     */
    private class ConnectAsync extends AsyncTask<Host, Void, Boolean> {
        private static final int DEFAULT_TIMEOUT = 500;
        private Host host;

        @Override
        protected Boolean doInBackground(Host... params) {
            this.host = params[0];

            try {
                return this.host.getAddress().isReachable(ConnectAsync.DEFAULT_TIMEOUT);
            } catch (IOException e) {
                Log.d(RemoteDroid.TAG, "IOException when connecting to host", e);
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean reachable) {
            if (reachable) {
                if (!RemoteDroid.this.hosts.contains(this.host)) {
                    RemoteDroid.this.hostAdapter.add(this.host);
                }

                Intent i = new Intent(RemoteDroid.this, PadActivity.class);
                i.putExtra(PadActivity.CONNECT_IP, this.host.getAddress());
                RemoteDroid.this.startActivity(i);
            } else {
                Toast.makeText(RemoteDroid.this.getParent(), R.string.txt_ip_error, Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(reachable);
        }
    }

}
