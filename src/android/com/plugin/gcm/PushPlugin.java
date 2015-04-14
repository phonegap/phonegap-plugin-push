package com.plugin.gcm;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * @author awysocki
 */

public class PushPlugin extends CordovaPlugin {
	public static final String LOG_TAG = "PushPlugin";

	public static final String INITIALIZE = "init";
	public static final String UNREGISTER = "unregister";
	public static final String EXIT = "exit";

	private static CallbackContext pushContext;
	private static CordovaWebView gWebView;
	//private static String gECB;
	private static String gSenderID;
	private static Bundle gCachedExtras = null;
    private static boolean gForeground = false;

	/**
	 * Gets the application context from cordova's main activity.
	 * @return the application context
	 */
	private Context getApplicationContext() {
		return this.cordova.getActivity().getApplicationContext();
	}

	@Override
	public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {

		boolean result = false;

		Log.v(LOG_TAG, "execute: action=" + action);

		if (INITIALIZE.equals(action)) {
			pushContext = callbackContext;

			Log.v(LOG_TAG, "execute: data=" + data.toString());

			try {
				JSONObject jo = data.getJSONObject(0).getJSONObject("android");

				gWebView = this.webView;
				Log.v(LOG_TAG, "execute: jo=" + jo.toString());

				gSenderID = jo.getString("senderID");

				Log.v(LOG_TAG, "execute: senderID=" + gSenderID);

				GCMRegistrar.register(getApplicationContext(), gSenderID);
				result = true;
			} catch (JSONException e) {
				Log.e(LOG_TAG, "execute: Got JSON Exception " + e.getMessage());
				result = false;
				callbackContext.error(e.getMessage());
			}

			if ( gCachedExtras != null) {
				Log.v(LOG_TAG, "sending cached extras");
				sendExtras(gCachedExtras);
				gCachedExtras = null;
			}

		} else if (UNREGISTER.equals(action)) {

			GCMRegistrar.unregister(getApplicationContext());

			Log.v(LOG_TAG, "UNREGISTER");
			result = true;
			callbackContext.success();
		} else {
			result = false;
			Log.e(LOG_TAG, "Invalid action : " + action);
			callbackContext.error("Invalid action : " + action);
		}

		return result;
	}

	public static void sendEvent(JSONObject _json) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, _json);
        pluginResult.setKeepCallback(true);
        pushContext.sendPluginResult(pluginResult);
	}

	/*
	 * Sends the pushbundle extras to the client application.
	 * If the client application isn't currently active, it is cached for later processing.
	 */
	public static void sendExtras(Bundle extras) {
		if (extras != null) {
			if (gWebView != null) {
				sendEvent(convertBundleToJson(extras));
			} else {
				Log.v(LOG_TAG, "sendExtras: caching extras to send at a later time.");
				gCachedExtras = extras;
			}
		}
	}

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        gForeground = true;
    }

	@Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        gForeground = false;
        final NotificationManager notificationManager = (NotificationManager) cordova.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        gForeground = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gForeground = false;
		gWebView = null;
    }

    /*
     * serializes a bundle to JSON.
     */
    private static JSONObject convertBundleToJson(Bundle extras)
    {
		try {
			JSONObject json = new JSONObject();
			JSONObject additionalData = new JSONObject();
			Iterator<String> it = extras.keySet().iterator();
			while (it.hasNext())
			{
				String key = it.next();
				Object value = extras.get(key);
				 
				Log.d(LOG_TAG, "key = " + key);

				// System data from Android
				if (key.equals("from") || key.equals("collapse_key")) {
					additionalData.put(key, value);
				}
				else if (key.equals("foreground")) {
					additionalData.put(key, extras.getBoolean("foreground"));
				}
				else if (key.equals("coldstart")){
					additionalData.put(key, extras.getBoolean("coldstart"));
				} else if (key.equals("message") || key.equals("title")) {
					json.put(key, value);
				} else if (key.equals("msgcnt")) {
					json.put("count", value);
				} else if (key.equals("soundname")) {
					json.put("sound", value);
				}
				else 
				{
					if ( value instanceof String ) {
					// Try to figure out if the value is another JSON object

						String strValue = (String)value;
						if (strValue.startsWith("{")) {
							try {
								JSONObject json2 = new JSONObject(strValue);
								additionalData.put(key, json2);
							}
							catch (Exception e) {
								additionalData.put(key, value);
							}
							// Try to figure out if the value is another JSON array
						}
						else if (strValue.startsWith("["))
						{
							try
							{
								JSONArray json2 = new JSONArray(strValue);
								additionalData.put(key, json2);
							}
							catch (Exception e)
							{
								additionalData.put(key, value);
							}
						}
						else
						{
							additionalData.put(key, value);
						}
					}
				}
			} // while
			json.put("additionalData", additionalData);

			Log.v(LOG_TAG, "extrasToJSON: " + json.toString());

			return json;
		}
		catch( JSONException e)
		{
			Log.e(LOG_TAG, "extrasToJSON: JSON exception");
		}
		return null;
    }

    public static boolean isInForeground()
    {
      return gForeground;
    }

    public static boolean isActive()
    {
    	return gWebView != null;
    }
}
