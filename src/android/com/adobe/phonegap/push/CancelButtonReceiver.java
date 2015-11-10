package com.adobe.phonegap.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.util.Log;

public class CancelButtonReceiver extends BroadcastReceiver implements PushConstants {
    private static String LOG_TAG = "PushPlugin_CancelButtonReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive Close action");
        String appName = intent.getStringExtra(APP_NAME);
        int notId = intent.getIntExtra(NOT_ID, -1);

        // Cancel notification.
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(appName, notId);
    }
}
