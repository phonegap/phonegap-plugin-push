package com.google.android.gcm;

import java.io.IOException;
import java.util.List;

import com.plugin.GCM.PushHandlerActivity;
import com.google.android.gcm.*;

import org.apache.cordova.example.R;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.plugin.GCM.PushPlugin;


@SuppressLint("NewApi")
public class GCMIntentService extends GCMBaseIntentService {

  public static final String ME="GCMReceiver";
  public static final int notificationID = 237;

  public GCMIntentService() {
    super("GCMIntentService");
  }
  private static final String TAG = "GCMIntentService";

  @Override
  public void onRegistered(Context context, String regId) {

    Log.v(ME + ":onRegistered", "Registration ID arrived!");
    Log.v(ME + ":onRegistered", regId);

    JSONObject json;

    try
    {
      json = new JSONObject().put("event", "registered");
      json.put("regid", regId);

      Log.v(ME + ":onRegisterd", json.toString());

      // Send this JSON data to the JavaScript application above EVENT should be set to the msg type
      // In this case this is the registration ID
      PushPlugin.sendJavascript( json );

    }
    catch( JSONException e)
    {
      // No message to the user is sent, JSON failed
      Log.e(ME + ":onRegisterd", "JSON exception");
    }
  }

  @Override
  public void onUnregistered(Context context, String regId) {
    Log.d(TAG, "onUnregistered - regId: " + regId);
  }

  @Override
  protected void onMessage(Context context, Intent intent) {
	Log.d(TAG, "onMessage - context: " + context);

    // Extract the payload from the message
	Bundle extras = intent.getExtras();
    if (extras != null)
    {
    	boolean	foreground = this.isInForeground();
    	extras.putBoolean("foreground", foreground);
    	
    	// we can't call into the JS app if we are in the background
    	if (foreground)
    	{
    		try
    		{
    			JSONObject json;
    			json = new JSONObject().put("event", "message");
            	
    			// My application on my host server sends back to "EXTRAS" variables message and msgcnt
    			// Depending on how you build your server app you can specify what variables you want to send
    			json.put("message", extras.getString("message"));
    			json.put("msgcnt", extras.getString("msgcnt"));
    			json.put("soundname", extras.getString("soundname"));
    			json.put("foreground", foreground);

    			Log.v(ME + ":onMessage ", json.toString());

    			PushPlugin.sendJavascript( json );
    		}
    		catch( JSONException e)
    		{
    			Log.e(ME + ":onMessage", "JSON exception");
    		}
    	}
    	else
    		this.onReceive(context, extras);
	}
  }
  
	// This is called for all notifications, whether the app is in the foreground or the background.
	// This is so we can update any existing notification in the tray, for our app.
	public void onReceive(Context context, Bundle extras)
	{
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		CharSequence appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
		if (null == appName)
			appName = "";
		
		Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		notificationIntent.putExtra("pushBundle", extras);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);		

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
		.setSmallIcon(R.drawable.icon)
		.setWhen(System.currentTimeMillis())
		.setContentTitle(appName)
		.setTicker(appName)
		.setContentText(extras.getString("message"))
		.setNumber(Integer.parseInt(extras.getString("msgcnt")))
		.setContentIntent(contentIntent);
		
		mNotificationManager.notify(notificationID, mBuilder.build());
	
		try
		{
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
			r.play();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean isInForeground()
	{
	    ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningTaskInfo> services = activityManager
	    		.getRunningTasks(Integer.MAX_VALUE);

	    if (services.get(0).topActivity.getPackageName().toString()
	            .equalsIgnoreCase(getApplicationContext().getPackageName().toString()))
	        return true;
		
		return false;
	}
	

  @Override
  public void onError(Context context, String errorId) {
    Log.e(TAG, "onError - errorId: " + errorId);
  }

}
