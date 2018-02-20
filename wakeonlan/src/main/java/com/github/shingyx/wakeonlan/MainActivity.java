package com.github.shingyx.wakeonlan;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    private MagicPacketProcessor magicPacketProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        magicPacketProcessor = new MagicPacketProcessor(this);

        EditText macAddressField = findViewById(R.id.macAddress);
        Button sendButton = findViewById(R.id.send);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        macAddressField.setText(magicPacketProcessor.getSavedMacAddress());

        sendButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);

            String macAddress = macAddressField.getText().toString().trim();

            new SendPacketTask(magicPacketProcessor, macAddress, (String error) -> {
                if (error == null) {
                    magicPacketProcessor.setSavedMacAddress(macAddress);
                }
                this.runOnUiThread(() -> {
                    progressBar.setVisibility(View.INVISIBLE);

                    new AlertDialog.Builder(this)
                            .setTitle(error == null ? R.string.packet_sent : R.string.error)
                            .setMessage(error)
                            .setPositiveButton(R.string.ok, null)
                            .create()
                            .show();
                });
            }).execute();
        });
    }
}
