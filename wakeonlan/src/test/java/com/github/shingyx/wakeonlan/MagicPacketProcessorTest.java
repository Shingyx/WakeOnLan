package com.github.shingyx.wakeonlan;

import org.junit.Test;

import static org.junit.Assert.*;

public class MagicPacketProcessorTest {

    @Test
    public void convertMacAddressString_allZero() throws Exception {
        runHappyTest(new byte[]{0, 0, 0, 0, 0, 0}, "00:00:00:00:00:00");
    }

    @Test
    public void convertMacAddressString_allF() throws Exception {
        runHappyTest(new int[]{0xff, 0xff, 0xff, 0xff, 0xff, 0xff}, "ff:ff:ff:ff:ff:ff");
    }

    @Test
    public void convertMacAddressString_aToF() throws Exception {
        runHappyTest(new int[]{0xab, 0xcd, 0xef, 0xfe, 0xdc, 0xba}, "ab:cd:ef:fe:dc:ba");
    }

    @Test
    public void convertMacAddressString_upperCase() throws Exception {
        runHappyTest(new int[]{0xff, 0xff, 0xff, 0xff, 0xff, 0xff}, "FF:FF:FF:FF:FF:FF");
    }

    @Test
    public void convertMacAddressString_hyphenSeparator() throws Exception {
        runHappyTest(new byte[]{0, 0, 0, 0, 0, 0}, "00-00-00-00-00-00");
    }

    @Test
    public void convertMacAddressString_containsInvalidHex() throws Exception {
        runSadTest("00:00:00:00:00:0g");
    }

    @Test
    public void convertMacAddressString_tooShort() throws Exception {
        runSadTest("ff:ff:ff:ff:ff:f");
    }

    @Test
    public void convertMacAddressString_tooLong() throws Exception {
        runSadTest("ff:ff:ff:ff:ff:fff");
    }

    @Test
    public void convertMacAddressString_tooLong2() throws Exception {
        runSadTest("ff:ff:ff:ff:ff:ff-");
    }

    @Test
    public void convertMacAddressString_doubleSeparator() throws Exception {
        runSadTest("ff:ff:ff:ff:ff::f");
    }

    @Test
    public void convertMacAddressString_noSeparators() throws Exception {
        runSadTest("ffffffffffff");
    }

    @Test
    public void convertMacAddressString_emptyString() throws Exception {
        runSadTest("");
    }

    @Test
    public void convertMacAddressString_null() throws Exception {
        runSadTest(null);
    }

    private void runHappyTest(byte[] expected, String input) {
        assertArrayEquals(expected, MagicPacketProcessor.convertMacAddressString(input));
    }

    private void runHappyTest(int[] expected, String input) {
        byte[] expectedBytes = new byte[expected.length];
        for (int i = 0; i < expected.length; i++) {
            expectedBytes[i] = (byte) expected[i];
        }
        runHappyTest(expectedBytes, input);
    }

    private void runSadTest(String input) {
        try {
            MagicPacketProcessor.convertMacAddressString(input);
            fail("Expected an exception");
        } catch (IllegalArgumentException e) {
            // Caught an IllegalArgumentException
        }
    }
}
