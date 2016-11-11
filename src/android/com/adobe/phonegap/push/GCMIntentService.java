package com.adobe.phonegap.push;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.app.RemoteInput;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

@SuppressLint("NewApi")
public class GCMIntentService extends GcmListenerService implements PushConstants {

    private static final String LOG_TAG = "PushPlugin_GCMIntentService";
    private static HashMap<Integer, ArrayList<String>> messageMap = new HashMap<Integer, ArrayList<String>>();

    public void setNotification(int notId, String message){
        ArrayList<String> messageList = messageMap.get(notId);
        if(messageList == null) {
            messageList = new ArrayList<String>();
            messageMap.put(notId, messageList);
        }

        if(message.isEmpty()){
            messageList.clear();
        }else{
            messageList.add(message);
        }
    }

    @Override
    public void onMessageReceived(String from, Bundle extras) {
        Log.d(LOG_TAG, "onMessage - from: " + from);

        if (extras != null) {
            Context applicationContext = getApplicationContext();

            SharedPreferences prefs = applicationContext.getSharedPreferences(PushPlugin.COM_ADOBE_PHONEGAP_PUSH, Context.MODE_PRIVATE);
            boolean forceShow = prefs.getBoolean(FORCE_SHOW, false);
            boolean clearBadge = prefs.getBoolean(CLEAR_BADGE, false);

            extras = normalizeExtras(applicationContext, extras);

            if (clearBadge) {
                PushPlugin.setApplicationIconBadgeNumber(getApplicationContext(), 0);
            }

            // if we are in the foreground and forceShow is `false` only send data
            if (!forceShow && PushPlugin.isInForeground()) {
                Log.d(LOG_TAG, "foreground");
                extras.putBoolean(FOREGROUND, true);
                extras.putBoolean(COLDSTART, false);
                PushPlugin.sendExtras(extras);
            }
            // if we are in the foreground and forceShow is `true`, force show the notification if the data has at least a message or title
            else if (forceShow && PushPlugin.isInForeground()) {
                Log.d(LOG_TAG, "foreground force");
                extras.putBoolean(FOREGROUND, true);
                extras.putBoolean(COLDSTART, false);

                showNotificationIfPossible(applicationContext, extras);
            }
            // if we are not in the foreground always send notification if the data has at least a message or title
            else {
                Log.d(LOG_TAG, "background");
                extras.putBoolean(FOREGROUND, false);
                extras.putBoolean(COLDSTART, PushPlugin.isActive());

                showNotificationIfPossible(applicationContext, extras);
            }
        }
    }

    /*
     * Change a values key in the extras bundle
     */
    private void replaceKey(Context context, String oldKey, String newKey, Bundle extras, Bundle newExtras) {
        Object value = extras.get(oldKey);
        if ( value != null ) {
            if (value instanceof String) {
                value = localizeKey(context, newKey, (String) value);

                newExtras.putString(newKey, (String) value);
            } else if (value instanceof Boolean) {
                newExtras.putBoolean(newKey, (Boolean) value);
            } else if (value instanceof Number) {
                newExtras.putDouble(newKey, ((Number) value).doubleValue());
            } else {
                newExtras.putString(newKey, String.valueOf(value));
            }
        }
    }

    /*
     * Normalize localization for key
     */
    private String localizeKey(Context context, String key, String value) {
        if (key.equals(TITLE) || key.equals(MESSAGE) || key.equals(SUMMARY_TEXT)) {
            try {
                JSONObject localeObject = new JSONObject(value);

                String localeKey = localeObject.getString(LOC_KEY);

                ArrayList<String> localeFormatData = new ArrayList<String>();
                if (!localeObject.isNull(LOC_DATA)) {
                    String localeData = localeObject.getString(LOC_DATA);
                    JSONArray localeDataArray = new JSONArray(localeData);
                    for (int i = 0 ; i < localeDataArray.length(); i++) {
                        localeFormatData.add(localeDataArray.getString(i));
                    }
                }

                String packageName = context.getPackageName();
                Resources resources = context.getResources();

                int resourceId = resources.getIdentifier(localeKey, "string", packageName);

                if (resourceId != 0) {
                    return resources.getString(resourceId, localeFormatData.toArray());
                }
                else {
                    Log.d(LOG_TAG, "can't find resource for locale key = " + localeKey);

                    return value;
                }
            }
            catch(JSONException e) {
                Log.d(LOG_TAG, "no locale found for key = " + key + ", error " + e.getMessage());

                return value;
            }
        }

        return value;
    }

    /*
     * Replace alternate keys with our canonical value
     */
    private String normalizeKey(String key) {
        if (key.equals(BODY) || key.equals(ALERT) || key.equals(GCM_NOTIFICATION_BODY) || key.equals(TWILIO_BODY)) {
            return MESSAGE;
        } else if (key.equals(TWILIO_TITLE)) {
            return TITLE;
        }else if (key.equals(MSGCNT) || key.equals(BADGE)) {
            return COUNT;
        } else if (key.equals(SOUNDNAME) || key.equals(TWILIO_SOUND)) {
            return SOUND;
        } else if (key.startsWith(GCM_NOTIFICATION)) {
            return key.substring(GCM_NOTIFICATION.length()+1, key.length());
        } else if (key.startsWith(GCM_N)) {
            return key.substring(GCM_N.length()+1, key.length());
        } else if (key.startsWith(UA_PREFIX)) {
            key = key.substring(UA_PREFIX.length()+1, key.length());
            return key.toLowerCase();
        } else {
            return key;
        }
    }

    /*
     * Parse bundle into normalized keys.
     */
    private Bundle normalizeExtras(Context context, Bundle extras) {
        Log.d(LOG_TAG, "normalize extras");
        Iterator<String> it = extras.keySet().iterator();
        Bundle newExtras = new Bundle();

        while (it.hasNext()) {
            String key = it.next();

            Log.d(LOG_TAG, "key = " + key);

            // If normalizeKeythe key is "data" or "message" and the value is a json object extract
            // This is to support parse.com and other services. Issue #147 and pull #218
            if (key.equals(PARSE_COM_DATA) || key.equals(MESSAGE)) {
                Object json = extras.get(key);
                // Make sure data is json object stringified
                if ( json instanceof String && ((String) json).startsWith("{") ) {
                    Log.d(LOG_TAG, "extracting nested message data from key = " + key);
                    try {
                        // If object contains message keys promote each value to the root of the bundle
                        JSONObject data = new JSONObject((String) json);
                        if ( data.has(ALERT) || data.has(MESSAGE) || data.has(BODY) || data.has(TITLE) ) {
                            Iterator<String> jsonIter = data.keys();
                            while (jsonIter.hasNext()) {
                                String jsonKey = jsonIter.next();

                                Log.d(LOG_TAG, "key = data/" + jsonKey);

                                String value = data.getString(jsonKey);
                                jsonKey = normalizeKey(jsonKey);
                                value = localizeKey(context, jsonKey, value);

                                newExtras.putString(jsonKey, value);
                            }
                        }
                    } catch( JSONException e) {
                        Log.e(LOG_TAG, "normalizeExtras: JSON exception");
                    }
                }
            } else if (key.equals(("notification"))) {
                Bundle value = extras.getBundle(key);
                Iterator<String> iterator = value.keySet().iterator();
                while (iterator.hasNext()) {
                    String notifkey = iterator.next();

                    Log.d(LOG_TAG, "notifkey = " + notifkey);
                    String newKey = normalizeKey(notifkey);
                    Log.d(LOG_TAG, "replace key " + notifkey + " with " + newKey);

                    String valueData = value.getString(notifkey);
                    valueData = localizeKey(context, newKey, valueData);

                    newExtras.putString(newKey, valueData);
                }
                continue;
            }

            String newKey = normalizeKey(key);
            Log.d(LOG_TAG, "replace key " + key + " with " + newKey);
            replaceKey(context, key, newKey, extras, newExtras);

        } // while

        return newExtras;
    }

    private int extractBadgeCount(Bundle extras) {
        int count = -1;
        String msgcnt = extras.getString(COUNT);

        try {
            if (msgcnt != null) {
                count = Integer.parseInt(msgcnt);
            }
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage(), e);
        }

        return count;
    }

    private void showNotificationIfPossible (Context context, Bundle extras) {

        // Send a notification if there is a message or title, otherwise just send data
        String message = extras.getString(MESSAGE);
        String title = extras.getString(TITLE);
        String contentAvailable = extras.getString(CONTENT_AVAILABLE);
        String forceStart = extras.getString(FORCE_START);
        int badgeCount = extractBadgeCount(extras);
        if (badgeCount >= 0) {
            Log.d(LOG_TAG, "count =[" + badgeCount + "]");
            PushPlugin.setApplicationIconBadgeNumber(context, badgeCount);
        }

        Log.d(LOG_TAG, "message =[" + message + "]");
        Log.d(LOG_TAG, "title =[" + title + "]");
        Log.d(LOG_TAG, "contentAvailable =[" + contentAvailable + "]");
        Log.d(LOG_TAG, "forceStart =[" + forceStart + "]");

        if ((message != null && message.length() != 0) ||
                (title != null && title.length() != 0)) {

            Log.d(LOG_TAG, "create notification");

            if(title == null || title.isEmpty()){
                extras.putString(TITLE, getAppName(this));
            }

            createNotification(context, extras);
        }

		if(!PushPlugin.isActive() && "1".equals(forceStart)){
            Log.d(LOG_TAG, "app is not running but we should start it and put in background");
			Intent intent = new Intent(this, PushHandlerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PUSH_BUNDLE, extras);
			intent.putExtra(START_IN_BACKGROUND, true);
            intent.putExtra(FOREGROUND, false);
            startActivity(intent);
		} else if ("1".equals(contentAvailable)) {
            Log.d(LOG_TAG, "app is not running and content available true");
            Log.d(LOG_TAG, "send notification event");
            PushPlugin.sendExtras(extras);
        }
    }

    public void createNotification(Context context, Bundle extras) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String appName = getAppName(this);
        String packageName = context.getPackageName();
        Resources resources = context.getResources();

        int notId = parseInt(NOT_ID, extras);
        Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra(PUSH_BUNDLE, extras);
        notificationIntent.putExtra(NOT_ID, notId);

        int requestCode = new Random().nextInt();
        PendingIntent contentIntent = PendingIntent.getActivity(this, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(fromHtml(extras.getString(TITLE)))
                        .setTicker(fromHtml(extras.getString(TITLE)))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);

        SharedPreferences prefs = context.getSharedPreferences(PushPlugin.COM_ADOBE_PHONEGAP_PUSH, Context.MODE_PRIVATE);
        String localIcon = prefs.getString(ICON, null);
        String localIconColor = prefs.getString(ICON_COLOR, null);
        boolean soundOption = prefs.getBoolean(SOUND, true);
        boolean vibrateOption = prefs.getBoolean(VIBRATE, true);
        Log.d(LOG_TAG, "stored icon=" + localIcon);
        Log.d(LOG_TAG, "stored iconColor=" + localIconColor);
        Log.d(LOG_TAG, "stored sound=" + soundOption);
        Log.d(LOG_TAG, "stored vibrate=" + vibrateOption);

        /*
         * Notification Vibration
         */

        setNotificationVibration(extras, vibrateOption, mBuilder);

        /*
         * Notification Icon Color
         *
         * Sets the small-icon background color of the notification.
         * To use, add the `iconColor` key to plugin android options
         *
         */
        setNotificationIconColor(extras.getString("color"), mBuilder, localIconColor);

        /*
         * Notification Icon
         *
         * Sets the small-icon of the notification.
         *
         * - checks the plugin options for `icon` key
         * - if none, uses the application icon
         *
         * The icon value must be a string that maps to a drawable resource.
         * If no resource is found, falls
         *
         */
        setNotificationSmallIcon(context, extras, packageName, resources, mBuilder, localIcon);

        /*
         * Notification Large-Icon
         *
         * Sets the large-icon of the notification
         *
         * - checks the gcm data for the `image` key
         * - checks to see if remote image, loads it.
         * - checks to see if assets image, Loads It.
         * - checks to see if resource image, LOADS IT!
         * - if none, we don't set the large icon
         *
         */
        setNotificationLargeIcon(extras, packageName, resources, mBuilder);

        /*
         * Notification Sound
         */
        if (soundOption) {
            setNotificationSound(context, extras, mBuilder);
        }

        /*
         *  LED Notification
         */
        setNotificationLedColor(extras, mBuilder);

        /*
         *  Priority Notification
         */
        setNotificationPriority(extras, mBuilder);

        /*
         * Notification message
         */
        setNotificationMessage(notId, extras, mBuilder);

        /*
         * Notification count
         */
        setNotificationCount(context, extras, mBuilder);

        /*
         * Notification count
         */
        setVisibility(context, extras, mBuilder);

        /*
         * Notification add actions
         */
        createActions(extras, mBuilder, resources, packageName, notId);

        mNotificationManager.notify(appName, notId, mBuilder.build());
    }

    private void updateIntent(Intent intent, String callback, Bundle extras, boolean foreground, int notId) {
        intent.putExtra(CALLBACK, callback);
        intent.putExtra(PUSH_BUNDLE, extras);
        intent.putExtra(FOREGROUND, foreground);
        intent.putExtra(NOT_ID, notId);
    }

    private void createActions(Bundle extras, NotificationCompat.Builder mBuilder, Resources resources, String packageName, int notId) {
        Log.d(LOG_TAG, "create actions: with in-line");
        String actions = extras.getString(ACTIONS);
        if (actions != null) {
            try {
                JSONArray actionsArray = new JSONArray(actions);
                ArrayList<NotificationCompat.Action> wActions = new ArrayList<NotificationCompat.Action>();
                for (int i=0; i < actionsArray.length(); i++) {
                    int min = 1;
                    int max = 2000000000;
                    Random random = new Random();
                    int uniquePendingIntentRequestCode = random.nextInt((max - min) + 1) + min;
                    Log.d(LOG_TAG, "adding action");
                    JSONObject action = actionsArray.getJSONObject(i);
                    Log.d(LOG_TAG, "adding callback = " + action.getString(CALLBACK));
                    boolean foreground = action.optBoolean(FOREGROUND, true);
                    boolean inline = action.optBoolean("inline", false);
                    Intent intent = null;
                    PendingIntent pIntent = null;
                    if (inline) {
                        Log.d(LOG_TAG, "Version: " + android.os.Build.VERSION.SDK_INT + " = " + android.os.Build.VERSION_CODES.M);
                        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.M) {
                            Log.d(LOG_TAG, "push activity");
                            intent = new Intent(this, PushHandlerActivity.class);
                        } else {
                            Log.d(LOG_TAG, "push receiver");
                            intent = new Intent(this, BackgroundActionButtonHandler.class);
                        }

                        updateIntent(intent, action.getString(CALLBACK), extras, foreground, notId);

                        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.M) {
                            Log.d(LOG_TAG, "push activity for notId " + notId);
                            pIntent = PendingIntent.getActivity(this, uniquePendingIntentRequestCode, intent, PendingIntent.FLAG_ONE_SHOT);
                        } else {
                            Log.d(LOG_TAG, "push receiver for notId " + notId);
                            pIntent = PendingIntent.getBroadcast(this, uniquePendingIntentRequestCode, intent, PendingIntent.FLAG_ONE_SHOT);
                        }
                    } else if (foreground) {
                        intent = new Intent(this, PushHandlerActivity.class);
                        updateIntent(intent, action.getString(CALLBACK), extras, foreground, notId);
                        pIntent = PendingIntent.getActivity(this, uniquePendingIntentRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    } else {
                        intent = new Intent(this, BackgroundActionButtonHandler.class);
                        updateIntent(intent, action.getString(CALLBACK), extras, foreground, notId);
                        pIntent = PendingIntent.getBroadcast(this, uniquePendingIntentRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    NotificationCompat.Action.Builder actionBuilder =
                        new NotificationCompat.Action.Builder(resources.getIdentifier(action.optString(ICON, ""), DRAWABLE, packageName),
                            action.getString(TITLE), pIntent);

                    RemoteInput remoteInput = null;
                    if (inline) {
                        Log.d(LOG_TAG, "create remote input");
                        String replyLabel = "Enter your reply here";
                        remoteInput =
                                new RemoteInput.Builder(INLINE_REPLY)
                                .setLabel(replyLabel)
                                .build();
                        actionBuilder.addRemoteInput(remoteInput);
                    }

                    NotificationCompat.Action wAction = actionBuilder.build();
                    wActions.add(actionBuilder.build());

                    if (inline) {
                        mBuilder.addAction(wAction);
                    } else {
                        mBuilder.addAction(resources.getIdentifier(action.optString(ICON, ""), DRAWABLE, packageName),
                                action.getString(TITLE), pIntent);
                    }
                    wAction = null;
                    pIntent = null;
                }
                mBuilder.extend(new WearableExtender().addActions(wActions));
                wActions.clear();
            } catch(JSONException e) {
                // nope
            }
        }
    }

    private void setNotificationCount(Context context, Bundle extras, NotificationCompat.Builder mBuilder) {
        int count = extractBadgeCount(extras);
        if (count >= 0) {
            Log.d(LOG_TAG, "count =[" + count + "]");
            mBuilder.setNumber(count);
        }
    }


    private void setVisibility(Context context, Bundle extras, NotificationCompat.Builder mBuilder) {
        String visibilityStr = extras.getString(VISIBILITY);
        if (visibilityStr != null) {
            try {
                Integer visibility = Integer.parseInt(visibilityStr);
                if (visibility >= NotificationCompat.VISIBILITY_SECRET && visibility <= NotificationCompat.VISIBILITY_PUBLIC) {
                    mBuilder.setVisibility(visibility);
                } else {
                    Log.e(LOG_TAG, "Visibility parameter must be between -1 and 1");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void setNotificationVibration(Bundle extras, Boolean vibrateOption, NotificationCompat.Builder mBuilder) {
        String vibrationPattern = extras.getString(VIBRATION_PATTERN);
        if (vibrationPattern != null) {
            String[] items = vibrationPattern.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
            long[] results = new long[items.length];
            for (int i = 0; i < items.length; i++) {
                try {
                    results[i] = Long.parseLong(items[i].trim());
                } catch (NumberFormatException nfe) {}
            }
            mBuilder.setVibrate(results);
        } else {
            if (vibrateOption) {
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            }
        }
    }

    private void setNotificationMessage(int notId, Bundle extras, NotificationCompat.Builder mBuilder) {
        String message = extras.getString(MESSAGE);

        String style = extras.getString(STYLE, STYLE_TEXT);
        if(STYLE_INBOX.equals(style)) {
            setNotification(notId, message);

            mBuilder.setContentText(fromHtml(message));

            ArrayList<String> messageList = messageMap.get(notId);
            Integer sizeList = messageList.size();
            if (sizeList > 1) {
                String sizeListMessage = sizeList.toString();
                String stacking = sizeList + " more";
                if (extras.getString(SUMMARY_TEXT) != null) {
                    stacking = extras.getString(SUMMARY_TEXT);
                    stacking = stacking.replace("%n%", sizeListMessage);
                }
                NotificationCompat.InboxStyle notificationInbox = new NotificationCompat.InboxStyle()
                        .setBigContentTitle(fromHtml(extras.getString(TITLE)))
                        .setSummaryText(fromHtml(stacking));

                for (int i = messageList.size() - 1; i >= 0; i--) {
                    notificationInbox.addLine(fromHtml(messageList.get(i)));
                }

                mBuilder.setStyle(notificationInbox);
            } else {
                NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                if (message != null) {
                    bigText.bigText(fromHtml(message));
                    bigText.setBigContentTitle(fromHtml(extras.getString(TITLE)));
                    mBuilder.setStyle(bigText);
                }
            }
        } else if (STYLE_PICTURE.equals(style)) {
            setNotification(notId, "");

            NotificationCompat.BigPictureStyle bigPicture = new NotificationCompat.BigPictureStyle();
            bigPicture.bigPicture(getBitmapFromURL(extras.getString(PICTURE)));
            bigPicture.setBigContentTitle(fromHtml(extras.getString(TITLE)));
            bigPicture.setSummaryText(fromHtml(extras.getString(SUMMARY_TEXT)));

            mBuilder.setContentTitle(fromHtml(extras.getString(TITLE)));
            mBuilder.setContentText(fromHtml(message));

            mBuilder.setStyle(bigPicture);
        } else {
            setNotification(notId, "");

            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();

            if (message != null) {
                mBuilder.setContentText(fromHtml(message));

                bigText.bigText(fromHtml(message));
                bigText.setBigContentTitle(fromHtml(extras.getString(TITLE)));

                String summaryText = extras.getString(SUMMARY_TEXT);
                if (summaryText != null) {
                    bigText.setSummaryText(fromHtml(summaryText));
                }

                mBuilder.setStyle(bigText);
            }
            /*
            else {
                mBuilder.setContentText("<missing message content>");
            }
            */
        }
    }

    private void setNotificationSound(Context context, Bundle extras, NotificationCompat.Builder mBuilder) {
        String soundname = extras.getString(SOUNDNAME);
        if (soundname == null) {
            soundname = extras.getString(SOUND);
        }
        if (SOUND_RINGTONE.equals(soundname)) {
            mBuilder.setSound(android.provider.Settings.System.DEFAULT_RINGTONE_URI);
        } else if (soundname != null && !soundname.contentEquals(SOUND_DEFAULT)) {
            Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + context.getPackageName() + "/raw/" + soundname);
            Log.d(LOG_TAG, sound.toString());
            mBuilder.setSound(sound);
        } else {
            mBuilder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
        }
    }

    private void setNotificationLedColor(Bundle extras, NotificationCompat.Builder mBuilder) {
        String ledColor = extras.getString(LED_COLOR);
        if (ledColor != null) {
            // Converts parse Int Array from ledColor
            String[] items = ledColor.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
            int[] results = new int[items.length];
            for (int i = 0; i < items.length; i++) {
                try {
                    results[i] = Integer.parseInt(items[i].trim());
                } catch (NumberFormatException nfe) {}
            }
            if (results.length == 4) {
                mBuilder.setLights(Color.argb(results[0], results[1], results[2], results[3]), 500, 500);
            } else {
                Log.e(LOG_TAG, "ledColor parameter must be an array of length == 4 (ARGB)");
            }
        }
    }

    private void setNotificationPriority(Bundle extras, NotificationCompat.Builder mBuilder) {
        String priorityStr = extras.getString(PRIORITY);
        if (priorityStr != null) {
            try {
                Integer priority = Integer.parseInt(priorityStr);
                if (priority >= NotificationCompat.PRIORITY_MIN && priority <= NotificationCompat.PRIORITY_MAX) {
                    mBuilder.setPriority(priority);
                } else {
                    Log.e(LOG_TAG, "Priority parameter must be between -2 and 2");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void setNotificationLargeIcon(Bundle extras, String packageName, Resources resources, NotificationCompat.Builder mBuilder) {
        String gcmLargeIcon = extras.getString(IMAGE); // from gcm
        if (gcmLargeIcon != null && !"".equals(gcmLargeIcon)) {
            if (gcmLargeIcon.startsWith("http://") || gcmLargeIcon.startsWith("https://")) {
                mBuilder.setLargeIcon(getBitmapFromURL(gcmLargeIcon));
                Log.d(LOG_TAG, "using remote large-icon from gcm");
            } else {
                AssetManager assetManager = getAssets();
                InputStream istr;
                try {
                    istr = assetManager.open(gcmLargeIcon);
                    Bitmap bitmap = BitmapFactory.decodeStream(istr);
                    mBuilder.setLargeIcon(bitmap);
                    Log.d(LOG_TAG, "using assets large-icon from gcm");
                } catch (IOException e) {
                    int largeIconId = 0;
                    largeIconId = resources.getIdentifier(gcmLargeIcon, DRAWABLE, packageName);
                    if (largeIconId != 0) {
                        Bitmap largeIconBitmap = BitmapFactory.decodeResource(resources, largeIconId);
                        mBuilder.setLargeIcon(largeIconBitmap);
                        Log.d(LOG_TAG, "using resources large-icon from gcm");
                    } else {
                        Log.d(LOG_TAG, "Not setting large icon");
                    }
                }
            }
        }
    }

    private void setNotificationSmallIcon(Context context, Bundle extras, String packageName, Resources resources, NotificationCompat.Builder mBuilder, String localIcon) {
        int iconId = 0;
        String icon = extras.getString(ICON);
        if (icon != null && !"".equals(icon)) {
            iconId = resources.getIdentifier(icon, DRAWABLE, packageName);
            Log.d(LOG_TAG, "using icon from plugin options");
        }
        else if (localIcon != null && !"".equals(localIcon)) {
            iconId = resources.getIdentifier(localIcon, DRAWABLE, packageName);
            Log.d(LOG_TAG, "using icon from plugin options");
        }
        if (iconId == 0) {
            Log.d(LOG_TAG, "no icon resource found - using application icon");
            iconId = context.getApplicationInfo().icon;
        }
        mBuilder.setSmallIcon(iconId);
    }

    private void setNotificationIconColor(String color, NotificationCompat.Builder mBuilder, String localIconColor) {
        int iconColor = 0;
        if (color != null && !"".equals(color)) {
            try {
                iconColor = Color.parseColor(color);
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "couldn't parse color from android options");
            }
        }
        else if (localIconColor != null && !"".equals(localIconColor)) {
            try {
                iconColor = Color.parseColor(localIconColor);
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "couldn't parse color from android options");
            }
        }
        if (iconColor != 0) {
            mBuilder.setColor(iconColor);
        }
    }

    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getAppName(Context context) {
        CharSequence appName =  context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
        return (String)appName;
    }

    private int parseInt(String value, Bundle extras) {
        int retval = 0;

        try {
            retval = Integer.parseInt(extras.getString(value));
        }
        catch(NumberFormatException e) {
            Log.e(LOG_TAG, "Number format exception - Error parsing " + value + ": " + e.getMessage());
        }
        catch(Exception e) {
            Log.e(LOG_TAG, "Number format exception - Error parsing " + value + ": " + e.getMessage());
        }

        return retval;
    }

    private Spanned fromHtml(String source) {
        if (source != null)
            return Html.fromHtml(source);
        else
            return null;
    }
}
