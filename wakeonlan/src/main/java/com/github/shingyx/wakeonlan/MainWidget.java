package com.github.shingyx.wakeonlan;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MainWidget extends AppWidgetProvider {

    private static final String SEND_PACKET = "SEND_PACKET";

    private Handler handler;
    private MagicPacketProcessor magicPacketProcessor;

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_widget);

        // Add the click listener
        Intent intent = new Intent(context, MainWidget.class);
        intent.setAction(SEND_PACKET);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (SEND_PACKET.equals(intent.getAction())) {
            lazySetup(context);

            new SendPacketTask(magicPacketProcessor, (String error) -> {
                String message = error == null ?
                        context.getString(R.string.packet_sent) :
                        (context.getString(R.string.error) + ": " + error);
                handler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
            }).execute();
        }
    }

    private void lazySetup(Context context) {
        if (handler == null) {
            handler = new Handler();
        }
        if (magicPacketProcessor == null) {
            magicPacketProcessor = new MagicPacketProcessor(context);
        }
    }
}

