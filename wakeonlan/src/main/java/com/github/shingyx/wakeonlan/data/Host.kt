package com.github.shingyx.wakeonlan.data

data class Host(
    val hostname: String,
    val ipAddress: String,
    val macAddress: String,
    val ssid: String
)
