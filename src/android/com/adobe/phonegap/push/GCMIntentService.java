package com.adobe.phonegap.push;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

@SuppressLint("NewApi")
public class GCMIntentService extends GCMBaseIntentService {

	private static final String LOG_TAG = "PushPlugin_GCMIntentService";
	
	public GCMIntentService() {
		super("GCMIntentService");
	}

	@Override
	public void onRegistered(Context context, String regId) {

		Log.v(LOG_TAG, "onRegistered: "+ regId);

		try {
			JSONObject json = new JSONObject().put("registrationId", regId);

			Log.v(LOG_TAG, "onRegistered: " + json.toString());

			PushPlugin.sendEvent( json );
		}
		catch(JSONException e) {
			// No message to the user is sent, JSON failed
			Log.e(LOG_TAG, "onRegistered: JSON exception");
		}
	}

	@Override
	public void onUnregistered(Context context, String regId) {
		Log.d(LOG_TAG, "onUnregistered - regId: " + regId);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.d(LOG_TAG, "onMessage - context: " + context);

		// Extract the payload from the message
		Bundle extras = intent.getExtras();
		if (extras != null) {
			// if we are in the foreground, just surface the payload, else post it to the statusbar
			if (PushPlugin.isInForeground()) {
				extras.putBoolean("foreground", true);
				PushPlugin.sendExtras(extras);
			}
			else {
				extras.putBoolean("foreground", false);

				// Send a notification if there is a message
				if (extras.getString("message") != null && extras.getString("message").length() != 0) {
					createNotification(context, extras);
				}
			}
		}
	}

	public void createNotification(Context context, Bundle extras) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String appName = getAppName(this);

		Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		notificationIntent.putExtra("pushBundle", extras);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		int defaults = Notification.DEFAULT_ALL;

		if (extras.getString("defaults") != null) {
			try {
				defaults = Integer.parseInt(extras.getString("defaults"));
			} catch (NumberFormatException e) {}
		}
		
		NotificationCompat.Builder mBuilder =
			new NotificationCompat.Builder(context)
				.setDefaults(defaults)
				.setSmallIcon(context.getApplicationInfo().icon)
				.setWhen(System.currentTimeMillis())
				.setContentTitle(extras.getString("title"))
				.setTicker(extras.getString("title"))
				.setContentIntent(contentIntent)
				.setAutoCancel(true);

		NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();  

		String message = extras.getString("message");
		String summaryText = extras.getString("summaryText");
		if (message != null) {
			mBuilder.setContentText(message);

			bigText.bigText(message);
			bigText.setBigContentTitle(extras.getString("title"));

			if (summaryText != null)
				bigText.setSummaryText(summaryText);
			else
				bigText.setSummaryText(extras.getString("title"));

			mBuilder.setStyle(bigText);
		} else {
			mBuilder.setContentText("<missing message content>");
		}

		String msgcnt = extras.getString("msgcnt");
		if (msgcnt != null) {
			mBuilder.setNumber(Integer.parseInt(msgcnt));
		}
		
		int notId = 0;
		
		try {
			notId = Integer.parseInt(extras.getString("notId"));
		}
		catch(NumberFormatException e) {
			Log.e(LOG_TAG, "Number format exception - Error parsing Notification ID: " + e.getMessage());
		}
		catch(Exception e) {
			Log.e(LOG_TAG, "Number format exception - Error parsing Notification ID" + e.getMessage());
		}
		
		mNotificationManager.notify((String) appName, notId, mBuilder.build());
	}
	
	private static String getAppName(Context context) {
		CharSequence appName =	context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
		return (String)appName;
	}
	
	@Override
	public void onError(Context context, String errorId) {
		Log.e(LOG_TAG, "onError - errorId: " + errorId);
	}

}
