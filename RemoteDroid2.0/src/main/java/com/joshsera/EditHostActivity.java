package com.joshsera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class EditHostActivity extends AppCompatActivity {
    public static final String HOST = "com.remotedroid.HOST";
    public static final String EDITED_HOST = "com.remotedroid.EDITED_HOST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_host);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);

        final EditText
                hostname = (EditText) findViewById(R.id.hostname),
                address = (EditText) findViewById(R.id.address);

        final Intent intent = getIntent();
        final Host host = (Host) intent.getSerializableExtra(HOST);
        if (host == null) {
            toolbar.setTitle("New Host");
        } else {
            toolbar.setTitle("Edit Host");
            if (host.getHostname() != null) {
                hostname.setText(host.getHostname());
            }
            if (host.getAddress() != null) {
                address.setText(host.getAddress().getHostAddress());
            }
        }

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Host h = new Host(hostname.getText().toString(), InetAddress.getByName(address.getText().toString()));
                    intent.putExtra(EDITED_HOST, h);
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (UnknownHostException e) {
                    Toast.makeText(EditHostActivity.this, "Error: Unknown Host", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
