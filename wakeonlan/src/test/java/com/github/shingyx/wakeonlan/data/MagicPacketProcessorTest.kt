package com.github.shingyx.wakeonlan.data

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MagicPacketProcessorTest {
    @Nested
    inner class ConvertMacAddressString {
        @Test
        fun allZero() {
            runHappyTest(intArrayOf(0, 0, 0, 0, 0, 0), "00:00:00:00:00:00")
        }

        @Test
        fun allF() {
            runHappyTest(intArrayOf(0xff, 0xff, 0xff, 0xff, 0xff, 0xff), "ff:ff:ff:ff:ff:ff")
        }

        @Test
        fun aToF() {
            runHappyTest(intArrayOf(0xab, 0xcd, 0xef, 0xfe, 0xdc, 0xba), "ab:cd:ef:fe:dc:ba")
        }

        @Test
        fun upperCase() {
            runHappyTest(intArrayOf(0xff, 0xff, 0xff, 0xff, 0xff, 0xff), "FF:FF:FF:FF:FF:FF")
        }

        @Test
        fun containsInvalidHex() {
            runSadTest("00:00:00:00:00:0g")
        }

        @Test
        fun tooShort() {
            runSadTest("ff:ff:ff:ff:ff:f")
        }

        @Test
        fun tooLong() {
            runSadTest("ff:ff:ff:ff:ff:fff")
        }

        @Test
        fun tooLong2() {
            runSadTest("ff:ff:ff:ff:ff:ff:")
        }

        @Test
        fun doubleSeparator() {
            runSadTest("ff:ff:ff:ff:ff::f")
        }

        @Test
        fun noSeparators() {
            runSadTest("ffffffffffff")
        }

        @Test
        fun emptyString() {
            runSadTest("")
        }

        private fun runHappyTest(expected: IntArray, input: String) {
            val expectedBytes = expected.map { it.toByte() }.toByteArray()
            assertArrayEquals(expectedBytes, convertMacAddressString(input))
        }

        private fun runSadTest(input: String) {
            assertThrows(IllegalArgumentException::class.java) {
                convertMacAddressString(input)
            }
        }
    }
}
