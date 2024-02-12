package com.github.shingyx.wakeonlan.ui

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.github.shingyx.wakeonlan.R
import com.github.shingyx.wakeonlan.data.Host
import com.github.shingyx.wakeonlan.data.WifiAddresses
import com.github.shingyx.wakeonlan.data.isMacAddressValid
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private val scope = MainScope()

    private lateinit var model: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = MainViewModel(application)

        model.host.observe(this, Observer(this::populateHostUi))
        model.turnOnPcResult.observe(this, Observer(this::handleTurnOnResult))

        configure.setOnClickListener {
            if (checkPermissions()) {
                configureHost()
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
        name.text = host?.name ?: "-"
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

    private fun configureHost() {
        val ssid = try {
            WifiAddresses(this).ssid
        } catch (e: Exception) {
            return showError(e.message)
        }

        val view = layoutInflater.inflate(R.layout.dialog_host, null)
        val nameField = view.findViewById<EditText>(R.id.edit_name)
        val macAddressField = view.findViewById<EditText>(R.id.edit_mac_address)
        model.host.value?.let {
            nameField.setText(it.name)
            macAddressField.setText(it.macAddress)
        }
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.configure)
            .setView(view)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
            .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val name = nameField.text.toString()
            val macAddress = macAddressField.text.toString()
            if (!isMacAddressValid(macAddress)) {
                showError(getString(R.string.invalid_mac_address))
            } else {
                val host = Host(name, macAddress, ssid)
                model.selectHost(host)
                dialog.dismiss()
            }
        }
    }

    private fun showError(message: String?) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
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
        configure.isEnabled = enable
        turn_on.isEnabled = enable
        progress.visibility = if (enable) View.INVISIBLE else View.VISIBLE
    }
}
