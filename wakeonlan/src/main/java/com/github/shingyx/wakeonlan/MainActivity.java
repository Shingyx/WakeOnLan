package com.github.shingyx.wakeonlan;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText macAddress = findViewById(R.id.macAddress);
        Button send = findViewById(R.id.send);
        ProgressBar progress = findViewById(R.id.progress);

        // TODO replace with previously used, save to shared preferences
        macAddress.setText("00:00:00:00:00:00");

        send.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);

            new SendPacketTask(this, macAddress.getText().toString(), (String error) -> {
                progress.setVisibility(View.INVISIBLE);

                new AlertDialog.Builder(this)
                        .setTitle(error == null ? R.string.packet_sent : R.string.error)
                        .setMessage(error)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show();
            }).execute();
        });
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
            } catch (IOException e) {
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
