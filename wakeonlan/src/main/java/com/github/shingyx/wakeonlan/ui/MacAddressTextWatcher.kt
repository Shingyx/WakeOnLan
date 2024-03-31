package com.github.shingyx.wakeonlan.ui

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class MacAddressTextWatcher(
    private val editText: EditText
) : TextWatcher {
    private var previousText: String = ""

    override fun afterTextChanged(text: Editable) {}

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        previousText = text.toString()
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        val previousCursorPosition = start + before
        val previousTextAfterCursor = previousText.substring(previousCursorPosition)
        val previousDigitsAfterCursor = getNumberOfDigits(previousTextAfterCursor)

        val newText = formatMacAddress(text.toString())
        editText.removeTextChangedListener(this)
        editText.setText(newText)
        editText.setSelection(getNewCursorPosition(previousDigitsAfterCursor, newText))
        editText.addTextChangedListener(this)
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
        return builder.toString()
    }

    private fun getNumberOfDigits(text: String): Int {
        return text.count(Char::isLetterOrDigit)
    }

    private fun getNewCursorPosition(previousDigitsAfterCursor: Int, newText: String): Int {
        var charsAfterCursor = 0
        var remainingDigits = previousDigitsAfterCursor
        for (char in newText.reversed()) {
            if (remainingDigits == 0) {
                break
            }
            if (char.isLetterOrDigit()) {
                remainingDigits--
            }
            charsAfterCursor++
        }
        return newText.length - charsAfterCursor
    }
}
