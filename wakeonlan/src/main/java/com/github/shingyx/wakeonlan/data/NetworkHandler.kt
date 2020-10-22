package com.github.shingyx.wakeonlan.data

import android.content.Context
import com.github.shingyx.wakeonlan.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.coroutineContext

private const val MAGIC_PACKET_LENGTH = 102
private const val SYNC_STREAM_LENGTH = 6
private const val MAC_ADDRESS_BYTE_LENGTH = 6
private const val WOL_PORT = 9
private const val PING_TIMEOUT_MS = 200

private val macRegex = Regex("(?:[0-9a-f]{2}:){5}[0-9a-f]{2}", RegexOption.IGNORE_CASE)

class NetworkHandler(private val context: Context) {
    suspend fun sendMagicPacket(host: Host) {
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

    suspend fun scanForHosts(): List<Host> {
        val wifiAddresses = WifiAddresses(context)
        val ssid = wifiAddresses.ssid
        val inetAddressMap = scanForReachableAddresses(wifiAddresses).associateBy { it.hostAddress }

        if (!coroutineContext.isActive) {
            return emptyList()
        }

        val ipProcess = withContext(Dispatchers.IO) {
            Runtime.getRuntime().exec("ip neigh").also { it.waitFor() }
        }
        if (ipProcess.exitValue() != 0) {
            throw IOException(context.getString(R.string.error_cannot_read_arp_entries))
        }

        fun tryParseHostFromLine(line: String): Host? {
            val values = line.split(Regex("\\s+"))
            if (values.size >= 5) {
                val (ipAddress, _, _, _, macAddress) = values
                val inetAddress = inetAddressMap[ipAddress]
                    ?: return null

                val state = values.last()
                if (state != "INCOMPLETE" && state != "FAILED") {
                    return Host(inetAddress.hostName, inetAddress.hostAddress, macAddress, ssid)
                }
            }
            return null
        }

        val hosts = ArrayList<Host>()
        ipProcess.inputStream.bufferedReader().forEachLine {
            tryParseHostFromLine(it)?.let(hosts::add)
        }
        return hosts
    }

    private suspend fun scanForReachableAddresses(wifiAddresses: WifiAddresses): List<InetAddress> {
        val reachableAddresses = ConcurrentLinkedQueue<InetAddress>()
        val allPotentialHosts = wifiAddresses.getAllPotentialHosts()

        coroutineScope {
            allPotentialHosts.map {
                async {
                    withContext(Dispatchers.IO) {
                        if (isActive && it.isReachable(PING_TIMEOUT_MS) && it.hostName != null) {
                            reachableAddresses.add(it)
                        }
                    }
                }
            }.awaitAll()
        }

        return reachableAddresses.toList()
    }
}

fun convertMacAddressString(macAddress: String): ByteArray {
    require(macRegex.matches(macAddress)) { "Invalid MAC address" }

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
