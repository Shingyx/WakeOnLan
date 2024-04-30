package com.github.shingyx.wakeonlan.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.shingyx.wakeonlan.R
import com.github.shingyx.wakeonlan.data.NetworkHandler
import com.github.shingyx.wakeonlan.data.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

private const val ACTION_SEND_PACKET = "ACTION_SEND_PACKET"

class ShortcutActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val tag = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            Intent.ACTION_CREATE_SHORTCUT -> createShortcut()
            ACTION_SEND_PACKET -> launch { sendPacket() }
            else -> Log.w(tag, "Unknown intent action ${intent.action}")
        }

        finish()
    }

    private fun createShortcut() {
        val shortcutIntent = Intent(ACTION_SEND_PACKET, null, this, javaClass)
        val iconResource = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher)
        @Suppress("DEPRECATION") // Currently, the deprecated approach has a more consistent style
        val intent = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name))
            putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
        }
        setResult(Activity.RESULT_OK, intent)
    }

    private suspend fun sendPacket() {
        val result = runCatching {
            val host = Storage.getInstance(this).savedHost
                ?: throw Exception(getString(R.string.error_null_host))
            NetworkHandler(this).sendMagicPacket(host)
        }
        val error = result.exceptionOrNull()?.message

        val message = if (error == null) {
            getString(R.string.pc_turned_on)
        } else {
            "${getString(R.string.error)}: $error"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
