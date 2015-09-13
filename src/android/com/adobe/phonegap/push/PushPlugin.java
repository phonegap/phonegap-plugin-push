package com.adobe.phonegap.push;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
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

public class PushPlugin extends CordovaPlugin implements PushConstants {

    public static final String LOG_TAG = "PushPlugin";

    private static CallbackContext pushContext;
    private static CordovaWebView gWebView;
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
            JSONObject jo = null;

            Log.v(LOG_TAG, "execute: data=" + data.toString());

            try {
                jo = data.getJSONObject(0).getJSONObject(ANDROID);

                gWebView = this.webView;
                Log.v(LOG_TAG, "execute: jo=" + jo.toString());

                String senderID = jo.getString(SENDER_ID);

                Log.v(LOG_TAG, "execute: senderID=" + senderID);

                GCMRegistrar.register(getApplicationContext(), senderID);
                result = true;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "execute: Got JSON Exception " + e.getMessage());
                result = false;
                callbackContext.error(e.getMessage());
            }

            if (jo != null) {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(COM_ADOBE_PHONEGAP_PUSH, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                try {
                    editor.putString(ICON, jo.getString(ICON));
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "no icon option");
                }
                try {
                    editor.putString(ICON_COLOR, jo.getString(ICON_COLOR));
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "no iconColor option");
                }
                editor.putBoolean(SOUND, jo.optBoolean(SOUND, true));
                editor.putBoolean(VIBRATE, jo.optBoolean(VIBRATE, true));
                editor.putBoolean(CLEAR_NOTIFICATIONS, jo.optBoolean(CLEAR_NOTIFICATIONS, true));
                editor.commit();
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
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
        }

        return result;
    }

    public static void sendEvent(JSONObject _json) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, _json);
        pluginResult.setKeepCallback(true);
        pushContext.sendPluginResult(pluginResult);
    }

    public static void sendError(String message) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, message);
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

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(COM_ADOBE_PHONEGAP_PUSH, Context.MODE_PRIVATE);
        if (prefs.getBoolean(CLEAR_NOTIFICATIONS, true)) {
            final NotificationManager notificationManager = (NotificationManager) cordova.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
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
    private static JSONObject convertBundleToJson(Bundle extras) {
        try {
            JSONObject json = new JSONObject();
            JSONObject additionalData = new JSONObject();
            Iterator<String> it = extras.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                Object value = extras.get(key);
                 
                Log.d(LOG_TAG, "key = " + key);
                if (key.startsWith(GCM_NOTIFICATION)) {
                    key = key.substring(GCM_NOTIFICATION.length()+1, key.length());
                }

                // System data from Android
                if (key.equals(FROM) || key.equals(COLLAPSE_KEY)) {
                    additionalData.put(key, value);
                }
                else if (key.equals(FOREGROUND)) {
                    additionalData.put(key, extras.getBoolean(FOREGROUND));
                }
                else if (key.equals(COLDSTART)){
                    additionalData.put(key, extras.getBoolean(COLDSTART));
                } else if (key.equals(MESSAGE) || key.equals(BODY)) {
                    json.put(MESSAGE, value);
                } else if (key.equals(TITLE)) {
                    json.put(TITLE, value);
                } else if (key.equals(MSGCNT) || key.equals(BADGE)) {
                    json.put(COUNT, value);
                } else if (key.equals(SOUNDNAME) || key.equals(SOUND)) {
                    json.put(SOUND, value);
                } else if (key.equals(IMAGE)) {
                    json.put(IMAGE, value);
                } else if (key.equals(CALLBACK)) {
                    json.put(CALLBACK, value);
                }
                else if ( value instanceof String ) {
                    String strValue = (String)value;
                    try {
                        // Try to figure out if the value is another JSON object
                        if (strValue.startsWith("{")) {
                            additionalData.put(key, new JSONObject(strValue));
                        }
                        // Try to figure out if the value is another JSON array
                        else if (strValue.startsWith("[")) {
                            additionalData.put(key, new JSONArray(strValue));
                        }
                        else {
                            additionalData.put(key, value);
                        }                       
                    } catch (Exception e) {
                        additionalData.put(key, value);
                    }
                }
            } // while
            
            json.put(ADDITIONAL_DATA, additionalData);
            Log.v(LOG_TAG, "extrasToJSON: " + json.toString());

            return json;
        }
        catch( JSONException e) {
            Log.e(LOG_TAG, "extrasToJSON: JSON exception");
        }
        return null;
    }

    public static boolean isInForeground() {
      return gForeground;
    }

    public static boolean isActive() {
        return gWebView != null;
    }
}