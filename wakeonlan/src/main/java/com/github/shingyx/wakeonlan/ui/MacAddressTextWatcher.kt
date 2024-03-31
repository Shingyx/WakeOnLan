package com.github.shingyx.wakeonlan.ui

import android.text.Editable
import android.text.TextWatcher

class MacAddressTextWatcher : TextWatcher {
    private var previousText: String = ""

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        previousText = s.toString()
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(editable: Editable) {
        val text = editable.toString()
        val formattedText = formatMacAddress(text)
        if (formattedText != text) {
            editable.replace(0, editable.length, formattedText)
        }
    }

    private fun formatMacAddress(text: String): String {
        val builder = StringBuilder()
        var digits = 0
        var lastColonRequired = false
        for (char in text) {
            if (char.isLetterOrDigit()) {
                if (builder.lastOrNull() != ':' && allowSeparator(digits)) {
                    builder.append(':')
                    lastColonRequired = false
                }
                builder.append(char)
                digits++
            } else if (char == ':') {
                lastColonRequired = true
            }
        }
        if ((lastColonRequired || text.lastOrNull() == ':') && allowSeparator(digits)) {
            builder.append(':')
        }
        return builder.toString()
    }

    private fun allowSeparator(digits: Int): Boolean {
        return digits % 2 == 0 && digits in 1 until 12
    }
}
