package com.github.shingyx.wakeonlan.data

import android.content.Context

private const val SHARED_PREFERENCES_NAME = "WakeOnLanData"
private const val SAVED_NAME = "SavedName"
private const val SAVED_MAC_ADDRESS = "SavedMacAddress"
private const val SAVED_SSID = "SavedSsid"

class Storage private constructor(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    var savedHost: Host?
        get() {
            val hostname = sharedPreferences.getString(SAVED_NAME, null)
            val macAddress = sharedPreferences.getString(SAVED_MAC_ADDRESS, null)
            val ssid = sharedPreferences.getString(SAVED_SSID, null)
            return if (hostname != null && macAddress != null && ssid != null) {
                Host(hostname, macAddress, ssid)
            } else {
                null
            }
        }
        set(value) {
            sharedPreferences.edit()
                .putString(SAVED_NAME, value?.name)
                .putString(SAVED_MAC_ADDRESS, value?.macAddress)
                .putString(SAVED_SSID, value?.ssid)
                .apply()
        }

    companion object {
        private lateinit var instance: Storage

        fun getInstance(context: Context): Storage {
            if (!this::instance.isInitialized) {
                instance = Storage(context)
            }
            return instance
        }
    }
}
