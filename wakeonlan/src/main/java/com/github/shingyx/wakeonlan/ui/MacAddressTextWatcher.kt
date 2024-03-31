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
        for (char in text) {
            if (char.isLetterOrDigit()) {
                if (digits % 2 == 0 && digits > 0 && digits < 12) {
                    builder.append(':')
                }
                builder.append(char)
                digits++
            }
        }
        if (text.lastOrNull() == ':') {
            builder.append(':')
        }
        return builder.toString()
    }
}
