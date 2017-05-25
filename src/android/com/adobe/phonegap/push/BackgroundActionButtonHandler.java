package com.adobe.phonegap.push;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.support.v4.app.RemoteInput;

public class BackgroundActionButtonHandler extends BroadcastReceiver implements PushConstants {
    private static String LOG_TAG = "Push_BGActionButton";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d(LOG_TAG, "BackgroundActionButtonHandler = " + extras);

        int notId = intent.getIntExtra(NOT_ID, 0);
        Log.d(LOG_TAG, "not id = " + notId);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(FCMService.getAppName(context), notId);

        if (extras != null)	{
            Bundle originalExtras = extras.getBundle(PUSH_BUNDLE);

            originalExtras.putBoolean(FOREGROUND, false);
            originalExtras.putBoolean(COLDSTART, false);
            originalExtras.putString(ACTION_CALLBACK, extras.getString(CALLBACK));

            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput != null) {
                String inputString = remoteInput.getCharSequence(INLINE_REPLY).toString();
                Log.d(LOG_TAG, "response: " + inputString);
                originalExtras.putString(INLINE_REPLY, inputString);
            }

            PushPlugin.sendExtras(originalExtras);
        }
     }
}
