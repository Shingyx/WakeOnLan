package com.github.shingyx.wakeonlan;

import android.os.AsyncTask;

public class SendPacketTask extends AsyncTask<Void, Void, Void> {
    private MagicPacketProcessor magicPacketProcessor;
    private String macAddress;
    private Callback callback;

    public SendPacketTask(MagicPacketProcessor magicPacketProcessor, String macAddress, Callback callback) {
        this.magicPacketProcessor = magicPacketProcessor;
        this.macAddress = macAddress;
        this.callback = callback;
    }

    public SendPacketTask(MagicPacketProcessor magicPacketProcessor, Callback callback) {
        this(magicPacketProcessor, magicPacketProcessor.getSavedMacAddress(), callback);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String error = null;
        try {
            magicPacketProcessor.send(macAddress);
        } catch (Exception e) {
            error = e.getMessage();
        }
        callback.function(error);
        return null;
    }
}
