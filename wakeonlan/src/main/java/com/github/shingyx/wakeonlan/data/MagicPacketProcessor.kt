package com.github.shingyx.wakeonlan.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.FileReader
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue

private const val MAGIC_PACKET_LENGTH = 102
private const val SYNC_STREAM_LENGTH = 6
private const val MAC_ADDRESS_BYTE_LENGTH = 6
private const val WOL_PORT = 9
private const val PING_TIMEOUT_MS = 200
private const val SHARED_PREFERENCES_NAME = "WakeOnLanData"
private const val SAVED_MAC_ADDRESS = "SavedMacAddress"

private val ip4Regex = Regex("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}") // Note: very loose match
private val macRegex = Regex("(?:[0-9a-f]{2}:){5}[0-9a-f]{2}", RegexOption.IGNORE_CASE)
private val arpTableIpMacRegex = Regex(
    "^($ip4Regex).+($macRegex).*$",
    setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
)

class MagicPacketProcessor(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    var savedMacAddress: String
        get() = sharedPreferences.getString(SAVED_MAC_ADDRESS, "")!!
        set(value) = sharedPreferences.edit()
            .putString(SAVED_MAC_ADDRESS, value)
            .apply()

    suspend fun send(macAddress: String = savedMacAddress) {
        withContext(Dispatchers.IO) {
            val macAddressBytes = convertMacAddressString(macAddress)
            val packetBytes = getMagicPacketBytes(macAddressBytes)
            val broadcastAddress = WifiAddresses(context).getBroadcastAddress()
            val packet = DatagramPacket(packetBytes, packetBytes.size, broadcastAddress, WOL_PORT)
            DatagramSocket().use { it.send(packet) }
        }
    }

    suspend fun scanForHosts(): List<Host> {
        val inetAddressMap = scanForReachableAddresses().associateBy { it.hostAddress }

        val arpTable = try {
            FileReader("/proc/net/arp").use { it.readText() }
        } catch (e: IOException) {
            throw IOException("Failed to read arp table", e)
        }

        val hosts = ArrayList<Host>()

        for (match in arpTableIpMacRegex.findAll(arpTable)) {
            val (_, ipAddress, macAddress) = match.groupValues
            val inetAddress = inetAddressMap[ipAddress]
            if (inetAddress != null) {
                hosts.add(Host(inetAddress.hostName, inetAddress.hostAddress, macAddress))
            }
        }

        return hosts
    }

    private suspend fun scanForReachableAddresses(): List<InetAddress> {
        val reachableAddresses = ConcurrentLinkedQueue<InetAddress>()
        val allPotentialHosts = WifiAddresses(context).getAllPotentialHosts()

        withContext(Dispatchers.IO) {
            allPotentialHosts.map {
                async {
                    if (it.isReachable(PING_TIMEOUT_MS) && it.hostName != null) {
                        reachableAddresses.add(it)
                    }
                }
            }.awaitAll()
        }

        return reachableAddresses.toList()
    }
}

fun convertMacAddressString(macAddress: String): ByteArray {
    require(macRegex.matches(macAddress)) { "Invalid MAC address" }

    val parts = macAddress.split(Regex("[:-]"))
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
