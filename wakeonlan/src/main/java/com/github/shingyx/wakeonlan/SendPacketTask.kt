package com.github.shingyx.wakeonlan

import android.os.AsyncTask

class SendPacketTask(
    private val magicPacketProcessor: MagicPacketProcessor,
    private val macAddress: String,
    private val callback: (error: String?) -> Unit
) : AsyncTask<Unit, Unit, String?>() {
    constructor(
        magicPacketProcessor: MagicPacketProcessor,
        callback: (error: String?) -> Unit
    ) : this(magicPacketProcessor, magicPacketProcessor.getSavedMacAddress(), callback)

    override fun doInBackground(vararg args: Unit): String? {
        return try {
            magicPacketProcessor.send(macAddress)
            null
        } catch (e: Exception) {
            e.message
        }
    }

    override fun onPostExecute(result: String?) {
        callback(result)
    }
}
