package com.github.shingyx.wakeonlan;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MagicPacketSender {

    private static final int MAGIC_PACKET_LENGTH = 102;
    private static final int SYNC_STREAM_LENGTH = 6;
    private static final int MAC_ADDRESS_BYTE_LENGTH = 6;
    private static final int WOL_PORT = 9;

    public void send(Context context, String macAddress) throws IOException {
        byte[] bytes = getMagicPacketBytes(macAddress);
        InetAddress ip = getBroadcastAddress(context);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, ip, WOL_PORT);
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(packet);
        }
    }

    private byte[] getMagicPacketBytes(String macAddress) {
        byte[] packet = new byte[MAGIC_PACKET_LENGTH];

        // Synchronization Stream
        for (int i = 0; i < SYNC_STREAM_LENGTH; i++) {
            packet[i] = (byte) 0xff;
        }

        // Target MAC copied 16 times
        byte[] macAddressBytes = convertMacAddressString(macAddress);
        for (int i = SYNC_STREAM_LENGTH; i < MAGIC_PACKET_LENGTH; i += MAC_ADDRESS_BYTE_LENGTH) {
            System.arraycopy(macAddressBytes, 0, packet, i, MAC_ADDRESS_BYTE_LENGTH);
        }

        return packet;
    }

    private byte[] convertMacAddressString(String macAddress) {
        String[] parts = macAddress.split(":");
        byte[] bytes = new byte[MAC_ADDRESS_BYTE_LENGTH];
        for (int i = 0; i < MAC_ADDRESS_BYTE_LENGTH; i++) {
            int hex = Integer.parseInt(parts[i], 16);
            bytes[i] = (byte) hex;
        }
        return bytes;
    }

    private InetAddress getBroadcastAddress(Context context) throws UnknownHostException {
        WifiManager wifiManager = context.getSystemService(WifiManager.class);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

            if (wifiInfo != null && dhcpInfo != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                int broadcast = (dhcpInfo.ipAddress & dhcpInfo.netmask) | ~dhcpInfo.netmask;
                byte[] quads = new byte[4];
                for (int i = 0; i < 4; i++) {
                    quads[i] = (byte) (broadcast >> (i * 8));
                }
                return InetAddress.getByAddress(quads);
            }
        }
        throw new UnknownHostException("Unable to retrieve broadcast address");
    }
}