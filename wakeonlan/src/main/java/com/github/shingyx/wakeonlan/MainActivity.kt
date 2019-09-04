package com.github.shingyx.wakeonlan

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val scope = MainScope()

    private lateinit var magicPacketProcessor: MagicPacketProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        magicPacketProcessor = MagicPacketProcessor(this)

        macAddressField.setText(magicPacketProcessor.getSavedMacAddress())

        sendButton.setOnClickListener {
            scope.launch {
                progressBar.visibility = View.VISIBLE

                val macAddress = macAddressField.text.toString().trim()
                val result = Result.runCatching {
                    magicPacketProcessor.send(macAddress)
                    magicPacketProcessor.saveMacAddress(macAddress)
                }
                val error = result.exceptionOrNull()?.message

                progressBar.visibility = View.INVISIBLE

                AlertDialog.Builder(this@MainActivity)
                    .setTitle(if (error == null) R.string.packet_sent else R.string.error)
                    .setMessage(error)
                    .setPositiveButton(R.string.ok, null)
                    .create()
                    .show()
            }
        }
    }
}
