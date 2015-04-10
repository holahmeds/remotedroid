package com.joshsera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/*
 * To-do
 * 
 * DNS lookup
 * arrow keys, esc, win key
 */

public class RemoteDroid extends Activity {
    private static final String TAG = "RemoteDroid";

    //
    private EditText tbIp;
    private ListView lvHosts;

    private DiscoverThread discover;
    private ArrayList<InetAddress> hostlist;

    public RemoteDroid() {
        super();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tbIp = (EditText) findViewById(R.id.etIp);

        // set some listeners
        Button but = (Button) this.findViewById(R.id.btnConnect);
        but.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onConnectButton();
            }
        });

        // discover some servers
        this.hostlist = new ArrayList<InetAddress>();
        lvHosts = (ListView) findViewById(R.id.lvHosts);
        lvHosts.setAdapter(
                new ArrayAdapter<InetAddress>(this, R.layout.savedhost, R.id.hostEntry, hostlist));
        lvHosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                connect(hostlist.get(position));
            }
        });
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

    private void connect(InetAddress address) {
        Intent i = new Intent(this, PadActivity.class);
        i.putExtra(PadActivity.CONNECT_IP, address);
        this.startActivity(i);
    }

    private void onConnectButton() {
        String ip = this.tbIp.getText().toString();
        try {
            connect(InetAddress.getByName(ip));
        } catch (UnknownHostException e) {
            Log.d(TAG, "Tried but failed to connect to " + ip, e);
            Toast.makeText(this, this.getResources().getText(R.string.toast_invalidIP), Toast.LENGTH_LONG).show();
        }
    }

}
