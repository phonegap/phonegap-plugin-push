//
//  PushHandlerActivity.java
//
// Pushwoosh Push Notifications SDK
// www.pushwoosh.com
//
// MIT Licensed

package com.plugin.GCM;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class PushHandlerActivity extends Activity
{
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
    		{
    			try
    			{
     				JSONObject json;
    				json = new JSONObject().put("event", "message");

    				json.put("message", originalExtras.getString("message"));
    				json.put("msgcnt", originalExtras.getString("msgcnt"));
    				json.put("foreground", originalExtras.getBoolean("foreground"));

    				Log.v("PushHandlerActivity:handlePush", json.toString());

    				PushPlugin.sendJavascript( json );
    			}
    			catch( JSONException e)
    			{
    				Log.e("PushHandlerActivity:handlePush", "JSON exception");
    			}
    		}
	}
        finish();
  }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }
}
