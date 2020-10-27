package com.adobe.phonegap.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class PushDismissedHandler extends BroadcastReceiver implements PushConstants {
  private static String LOG_TAG = "Push_DismissedHandler";

  @Override
  public void onReceive (Context context, Intent intent) {
    Bundle extras = intent.getExtras();
    FCMService fcm = new FCMService();
    String action = intent.getAction();
    int notID = intent.getIntExtra(NOT_ID, 0);

    if (action.equals(PUSH_DISMISSED)) {
      Log.d(LOG_TAG, "PushDismissedHandler = " + extras);
      Log.d(LOG_TAG, "not id = " + notID);

      fcm.setNotification(notID, "");
    }
  }
}
