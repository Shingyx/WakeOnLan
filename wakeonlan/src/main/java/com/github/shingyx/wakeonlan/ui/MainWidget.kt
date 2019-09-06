package com.github.shingyx.wakeonlan.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.github.shingyx.wakeonlan.R
import com.github.shingyx.wakeonlan.data.MagicPacketProcessor
import com.github.shingyx.wakeonlan.data.Storage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

private const val SEND_PACKET = "SEND_PACKET"

class MainWidget : AppWidgetProvider() {
    private val scope = MainScope()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.main_widget)

            val intent = Intent(SEND_PACKET, null, context, this.javaClass)
            views.setOnClickPendingIntent(
                R.id.widgetLayout,
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == SEND_PACKET) {
            scope.launch {
                val result = Result.runCatching {
                    val host = Storage.getInstance(context).savedHost
                        ?: throw Exception(context.getString(R.string.error_null_host))
                    MagicPacketProcessor(context).send(host)
                }
                val error = result.exceptionOrNull()?.message

                val message = if (error == null) {
                    context.getString(R.string.pc_turned_on)
                } else {
                    "${context.getString(R.string.error)}: $error"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
