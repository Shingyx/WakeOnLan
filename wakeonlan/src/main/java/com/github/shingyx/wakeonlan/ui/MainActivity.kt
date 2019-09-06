package com.github.shingyx.wakeonlan.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.github.shingyx.wakeonlan.R
import com.github.shingyx.wakeonlan.data.Host
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val scope = MainScope()

    private lateinit var model: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = MainViewModel(application)

        model.host.observe(this, Observer { host ->
            hostname.text = host?.hostname ?: "-"
            ip_address.text = host?.ipAddress ?: "-"
            mac_address.text = host?.macAddress ?: "-"
        })

        model.hostScanResult.observe(this, Observer { result ->
            result.onSuccess { hosts ->
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(R.string.select_pc)
                    .setAdapter(HostListAdapter(hosts)) { _, index ->
                        model.selectHost(hosts[index])
                    }
                    .show()
            }
            result.onFailure { exception ->
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(R.string.error)
                    .setMessage(exception.message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
            setUiEnabled(true)
        })

        model.turnOnResult.observe(this, Observer { result ->
            val error = result.exceptionOrNull()?.message
            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle(if (error == null) R.string.pc_turned_on else R.string.error)
                .setMessage(error)
                .setPositiveButton(android.R.string.ok, null)
                .show()

            setUiEnabled(true)
        })

        select_pc.setOnClickListener {
            setUiEnabled(false)
            scope.launch { model.scanForHosts() }
        }

        turn_on.setOnClickListener {
            setUiEnabled(false)
            scope.launch { model.turnOn() }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun setUiEnabled(enable: Boolean) {
        select_pc.isEnabled = enable
        turn_on.isEnabled = enable
        progress.visibility = if (enable) View.INVISIBLE else View.VISIBLE
    }

    private inner class HostListAdapter(
        private val hosts: List<Host>
    ) : BaseAdapter() {
        override fun getItem(position: Int): Host {
            return hosts[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return hosts.size
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val host = hosts[position]
            val view = convertView
                ?: layoutInflater.inflate(R.layout.list_item_host, parent, false)
            return view.apply {
                findViewById<TextView>(R.id.hostname).text = host.hostname
                findViewById<TextView>(R.id.ip_address).text = host.ipAddress
                findViewById<TextView>(R.id.mac_address).text = host.macAddress
            }
        }
    }
}
