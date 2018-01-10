package com.github.shingyx.wakeonlan;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MagicPacketSenderTest {

    private MagicPacketSender instance;

    @Before
    public void setup() {
        instance = new MagicPacketSender();
    }

    @Test
    public void isValidMacAddress_allZero() throws Exception {
        assertTrue(instance.isValidMacAddress("00:00:00:00:00:00"));
    }

    @Test
    public void isValidMacAddress_allF() throws Exception {
        assertTrue(instance.isValidMacAddress("ff:ff:ff:ff:ff:ff"));
    }

    @Test
    public void isValidMacAddress_aToF() throws Exception {
        assertTrue(instance.isValidMacAddress("ab:cd:ef:fe:dc:ba"));
    }

    @Test
    public void isValidMacAddress_upperCase() throws Exception {
        assertTrue(instance.isValidMacAddress("FF:FF:FF:FF:FF:FF"));
    }

    @Test
    public void isValidMacAddress_hyphenSeparator() throws Exception {
        assertTrue(instance.isValidMacAddress("00-00-00-00-00-00"));
    }

    @Test
    public void isValidMacAddress_containsInvalidHex() throws Exception {
        assertFalse(instance.isValidMacAddress("00:00:00:00:00:0g"));
    }

    @Test
    public void isValidMacAddress_tooShort() throws Exception {
        assertFalse(instance.isValidMacAddress("ff:ff:ff:ff:ff:f"));
    }

    @Test
    public void isValidMacAddress_tooLong() throws Exception {
        assertFalse(instance.isValidMacAddress("ff:ff:ff:ff:ff:fff"));
    }

    @Test
    public void isValidMacAddress_doubleSeparator() throws Exception {
        assertFalse(instance.isValidMacAddress("ff:ff:ff:ff:ff::f"));
    }

    @Test
    public void isValidMacAddress_noSeparators() throws Exception {
        assertFalse(instance.isValidMacAddress("ffffffffffff"));
    }

    @Test
    public void isValidMacAddress_null() throws Exception {
        assertFalse(instance.isValidMacAddress(null));
    }
}
