//
//  PushHandlerActivity.java
//
// Pushwoosh Push Notifications SDK
// www.pushwoosh.com
//
// MIT Licensed

package com.plugin.GCM;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class PushHandlerActivity extends Activity
{
	public static final int NOTIFICATION_ID = 237;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        handlePush();
    }

    private void handlePush()
    {
    	// If we are here, it is because we were launched via a notification. It is up to the author to decide what to do with it.
    	// You may simply ignore it since the notification already fired in the background. Either way, the background flag
    	// will let you know what the state was when the notification fired.
    	Bundle extras = this.getIntent().getExtras();
        if (extras != null)
        {
    		Bundle	originalExtras = extras.getBundle("pushBundle");
    		if (originalExtras != null)
    			PushHandlerActivity.sendToApp(originalExtras);
        }
        finish();

		// Now that we've processed the notification, remove it from the tray
		CharSequence appName = this.getPackageManager().getApplicationLabel(this.getApplicationInfo());
		if (null == appName)
			appName = "";
        
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // use a combo of appName and id to insure uniqueness since this plugin may be running
        // in multiple apps on a particular device.
        mNotificationManager.cancel((String) appName, NOTIFICATION_ID);
	}
    
    public static void sendToApp(Bundle extras)
    {
		try
		{
			JSONObject json;
			json = new JSONObject().put("event", "message");
        
			JSONObject jsondata = new JSONObject();
			Iterator<String> it = extras.keySet().iterator();
			while (it.hasNext())
			{
				String key = it.next();
				String value = extras.getString(key);	
        	
				// System data from Android
				if (key.equals("from") || key.equals("collapse_key"))
				{
					json.put(key, value);
				}
				else if (key.equals("foreground"))
				{
					json.put(key, extras.getBoolean("foreground"));
				}
				else
				{
					// Maintain backwards compatibility
					if (key.equals("message") || key.equals("msgcnt") || key.equals("soundname"))
					{
						json.put(key, value);
					}
        		
					// Try to figure out if the value is another JSON object
					if (value.startsWith("{"))
					{
						try
						{
							JSONObject json2 = new JSONObject(value);
							jsondata.put(key, json2);
						}
						catch (Exception e)
						{
							jsondata.put(key, value);
						}
						// Try to figure out if the value is another JSON array
					}
					else if (value.startsWith("["))
					{
						try
						{
							JSONArray json2 = new JSONArray(value);
							jsondata.put(key, json2);
						}
						catch (Exception e)
						{
							jsondata.put(key, value);
						}
					}
					else
					{
						jsondata.put(key, value);
					}
				}
			} // while
			json.put("payload", jsondata);
        
			Log.v("sendToApp:", json.toString());

			PushPlugin.sendJavascript( json );
			// Send the MESSAGE to the Javascript application
		}
		catch( JSONException e)
		{
			Log.e("sendToApp:", "JSON exception");
		}        	
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }
}
