package com.adobe.phonegap.push;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

public class PushPlugin extends CordovaPlugin implements PushConstants {

  public static final String LOG_TAG = "Push_Plugin";

  private static CallbackContext pushContext;
  private static CordovaWebView gWebView;
  private static List<Bundle> gCachedExtras = Collections.synchronizedList(new ArrayList<Bundle>());
  private static boolean gForeground = false;

  private static String registration_id = "";

  /**
   * Gets the application context from cordova's main activity.
   * @return the application context
   */
  private Context getApplicationContext() {
    return this.cordova.getActivity().getApplicationContext();
  }

  @TargetApi(26)
  private JSONArray listChannels() throws JSONException {
    JSONArray channels = new JSONArray();
    // only call on Android O and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      final NotificationManager notificationManager = (NotificationManager) cordova.getActivity()
          .getSystemService(Context.NOTIFICATION_SERVICE);
      List<NotificationChannel> notificationChannels = notificationManager.getNotificationChannels();
      for (NotificationChannel notificationChannel : notificationChannels) {
        JSONObject channel = new JSONObject();
        channel.put(CHANNEL_ID, notificationChannel.getId());
        channel.put(CHANNEL_DESCRIPTION, notificationChannel.getDescription());
        channels.put(channel);
      }
    }
    return channels;
  }

  @TargetApi(26)
  private void deleteChannel(String channelId) {
    // only call on Android O and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      final NotificationManager notificationManager = (NotificationManager) cordova.getActivity()
          .getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.deleteNotificationChannel(channelId);
    }
  }

  @TargetApi(26)
  private void createChannel(JSONObject channel) throws JSONException {
    // only call on Android O and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      final NotificationManager notificationManager = (NotificationManager) cordova.getActivity()
          .getSystemService(Context.NOTIFICATION_SERVICE);

      String packageName = getApplicationContext().getPackageName();
      NotificationChannel mChannel = new NotificationChannel(channel.getString(CHANNEL_ID),
          channel.optString(CHANNEL_DESCRIPTION, ""),
          channel.optInt(CHANNEL_IMPORTANCE, NotificationManager.IMPORTANCE_DEFAULT));

      int lightColor = channel.optInt(CHANNEL_LIGHT_COLOR, -1);
      if (lightColor != -1) {
        mChannel.setLightColor(lightColor);
      }

      int visibility = channel.optInt(VISIBILITY, NotificationCompat.VISIBILITY_PUBLIC);
      mChannel.setLockscreenVisibility(visibility);

      boolean badge = channel.optBoolean(BADGE, true);
      mChannel.setShowBadge(badge);

      String sound = channel.optString(SOUND, "default");
      AudioAttributes audioAttributes = new AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build();
      if (SOUND_RINGTONE.equals(sound)) {
        mChannel.setSound(android.provider.Settings.System.DEFAULT_RINGTONE_URI, audioAttributes);
      } else if (sound != null && !sound.contentEquals(SOUND_DEFAULT)) {
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/raw/" + sound);
        mChannel.setSound(soundUri, audioAttributes);
      } else {
        mChannel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);
      }

      //JSONArray pattern = channel.optJSONArray(CHANNEL_VIBRATION);
      //mChannel.setVibrationPattern();

      notificationManager.createNotificationChannel(mChannel);
    }
  }

  @TargetApi(26)
  private void createDefaultNotificationChannelIfNeeded(JSONObject options) {
    // only call on Android O and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      final NotificationManager notificationManager = (NotificationManager) cordova.getActivity()
          .getSystemService(Context.NOTIFICATION_SERVICE);
      List<NotificationChannel> channels = notificationManager.getNotificationChannels();
      if (channels.size() == 0) {
        NotificationChannel mChannel = new NotificationChannel(DEFAULT_CHANNEL_ID, "PhoneGap PushPlugin",
            NotificationManager.IMPORTANCE_DEFAULT);
        mChannel.enableVibration(options.optBoolean(VIBRATE, true));
        mChannel.setShowBadge(true);
        notificationManager.createNotificationChannel(mChannel);
      }
    }
  }

  @Override
  public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) {
    Log.v(LOG_TAG, "execute: action=" + action);
    gWebView = this.webView;

    if (INITIALIZE.equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          pushContext = callbackContext;
          JSONObject jo = null;

          Log.v(LOG_TAG, "execute: data=" + data.toString());
          SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(COM_ADOBE_PHONEGAP_PUSH,
              Context.MODE_PRIVATE);
          String token = null;
          String senderID = null;

          try {
            jo = data.getJSONObject(0).getJSONObject(ANDROID);

            // If no NotificationChannels exist create the default one
            createDefaultNotificationChannelIfNeeded(jo);

            Log.v(LOG_TAG, "execute: jo=" + jo.toString());

            senderID = getStringResourceByName(GCM_DEFAULT_SENDER_ID);

            Log.v(LOG_TAG, "execute: senderID=" + senderID);

            token = FirebaseInstanceId.getInstance().getToken();

            if (token == null) {
              token = FirebaseInstanceId.getInstance().getToken(senderID, FCM);
            }

            if (!"".equals(token)) {
              JSONObject json = new JSONObject().put(REGISTRATION_ID, token);
              json.put(REGISTRATION_TYPE, FCM);

              Log.v(LOG_TAG, "onRegistered: " + json.toString());

              JSONArray topics = jo.optJSONArray(TOPICS);
              subscribeToTopics(topics, registration_id);

              PushPlugin.sendEvent(json);
            } else {
              callbackContext.error("Empty registration ID received from FCM");
              return;
            }
          } catch (JSONException e) {
            Log.e(LOG_TAG, "execute: Got JSON Exception " + e.getMessage());
            callbackContext.error(e.getMessage());
          } catch (IOException e) {
            Log.e(LOG_TAG, "execute: Got IO Exception " + e.getMessage());
            callbackContext.error(e.getMessage());
          } catch (Resources.NotFoundException e) {

            Log.e(LOG_TAG, "execute: Got Resources NotFoundException " + e.getMessage());
            callbackContext.error(e.getMessage());
          }

          if (jo != null) {
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

            boolean clearBadge = jo.optBoolean(CLEAR_BADGE, false);
            if (clearBadge) {
              setApplicationIconBadgeNumber(getApplicationContext(), 0);
            }

            editor.putBoolean(SOUND, jo.optBoolean(SOUND, true));
            editor.putBoolean(VIBRATE, jo.optBoolean(VIBRATE, true));
            editor.putBoolean(CLEAR_BADGE, clearBadge);
            editor.putBoolean(CLEAR_NOTIFICATIONS, jo.optBoolean(CLEAR_NOTIFICATIONS, true));
            editor.putBoolean(FORCE_SHOW, jo.optBoolean(FORCE_SHOW, false));
            editor.putString(SENDER_ID, senderID);
            editor.putString(MESSAGE_KEY, jo.optString(MESSAGE_KEY));
            editor.putString(TITLE_KEY, jo.optString(TITLE_KEY));
            editor.commit();

          }

          if (!gCachedExtras.isEmpty()) {
            Log.v(LOG_TAG, "sending cached extras");
            synchronized (gCachedExtras) {
              Iterator<Bundle> gCachedExtrasIterator = gCachedExtras.iterator();
              while (gCachedExtrasIterator.hasNext()) {
                sendExtras(gCachedExtrasIterator.next());
              }
            }
            gCachedExtras.clear();
          }
        }
      });
    } else if (UNREGISTER.equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          try {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(COM_ADOBE_PHONEGAP_PUSH,
                Context.MODE_PRIVATE);
            JSONArray topics = data.optJSONArray(0);
            if (topics != null && !"".equals(registration_id)) {
              unsubscribeFromTopics(topics, registration_id);
            } else {
              FirebaseInstanceId.getInstance().deleteInstanceId();
              Log.v(LOG_TAG, "UNREGISTER");

              // Remove shared prefs
              SharedPreferences.Editor editor = sharedPref.edit();
              editor.remove(SOUND);
              editor.remove(VIBRATE);
              editor.remove(CLEAR_BADGE);
              editor.remove(CLEAR_NOTIFICATIONS);
              editor.remove(FORCE_SHOW);
              editor.remove(SENDER_ID);
              editor.commit();
            }

            callbackContext.success();
          } catch (IOException e) {
            Log.e(LOG_TAG, "execute: Got JSON Exception " + e.getMessage());
            callbackContext.error(e.getMessage());
          }
        }
      });
    } else if (FINISH.equals(action)) {
      callbackContext.success();
    } else if (HAS_PERMISSION.equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          JSONObject jo = new JSONObject();
          try {
            Log.d(LOG_TAG,
                "has permission: " + NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled());
            jo.put("isEnabled", NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled());
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jo);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
          } catch (UnknownError e) {
            callbackContext.error(e.getMessage());
          } catch (JSONException e) {
            callbackContext.error(e.getMessage());
          }
        }
      });
    } else if (SET_APPLICATION_ICON_BADGE_NUMBER.equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          Log.v(LOG_TAG, "setApplicationIconBadgeNumber: data=" + data.toString());
          try {
            setApplicationIconBadgeNumber(getApplicationContext(), data.getJSONObject(0).getInt(BADGE));
          } catch (JSONException e) {
            callbackContext.error(e.getMessage());
          }
          callbackContext.success();
        }
      });
    } else if (GET_APPLICATION_ICON_BADGE_NUMBER.equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          Log.v(LOG_TAG, "getApplicationIconBadgeNumber");
          callbackContext.success(getApplicationIconBadgeNumber(getApplicationContext()));
        }
      });
    } else if (CLEAR_ALL_NOTIFICATIONS.equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          Log.v(LOG_TAG, "clearAllNotifications");
          clearAllNotifications();
          callbackContext.success();
        }
      });
    } else if (SUBSCRIBE.equals(action)) {
      // Subscribing for a topic
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          try {
            String topic = data.getString(0);
            subscribeToTopic(topic, registration_id);
            callbackContext.success();
          } catch (JSONException e) {
            callbackContext.error(e.getMessage());
          }
        }
      });
    } else if (UNSUBSCRIBE.equals(action)) {
      // un-subscribing for a topic
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          try {
            String topic = data.getString(0);
            unsubscribeFromTopic(topic, registration_id);
            callbackContext.success();
          } catch (JSONException e) {
            callbackContext.error(e.getMessage());
          }
        }
      });
    } else if (CREATE_CHANNEL.equals(action)) {
      // un-subscribing for a topic
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          try {
            // call create channel
            createChannel(data.getJSONObject(0));
            callbackContext.success();
          } catch (JSONException e) {
            callbackContext.error(e.getMessage());
          }
        }
      });
    } else if (DELETE_CHANNEL.equals(action)) {
      // un-subscribing for a topic
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          try {
            String channelId = data.getString(0);
            deleteChannel(channelId);
            callbackContext.success();
          } catch (JSONException e) {
            callbackContext.error(e.getMessage());
          }
        }
      });
    } else if (LIST_CHANNELS.equals(action)) {
      // un-subscribing for a topic
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          try {
            callbackContext.success(listChannels());
          } catch (JSONException e) {
            callbackContext.error(e.getMessage());
          }
        }
      });
    } else {
      Log.e(LOG_TAG, "Invalid action : " + action);
      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
      return false;
    }

    return true;
  }

  public static void sendEvent(JSONObject _json) {
    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, _json);
    pluginResult.setKeepCallback(true);
    if (pushContext != null) {
      pushContext.sendPluginResult(pluginResult);
    }
  }

  public static void sendError(String message) {
    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, message);
    pluginResult.setKeepCallback(true);
    if (pushContext != null) {
      pushContext.sendPluginResult(pluginResult);
    }
  }

  /*
   * Sends the pushbundle extras to the client application.
   * If the client application isn't currently active and the no-cache flag is not set, it is cached for later processing.
   */
  public static void sendExtras(Bundle extras) {
    if (extras != null) {
      String noCache = extras.getString(NO_CACHE);
      if (gWebView != null) {
        sendEvent(convertBundleToJson(extras));
      } else if (!"1".equals(noCache)) {
        Log.v(LOG_TAG, "sendExtras: caching extras to send at a later time.");
        gCachedExtras.add(extras);
      }
    }
  }

  /*
   * Retrives badge count from SharedPreferences
   */
  public static int getApplicationIconBadgeNumber(Context context) {
    SharedPreferences settings = context.getSharedPreferences(BADGE, Context.MODE_PRIVATE);
    return settings.getInt(BADGE, 0);
  }

  /*
   * Sets badge count on application icon and in SharedPreferences
   */
  public static void setApplicationIconBadgeNumber(Context context, int badgeCount) {
    if (badgeCount > 0) {
      ShortcutBadger.applyCount(context, badgeCount);
    } else {
      ShortcutBadger.removeCount(context);
    }

    SharedPreferences.Editor editor = context.getSharedPreferences(BADGE, Context.MODE_PRIVATE).edit();
    editor.putInt(BADGE, Math.max(badgeCount, 0));
    editor.apply();
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

    SharedPreferences prefs = getApplicationContext().getSharedPreferences(COM_ADOBE_PHONEGAP_PUSH,
        Context.MODE_PRIVATE);
    if (prefs.getBoolean(CLEAR_NOTIFICATIONS, true)) {
      clearAllNotifications();
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

  private void clearAllNotifications() {
    final NotificationManager notificationManager = (NotificationManager) cordova.getActivity()
        .getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancelAll();
  }

  private void subscribeToTopics(JSONArray topics, String registrationToken) {
    if (topics != null) {
      String topic = null;
      for (int i = 0; i < topics.length(); i++) {
        topic = topics.optString(i, null);
        subscribeToTopic(topic, registrationToken);
      }
    }
  }

  private void subscribeToTopic(String topic, String registrationToken) {
    if (topic != null) {
      Log.d(LOG_TAG, "Subscribing to topic: " + topic);
      FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }
  }

  private void unsubscribeFromTopics(JSONArray topics, String registrationToken) {
    if (topics != null) {
      String topic = null;
      for (int i = 0; i < topics.length(); i++) {
        topic = topics.optString(i, null);
        unsubscribeFromTopic(topic, registrationToken);
      }
    }
  }

  private void unsubscribeFromTopic(String topic, String registrationToken) {
    if (topic != null) {
      Log.d(LOG_TAG, "Unsubscribing to topic: " + topic);
      FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
    }
  }

  /*
   * serializes a bundle to JSON.
   */
  private static JSONObject convertBundleToJson(Bundle extras) {
    Log.d(LOG_TAG, "convert extras to json");
    try {
      JSONObject json = new JSONObject();
      JSONObject additionalData = new JSONObject();

      // Add any keys that need to be in top level json to this set
      HashSet<String> jsonKeySet = new HashSet();
      Collections.addAll(jsonKeySet, TITLE, MESSAGE, COUNT, SOUND, IMAGE);

      Iterator<String> it = extras.keySet().iterator();
      while (it.hasNext()) {
        String key = it.next();
        Object value = extras.get(key);

        Log.d(LOG_TAG, "key = " + key);

        if (jsonKeySet.contains(key)) {
          json.put(key, value);
        } else if (key.equals(COLDSTART)) {
          additionalData.put(key, extras.getBoolean(COLDSTART));
        } else if (key.equals(FOREGROUND)) {
          additionalData.put(key, extras.getBoolean(FOREGROUND));
        } else if (key.equals(DISMISSED)) {
          additionalData.put(key, extras.getBoolean(DISMISSED));
        } else if (value instanceof String) {
          String strValue = (String) value;
          try {
            // Try to figure out if the value is another JSON object
            if (strValue.startsWith("{")) {
              additionalData.put(key, new JSONObject(strValue));
            }
            // Try to figure out if the value is another JSON array
            else if (strValue.startsWith("[")) {
              additionalData.put(key, new JSONArray(strValue));
            } else {
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
    } catch (JSONException e) {
      Log.e(LOG_TAG, "extrasToJSON: JSON exception");
    }
    return null;
  }

  private String getStringResourceByName(String aString) {
    Activity activity = cordova.getActivity();
    String packageName = activity.getPackageName();
    int resId = activity.getResources().getIdentifier(aString, "string", packageName);
    return activity.getString(resId);
  }

  public static boolean isInForeground() {
    return gForeground;
  }

  public static boolean isActive() {
    return gWebView != null;
  }

  protected static void setRegistrationID(String token) {
    registration_id = token;
  }
}
