package com.adobe.phonegap.push;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.support.v4.app.RemoteInput;

public class BackgroundHandlerActivity extends Activity implements PushConstants {
  private static String LOG_TAG = "Push_BackgroundHandlerActivity";

  /*
   * this activity will be started if the user touches a notification that we own.
   * We send it's data off to the push plugin for processing.
   * If needed, we boot up the main activity to kickstart the application.
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  public void onCreate (Bundle savedInstanceState) {
    FCMService gcm = new FCMService();

    Intent intent = getIntent();

    int notId = intent.getExtras().getInt(NOT_ID, 0);
    Log.d(LOG_TAG, "not id = " + notId);
    gcm.setNotification(notId, "");
    super.onCreate(savedInstanceState);
    Log.v(LOG_TAG, "onCreate");
    String callback = getIntent().getExtras().getString("callback");
    Log.d(LOG_TAG, "callback = " + callback);
    boolean startOnBackground = getIntent().getExtras().getBoolean(START_IN_BACKGROUND, false);
    boolean dismissed = getIntent().getExtras().getBoolean(DISMISSED, false);
    Log.d(LOG_TAG, "dismissed = " + dismissed);

    if (!startOnBackground) {
      NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.cancel(FCMService.getAppName(this), notId);
    }

    boolean isPushPluginActive = PushPlugin.isActive();

    processPushBundle(isPushPluginActive, intent);
    finish();

    if (!dismissed) {
      // Tap the notification, app should start.
      if (!isPushPluginActive) {
        forceMainActivityReload(false);
      } else {
        forceMainActivityReload(true);
      }
    }
  }

  /**
   * Takes the pushBundle extras from the intent,
   * and sends it through to the PushPlugin for processing.
   */
  private boolean processPushBundle (boolean isPushPluginActive, Intent intent) {
    Bundle extras = getIntent().getExtras();
    Bundle remoteInput = null;

    if (extras != null) {
      Bundle originalExtras = extras.getBundle(PUSH_BUNDLE);

      if (originalExtras == null) {
        originalExtras = extras;
        originalExtras.remove(FROM);
        originalExtras.remove(MESSAGE_ID);
        originalExtras.remove(COLLAPSE_KEY);
      }

      originalExtras.putBoolean(FOREGROUND, false);
      originalExtras.putBoolean(COLDSTART, !isPushPluginActive);
      originalExtras.putBoolean(DISMISSED, extras.getBoolean(DISMISSED));
      originalExtras.putString(ACTION_CALLBACK, extras.getString(CALLBACK));
      originalExtras.remove(NO_CACHE);

      remoteInput = RemoteInput.getResultsFromIntent(intent);

      if (remoteInput != null) {
        String inputString = remoteInput.getCharSequence(INLINE_REPLY).toString();
        Log.d(LOG_TAG, "response: " + inputString);
        originalExtras.putString(INLINE_REPLY, inputString);
      }

      PushPlugin.sendExtras(originalExtras);
    }
    return remoteInput == null;
  }

  /**
   * Forces the main activity to re-launch if it's unloaded.
   */
  private void forceMainActivityReload (boolean startOnBackground) {
    PackageManager pm = getPackageManager();
    Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      Bundle originalExtras = extras.getBundle(PUSH_BUNDLE);

      if (originalExtras != null) {
        launchIntent.putExtras(originalExtras);
      }

      launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      launchIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
      launchIntent.putExtra(START_IN_BACKGROUND, startOnBackground);
    }

    startActivity(launchIntent);
  }

  @Override
  protected void onResume () {
    super.onResume();
    final NotificationManager notificationManager = (NotificationManager) this.getSystemService(
      Context.NOTIFICATION_SERVICE);
    notificationManager.cancelAll();
  }
}
