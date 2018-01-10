package com.github.shingyx.wakeonlan;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private static String SAVED_MAC_ADDRESS = "SavedMacAddress";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("WakeOnLanData", MODE_PRIVATE);

        EditText macAddressField = findViewById(R.id.macAddress);
        Button sendButton = findViewById(R.id.send);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        macAddressField.setText(getSavedMacAddress());

        sendButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);

            String macAddress = macAddressField.getText().toString();
            new SendPacketTask(this, macAddress, (String error) -> {
                progressBar.setVisibility(View.INVISIBLE);

                if (error == null) {
                    setSavedMacAddress(macAddress);
                }

                new AlertDialog.Builder(this)
                        .setTitle(error == null ? R.string.packet_sent : R.string.error)
                        .setMessage(error)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show();
            }).execute();
        });
    }

    private String getSavedMacAddress() {
        return sharedPreferences.getString(SAVED_MAC_ADDRESS, null);
    }

    private void setSavedMacAddress(String macAddress) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SAVED_MAC_ADDRESS, macAddress);
        editor.apply();
    }

    private static class SendPacketTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<Activity> activityWeakReference;
        private String macAddress;
        private Callback callback;

        public SendPacketTask(Activity activity, String macAddress, Callback callback) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.macAddress = macAddress;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Activity activity = this.activityWeakReference.get();
            if (activity == null) {
                return null;
            }
            String error = null;
            try {
                new MagicPacketSender().send(activity, macAddress);
            } catch (Exception e) {
                error = e.getMessage();
            }
            String finalError = error;
            activity.runOnUiThread(() -> callback.onComplete(finalError));
            return null;
        }
    }

    private interface Callback {
        void onComplete(String error);
    }
}
