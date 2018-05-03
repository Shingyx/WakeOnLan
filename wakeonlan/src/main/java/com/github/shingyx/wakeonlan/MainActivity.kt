package com.github.shingyx.wakeonlan

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var magicPacketProcessor: MagicPacketProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        magicPacketProcessor = MagicPacketProcessor(this)

        macAddressField.setText(magicPacketProcessor.getSavedMacAddress())

        sendButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val macAddress = macAddressField.text.toString().trim()

            SendPacketTask(magicPacketProcessor, macAddress, { error ->
                if (error == null) {
                    magicPacketProcessor.saveMacAddress(macAddress)
                }
                this.runOnUiThread {
                    progressBar.visibility = View.INVISIBLE

                    AlertDialog.Builder(this)
                            .setTitle(if (error == null) R.string.packet_sent else R.string.error)
                            .setMessage(error)
                            .setPositiveButton(R.string.ok, null)
                            .create()
                            .show()
                }
            }).execute()
        }
    }
}
