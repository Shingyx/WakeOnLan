package com.github.shingyx.wakeonlan.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        model.host.observe(this, Observer(this::populateHostUi))
        model.hostScanResult.observe(this, Observer(this::handleHostScanResults))
        model.turnOnPcResult.observe(this, Observer(this::handleTurnOnResult))

        select_pc.setOnClickListener {
            if (checkPermissions()) {
                setUiEnabled(false)
                scope.launch { model.scanForHosts() }
            }
        }

        turn_on.setOnClickListener {
            if (checkPermissions()) {
                setUiEnabled(false)
                scope.launch { model.turnOnPc() }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun populateHostUi(host: Host?) {
        hostname.text = host?.hostname ?: "-"
        ip_address.text = host?.ipAddress ?: "-"
        mac_address.text = host?.macAddress ?: "-"
        ssid.text = host?.ssid ?: "-"
    }

    private fun checkPermissions(): Boolean {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val checkResult = ContextCompat.checkSelfPermission(this, permission)
        if (checkResult == PackageManager.PERMISSION_GRANTED) {
            return true
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.location_permission_title)
                .setMessage(R.string.location_permission_rationale)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
                }
                .setCancelable(false)
                .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
        }
        return false
    }

    private fun handleHostScanResults(result: Result<List<Host>>) {
        result.onSuccess { hosts ->
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_pc)
                .setAdapter(HostListAdapter(this, hosts)) { _, index ->
                    model.selectHost(hosts[index])
                }
                .show()
        }
        result.onFailure { exception ->
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.error)
                .setMessage(exception.message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }

        setUiEnabled(true)
    }

    private fun handleTurnOnResult(result: Result<Unit>) {
        val error = result.exceptionOrNull()?.message
        MaterialAlertDialogBuilder(this)
            .setTitle(if (error == null) R.string.pc_turned_on else R.string.error)
            .setMessage(error)
            .setPositiveButton(android.R.string.ok, null)
            .show()

        setUiEnabled(true)
    }

    private fun setUiEnabled(enable: Boolean) {
        select_pc.isEnabled = enable
        turn_on.isEnabled = enable
        progress.visibility = if (enable) View.INVISIBLE else View.VISIBLE
    }
}

private class HostListAdapter(
    private val activity: Activity,
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
            ?: activity.layoutInflater.inflate(R.layout.list_item_host, parent, false)
        return view.apply {
            findViewById<TextView>(R.id.hostname).text = host.hostname
            findViewById<TextView>(R.id.ip_address).text = host.ipAddress
            findViewById<TextView>(R.id.mac_address).text = host.macAddress
        }
    }
}
