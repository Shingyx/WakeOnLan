package com.github.shingyx.wakeonlan.data

import android.content.Context
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.github.shingyx.wakeonlan.R
import java.net.InetAddress
import java.net.InterfaceAddress
import java.net.NetworkInterface
import java.net.UnknownHostException

class WifiAddresses(context: Context) {
    val ssid: String
    private val deviceIpAddress: Int
    private val interfaceAddress: InterfaceAddress

    init {
        val wifiInfo = getWifiInfo(context)

        var ssid = wifiInfo.ssid
        if (ssid.startsWith('"') && ssid.endsWith('"')) {
            ssid = ssid.substring(1, ssid.length - 1)
        }
        this.ssid = ssid

        deviceIpAddress = wifiInfo.ipAddress
        val deviceInetAddress = inetAddressFromInt(deviceIpAddress)
        interfaceAddress = NetworkInterface.getByInetAddress(deviceInetAddress)
            ?.interfaceAddresses
            ?.find { it.address == deviceInetAddress }
            ?: throw UnknownHostException(context.getString(R.string.error_cannot_read_wifi_info))
    }

    fun getAllPotentialHosts(): List<InetAddress> {
        val networkPrefixLength = interfaceAddress.networkPrefixLength.toInt()
        val subnetMask = (1 shl networkPrefixLength) - 1
        val networkPrefix = deviceIpAddress and subnetMask

        val suffixLength = 32 - networkPrefixLength
        val hostCount = (1 shl suffixLength) - 1 // Minus 1 to exclude broadcast address

        val results = ArrayList<InetAddress>(hostCount)
        for (i in 0 until hostCount) {
            val ipAddress = networkPrefix or (i shl networkPrefixLength)
            if (ipAddress != deviceIpAddress) {
                results.add(inetAddressFromInt(ipAddress))
            }
        }
        return results
    }

    fun getBroadcastAddress(): InetAddress {
        return interfaceAddress.broadcast
    }
}

private fun getWifiInfo(context: Context): WifiInfo {
    val wifiManager = context.getSystemService(WifiManager::class.java)
    if (wifiManager != null) {
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo?.supplicantState == SupplicantState.COMPLETED) {
            return wifiInfo
        }
    }
    throw UnknownHostException(context.getString(R.string.error_cannot_read_wifi_info))
}

private fun inetAddressFromInt(address: Int): InetAddress {
    val quads = ByteArray(4) {
        (address shr (it * 8)).toByte()
    }
    return InetAddress.getByAddress(quads)
}
