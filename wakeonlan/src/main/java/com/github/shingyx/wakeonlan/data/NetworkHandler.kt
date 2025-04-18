package com.github.shingyx.wakeonlan.data

import android.content.Context
import com.github.shingyx.wakeonlan.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket

private const val MAGIC_PACKET_LENGTH = 102
private const val SYNC_STREAM_LENGTH = 6
private const val MAC_ADDRESS_BYTE_LENGTH = 6
private const val WOL_PORT = 9

private val macRegex = Regex("(?:[0-9a-f]{2}:){5}[0-9a-f]{2}", RegexOption.IGNORE_CASE)

class NetworkHandler(private val context: Context) {
    suspend fun sendMagicPacket(host: Host) {
        try {
            doSendMagicPacket(host)
        } catch (e: IOException) {
            // retry
            delay(500)
            doSendMagicPacket(host)
        }
    }

    private suspend fun doSendMagicPacket(host: Host) {
        withContext(Dispatchers.IO) {
            val macAddressBytes = convertMacAddressString(host.macAddress)
            val packetBytes = getMagicPacketBytes(macAddressBytes)
            val wifiAddresses = WifiAddresses(context)
            if (wifiAddresses.ssid != host.ssid) {
                val errorMessageId = if (wifiAddresses.ssid == "<unknown ssid>") {
                    R.string.error_missing_location_permission
                } else {
                    R.string.error_incorrect_ssid
                }
                throw IOException(context.getString(errorMessageId))
            }
            val broadcastAddress = wifiAddresses.getBroadcastAddress()
            val packet = DatagramPacket(packetBytes, packetBytes.size, broadcastAddress, WOL_PORT)
            DatagramSocket().use { it.send(packet) }
        }
    }
}

fun isMacAddressValid(macAddress: String): Boolean {
    return macRegex.matches(macAddress)
}

fun convertMacAddressString(macAddress: String): ByteArray {
    require(isMacAddressValid(macAddress)) { "Invalid MAC address" }
    val parts = macAddress.split(":")
    return ByteArray(MAC_ADDRESS_BYTE_LENGTH) { parts[it].toInt(16).toByte() }
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
