package com.adobe.phonegap.push;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class PushHandlerActivity extends Activity implements PushConstants {
    private static String LOG_TAG = "PushPlugin_PushHandlerActivity";

    /*
     * this activity will be started if the user touches a notification that we own.
     * We send it's data off to the push plugin for processing.
     * If needed, we boot up the main activity to kickstart the application.
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        String appName = getIntent().getStringExtra(APP_NAME);
        int notId = getIntent().getIntExtra(NOT_ID, -1);

        GCMIntentService gcm = new GCMIntentService();
        gcm.setNotification(notId, "");
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate");

        boolean isPushPluginActive = PushPlugin.isActive();
        processPushBundle(isPushPluginActive);

        // Close after click.
        if (getIntent().getIntExtra(CLOSE_AFTER_CLICK, 0) == 1) {
            final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(appName, notId);
        }
        finish();

        if (!isPushPluginActive) {
            forceMainActivityReload();
        }
    }

    /**
     * Takes the pushBundle extras from the intent,
     * and sends it through to the PushPlugin for processing.
     */
    private void processPushBundle(boolean isPushPluginActive) {
        Bundle extras = getIntent().getExtras();

        if (extras != null)	{
            Bundle originalExtras = extras.getBundle(PUSH_BUNDLE);

            originalExtras.putBoolean(FOREGROUND, false);
            originalExtras.putBoolean(COLDSTART, !isPushPluginActive);
            originalExtras.putString(CALLBACK, extras.getString("callback"));

            PushPlugin.sendExtras(originalExtras);
        }
    }

    /**
     * Forces the main activity to re-launch if it's unloaded.
     */
    private void forceMainActivityReload() {
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        startActivity(launchIntent);
    }

    /**
     * We don't need to close all notifications after open-hide app!
     */
//    @Override
//    protected void onResume() {
//        super.onResume();
//        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.cancelAll();
//    }
}
