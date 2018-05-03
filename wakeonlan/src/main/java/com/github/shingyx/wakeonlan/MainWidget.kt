package com.github.shingyx.wakeonlan

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.widget.RemoteViews
import android.widget.Toast

private const val SEND_PACKET = "SEND_PACKET"

class MainWidget : AppWidgetProvider() {
    private lateinit var handler: Handler
    private lateinit var magicPacketProcessor: MagicPacketProcessor

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.main_widget)

            // Add the click listener
            val intent = Intent(context, this.javaClass)
            intent.action = SEND_PACKET
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == SEND_PACKET) {
            lazySetup(context)

            SendPacketTask(magicPacketProcessor, { error ->
                val message = if (error == null) {
                    context.getString(R.string.packet_sent)
                } else {
                    "${context.getString(R.string.error)}: $error"
                }
                handler.post { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
            }).execute()
        }
    }

    private fun lazySetup(context: Context) {
        if (!this::handler.isInitialized) {
            handler = Handler()
        }
        if (!this::magicPacketProcessor.isInitialized) {
            magicPacketProcessor = MagicPacketProcessor(context)
        }
    }
}
