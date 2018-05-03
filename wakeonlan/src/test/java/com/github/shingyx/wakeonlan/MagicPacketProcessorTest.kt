package com.github.shingyx.wakeonlan

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.fail
import org.junit.Test

class MagicPacketProcessorTest {

    @Test
    fun convertMacAddressString_allZero() {
        runHappyTest(intArrayOf(0, 0, 0, 0, 0, 0), "00:00:00:00:00:00")
    }

    @Test
    fun convertMacAddressString_allF() {
        runHappyTest(intArrayOf(0xff, 0xff, 0xff, 0xff, 0xff, 0xff), "ff:ff:ff:ff:ff:ff")
    }

    @Test
    fun convertMacAddressString_aToF() {
        runHappyTest(intArrayOf(0xab, 0xcd, 0xef, 0xfe, 0xdc, 0xba), "ab:cd:ef:fe:dc:ba")
    }

    @Test
    fun convertMacAddressString_upperCase() {
        runHappyTest(intArrayOf(0xff, 0xff, 0xff, 0xff, 0xff, 0xff), "FF:FF:FF:FF:FF:FF")
    }

    @Test
    fun convertMacAddressString_hyphenSeparator() {
        runHappyTest(intArrayOf(0, 0, 0, 0, 0, 0), "00-00-00-00-00-00")
    }

    @Test
    fun convertMacAddressString_containsInvalidHex() {
        runSadTest("00:00:00:00:00:0g")
    }

    @Test
    fun convertMacAddressString_tooShort() {
        runSadTest("ff:ff:ff:ff:ff:f")
    }

    @Test
    fun convertMacAddressString_tooLong() {
        runSadTest("ff:ff:ff:ff:ff:fff")
    }

    @Test
    fun convertMacAddressString_tooLong2() {
        runSadTest("ff:ff:ff:ff:ff:ff-")
    }

    @Test
    fun convertMacAddressString_doubleSeparator() {
        runSadTest("ff:ff:ff:ff:ff::f")
    }

    @Test
    fun convertMacAddressString_noSeparators() {
        runSadTest("ffffffffffff")
    }

    @Test
    fun convertMacAddressString_emptyString() {
        runSadTest("")
    }

    private fun runHappyTest(expected: IntArray, input: String) {
        val expectedBytes = expected.map { it.toByte() }.toByteArray()
        assertArrayEquals(expectedBytes, convertMacAddressString(input))
    }

    private fun runSadTest(input: String) {
        try {
            convertMacAddressString(input)
            fail("Expected an exception")
        } catch (e: IllegalArgumentException) {
            // Caught an IllegalArgumentException
        }
    }
}
