package com.github.shingyx.wakeonlan

import android.content.Context
import android.content.SharedPreferences
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.UnknownHostException

private const val MAGIC_PACKET_LENGTH = 102
private const val SYNC_STREAM_LENGTH = 6
private const val MAC_ADDRESS_BYTE_LENGTH = 6
private const val WOL_PORT = 9
private const val SHARED_PREFERENCES_NAME = "WakeOnLanData"
private const val SAVED_MAC_ADDRESS = "SavedMacAddress"

class MagicPacketProcessor(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getSavedMacAddress(): String {
        return sharedPreferences.getString(SAVED_MAC_ADDRESS, "")!!
    }

    fun saveMacAddress(macAddress: String) {
        val editor = sharedPreferences.edit()
        editor.putString(SAVED_MAC_ADDRESS, macAddress)
        editor.apply()
    }

    fun send(macAddress: String) {
        val macAddressBytes = convertMacAddressString(macAddress)
        val packetBytes = getMagicPacketBytes(macAddressBytes)
        val ip = getBroadcastAddress()
        val packet = DatagramPacket(packetBytes, packetBytes.size, ip, WOL_PORT)
        DatagramSocket().use { it.send(packet) }
    }

    private fun getBroadcastAddress(): InetAddress {
        val wifiManager = context.getSystemService(WifiManager::class.java)
        if (wifiManager != null) {
            val wifiInfo = wifiManager.connectionInfo
            val dhcpInfo = wifiManager.dhcpInfo

            if (dhcpInfo != null && wifiInfo?.supplicantState == SupplicantState.COMPLETED) {
                val broadcast = dhcpInfo.ipAddress and dhcpInfo.netmask or dhcpInfo.netmask.inv()
                val quads = ByteArray(4) { (broadcast shr it * 8).toByte() }
                return InetAddress.getByAddress(quads)
            }
        }
        throw UnknownHostException("Unable to retrieve broadcast address")
    }
}

fun convertMacAddressString(macAddress: String): ByteArray {
    require(isValidMacAddress(macAddress)) { "Invalid MAC address" }

    val parts = macAddress.split("[:-]".toRegex())
    return ByteArray(MAC_ADDRESS_BYTE_LENGTH) { parts[it].toInt(16).toByte() }
}

private fun isValidMacAddress(macAddress: String): Boolean {
    val regex = Regex("^([0-9a-f]{2}[:-]){5}[0-9a-f]{2}$", RegexOption.IGNORE_CASE)
    return regex.matches(macAddress)
}

private fun getMagicPacketBytes(macAddressBytes: ByteArray): ByteArray {
    val packet = ByteArray(MAGIC_PACKET_LENGTH)

    // Synchronization Stream
    for (i in 0 until SYNC_STREAM_LENGTH) {
        packet[i] = 0xff.toByte()
    }

    // Target MAC copied 16 times
    for (i in SYNC_STREAM_LENGTH until MAGIC_PACKET_LENGTH step MAC_ADDRESS_BYTE_LENGTH) {
        System.arraycopy(macAddressBytes, 0, packet, i, MAC_ADDRESS_BYTE_LENGTH)
    }

    return packet
}
