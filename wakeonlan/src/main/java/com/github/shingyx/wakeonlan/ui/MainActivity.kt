package com.github.shingyx.wakeonlan.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.shingyx.wakeonlan.R
import com.github.shingyx.wakeonlan.data.MagicPacketProcessor
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

        macAddressField.setText(magicPacketProcessor.savedMacAddress)

        scanButton.setOnClickListener {
            scope.launch {
                val hosts = magicPacketProcessor.scanForHosts()
                // TODO do something
                println(hosts.toString())
            }
        }

        sendButton.setOnClickListener {
            scope.launch {
                progressBar.visibility = View.VISIBLE
                sendButton.isEnabled = false

                val macAddress = macAddressField.text.toString().trim()
                val result = Result.runCatching {
                    magicPacketProcessor.send(macAddress)
                    magicPacketProcessor.savedMacAddress = macAddress
                }
                val error = result.exceptionOrNull()?.message

                AlertDialog.Builder(this@MainActivity)
                    .setTitle(if (error == null) R.string.computer_turned_on else R.string.error)
                    .setMessage(error)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                    .show()

                progressBar.visibility = View.INVISIBLE
                sendButton.isEnabled = true
            }
        }
    }
}
