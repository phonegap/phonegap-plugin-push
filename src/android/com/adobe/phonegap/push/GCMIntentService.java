package com.adobe.phonegap.push;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
		String packageName = context.getPackageName();
		Resources resources = context.getResources();

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
				.setWhen(System.currentTimeMillis())
				.setContentTitle(extras.getString("title"))
				.setTicker(extras.getString("title"))
				.setContentIntent(contentIntent)
				.setAutoCancel(true);

		
 		SharedPreferences prefs = context.getSharedPreferences("com.adobe.phonegap.push", Context.MODE_PRIVATE);
 		String localIcon = prefs.getString("icon", null);
 		String localIconColor = prefs.getString("iconColor", null);
		String localLargeIcon = prefs.getString("largeIcon", null);
 		Log.d(LOG_TAG, "stored icon=" + localIcon);
 		Log.d(LOG_TAG, "stored iconColor=" + localIconColor);
		Log.d(LOG_TAG, "stored largeIcon=" + localLargeIcon);

		/*
 		 * Notification Icon Color
 		 *
 		 * Sets the small-icon background color of the notification.
 		 * To use, add the `iconColor` key to plugin android options
 		 *
 		 */
		int iconColor = 0;
		if (localIconColor != null) {
			try {
				iconColor = Color.parseColor(localIconColor);
			} catch (IllegalArgumentException e) {
				Log.e(LOG_TAG, "couldnt parse color from android options");
			}
		}
		if (iconColor != 0) {
			mBuilder.setColor(iconColor);
		}

 		/*
 		 * Notification Icon
 		 *
 		 * Sets the small-icon of the notification.
 		 * 
 		 * - checks the gcm data for the `icon` key
 		 * - if none, checks the plugin options for `icon` key
 		 * - if none, uses the application icon 
 		 *
 		 * The icon value must be a string that maps to a drawable resource.
 		 * If no resource is found, falls
 		 *
 		 */
 		int iconId = 0;
 		String gcmIcon = extras.getString("icon"); // from gcm
 		if (gcmIcon != null) {
 			iconId = resources.getIdentifier(gcmIcon, "drawable", packageName);
 			Log.d(LOG_TAG, "using icon from gcm");
 		} else if (localIcon != null) {
 			iconId = resources.getIdentifier(localIcon, "drawable", packageName);
 			Log.d(LOG_TAG, "using icon from plugin options");
 		}
 		if (iconId == 0) {
 			Log.d(LOG_TAG, "no icon resource found - using application icon");
 			iconId = context.getApplicationInfo().icon;
 		}
 		mBuilder.setSmallIcon(iconId);

 		/*
 		 * Notification Large-Icon
 		 *
 		 * Sets the large-icon of the notification
 		 *
 		 * - checks the gcm data for the `largeIcon` key
 		 * - if none, checks the plugin options for `largeIcon` key
 		 * - if none, we don't set the large icon
 		 *
 		 */
 		int largeIconId = 0;
 		String gcmLargeIcon = extras.getString("largeIcon"); // from gcm
 		if (gcmLargeIcon != null) {
 			largeIconId = resources.getIdentifier(gcmLargeIcon, "drawable", packageName);
 			Log.d(LOG_TAG, "using large-icon from gcm");	
 		} else if (localLargeIcon != null) {
 			largeIconId = resources.getIdentifier(localLargeIcon, "drawable", packageName);
 			Log.d(LOG_TAG, "using large-icon from plugin options");	
 		}
 		if (largeIconId != 0) {
 			Bitmap largeIconBitmap = BitmapFactory.decodeResource(resources, largeIconId);
 			mBuilder.setLargeIcon(largeIconBitmap);
 		}


		String message = extras.getString("message");
		if (message != null) {
			mBuilder.setContentText(message);
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
		CharSequence appName =  context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
		return (String)appName;
	}
	
	@Override
	public void onError(Context context, String errorId) {
		Log.e(LOG_TAG, "onError - errorId: " + errorId);
	}

}