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

public class NewHostActivity extends AppCompatActivity {
    public static final String NEW_HOST = "com.remotedroid.NEW_HOST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_host);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);
        toolbar.setTitle("New Host");

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String hostname = ((EditText) findViewById(R.id.hostname)).getText().toString();
                    InetAddress address = InetAddress.getByName(((EditText) findViewById(R.id.address)).getText().toString());

                    Host host = new Host(hostname, address);
                    Intent i = new Intent();
                    i.putExtra(NEW_HOST, host);
                    setResult(RESULT_OK, i);
                    finish();
                } catch (UnknownHostException e) {
                    Toast.makeText(NewHostActivity.this, "Error: Unknown Host", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
