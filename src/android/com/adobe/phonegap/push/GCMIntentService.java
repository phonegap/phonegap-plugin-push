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
import android.text.Html;
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

            SharedPreferences prefs = getApplicationContext().getSharedPreferences(PushPlugin.COM_ADOBE_PHONEGAP_PUSH, Context.MODE_PRIVATE);
            boolean forceShow = prefs.getBoolean(FORCE_SHOW, false);

            // if we are in the foreground and forceShow is `false` only send data
            if (!forceShow && PushPlugin.isInForeground()) {
                extras.putBoolean(FOREGROUND, true);
                PushPlugin.sendExtras(extras);
            }
            // if we are in the foreground and forceShow is `true`, force show the notification if the data has at least a message or title
            else if (forceShow && PushPlugin.isInForeground()) {
                extras.putBoolean(FOREGROUND, true);

                showNotificationIfPossible(getApplicationContext(), extras);
            }
            // if we are not in the foreground always send notification if the data has at least a message or title
            else {
                extras.putBoolean(FOREGROUND, false);

                showNotificationIfPossible(getApplicationContext(), extras);
            }
        }
    }

    private void showNotificationIfPossible (Context context, Bundle extras) {

        // Send a notification if there is a message or title, otherwise just send data
        String message = this.getMessageText(extras);
        String title = getString(extras, TITLE, "");
        if ((message != null && message.length() != 0) ||
                (title != null && title.length() != 0)) {
            createNotification(context, extras);
        } else {
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

        String title = getString(extras, TITLE);
        try {
            if (title == null) {
                JSONObject json = new JSONObject(getString(extras, MESSAGE));
                title = json.getString(TITLE);
            }
        } catch (JSONException e) {}

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(title)
                        .setTicker(title)
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
        setNotificationIconColor(getString(extras,"color"), mBuilder, localIconColor);

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
        setNotificationCount(extras, mBuilder);

        /*
         * Notification add actions
         */
        createActions(extras, mBuilder, resources, packageName);

        mNotificationManager.notify(appName, notId, mBuilder.build());
    }

    private void createActions(Bundle extras, NotificationCompat.Builder mBuilder, Resources resources, String packageName) {
        Log.d(LOG_TAG, "create actions");
        String actions = getString(extras, ACTIONS);
        if (actions != null) {
            try {
                JSONArray actionsArray = new JSONArray(actions);
                for (int i=0; i < actionsArray.length(); i++) {
                    Log.d(LOG_TAG, "adding action");
                    JSONObject action = actionsArray.getJSONObject(i);
                    Log.d(LOG_TAG, "adding callback = " + action.getString(CALLBACK));
                    Intent intent = new Intent(this, PushHandlerActivity.class);
                    intent.putExtra(CALLBACK, action.getString(CALLBACK));
                    intent.putExtra(PUSH_BUNDLE, extras);
                    PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    mBuilder.addAction(resources.getIdentifier(action.getString(ICON), DRAWABLE, packageName),
                            action.getString(TITLE), pIntent);
                }
            } catch(JSONException e) {
                // nope
            }
        }
    }

    private void setNotificationCount(Bundle extras, NotificationCompat.Builder mBuilder) {
        String msgcnt = getString(extras, MSGCNT);
        if (msgcnt == null) {
            msgcnt = getString(extras, BADGE);
        }

        try {
            if (msgcnt == null) {
                JSONObject json = new JSONObject(getString(extras, MESSAGE));
                msgcnt = json.getString(MSGCNT);

                if (msgcnt == null) {
                    msgcnt = json.getString(BADGE);
                }
            }
        } catch (JSONException e) {}

        if (msgcnt != null) {
            mBuilder.setNumber(Integer.parseInt(msgcnt));
        }
    }

    private void setNotificationVibration(Bundle extras, Boolean vibrateOption, NotificationCompat.Builder mBuilder) {
        String vibrationPattern = getString(extras, VIBRATION_PATTERN);

        try {
            if (vibrationPattern == null) {
                JSONObject json = new JSONObject(getString(extras, MESSAGE));
                vibrationPattern = json.getString(VIBRATION_PATTERN);
            }
        } catch (JSONException e) {}

        if (vibrationPattern != null) {
            String[] items = vibrationPattern.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
            long[] results = new long[items.length];
            for (int i = 0; i < items.length; i++) {
                try {
                    results[i] = Long.parseLong(items[i]);
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
        String message = getMessageText(extras);
        String style = getString(extras, STYLE);
        String summaryText = getString(extras, SUMMARY_TEXT);
        String title = getString(extras, TITLE);
        String picture = getString(extras, PICTURE);

        try {
            JSONObject json = new JSONObject(getString(extras, MESSAGE));

            if (style == null) {
                style = json.getString(STYLE);
            }

            if (summaryText == null) {
                summaryText = json.getString(SUMMARY_TEXT);
            }

            if (title == null) {
                title = json.getString(TITLE);
            }

            if (picture == null) {
                picture = json.getString(PICTURE);
            }
        } catch (JSONException e) {}

        if (style == null) {
            style = STYLE_INBOX;
        }

        if(STYLE_INBOX.equals(style)) {
            setNotification(notId, message);
            mBuilder.setContentText(message);

            ArrayList<String> messageList = messageMap.get(notId);
            Integer sizeList = messageList.size();
            if (sizeList > 1) {
                String sizeListMessage = sizeList.toString();
                String stacking = sizeList + " more";

                if (summaryText != null) {
                    stacking = summaryText;
                    stacking = stacking.replace("%n%", sizeListMessage);
                }

                NotificationCompat.InboxStyle notificationInbox = new NotificationCompat.InboxStyle()
                        .setBigContentTitle(title)
                        .setSummaryText(stacking);

                for (int i = messageList.size() - 1; i >= 0; i--) {
                    notificationInbox.addLine(Html.fromHtml(messageList.get(i)));
                }

                mBuilder.setStyle(notificationInbox);
            } else {
                NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                if (message != null) {
                    bigText.bigText(message);
                    bigText.setBigContentTitle(title);
                    mBuilder.setStyle(bigText);
                }
            }
        } else if (STYLE_PICTURE.equals(style)) {
            setNotification(notId, "");

            NotificationCompat.BigPictureStyle bigPicture = new NotificationCompat.BigPictureStyle();
            bigPicture.bigPicture(getBitmapFromURL(picture));
            bigPicture.setBigContentTitle(title);
            bigPicture.setSummaryText(summaryText);

            mBuilder.setContentTitle(title);
            mBuilder.setContentText(message);

            mBuilder.setStyle(bigPicture);
        } else {
            setNotification(notId, "");

            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();

            if (message != null) {
                mBuilder.setContentText(Html.fromHtml(message));

                bigText.bigText(message);
                bigText.setBigContentTitle(title);

                if (summaryText != null) {
                    bigText.setSummaryText(summaryText);
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

    private String getString(Bundle extras,String key) {
        String message = extras.getString(key);
        if (message == null) {
            message = extras.getString(GCM_NOTIFICATION+"."+key);
        }
        return message;
    }

    private String getString(Bundle extras,String key, String defaultString) {
        String message = extras.getString(key);
        if (message == null) {
            message = extras.getString(GCM_NOTIFICATION+"."+key, defaultString);
        }
        return message;
    }

    private String getMessageText(Bundle extras) {
        String message = getString(extras, MESSAGE);
        if (message == null) {
            message = getString(extras, BODY);
        }

        if (message != null) {
            try {
                // Try to figure out if the value is another JSON object
                if (message.startsWith("{")) {
                    JSONObject json = new JSONObject(message);
                    message = null;
                    message = json.getString(MESSAGE);

                    if (message == null) {
                        message = json.getString(BODY);
                    }
                }
            } catch (JSONException e) {}
        }

        return message;
    }

    private void setNotificationSound(Context context, Bundle extras, NotificationCompat.Builder mBuilder) {
        String soundname = getString(extras, SOUNDNAME);
        if (soundname == null) {
            soundname = getString(extras, SOUND);
        }

        try {
            if (soundname == null) {
                JSONObject json = new JSONObject(getString(extras, MESSAGE));
                soundname = json.getString(SOUNDNAME);

                if (soundname == null) {
                    soundname = json.getString(SOUND);
                }
            }
        } catch (JSONException e) {}

        if (soundname != null) {
            Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + context.getPackageName() + "/raw/" + soundname);
            Log.d(LOG_TAG, sound.toString());
            mBuilder.setSound(sound);
        } else {
            mBuilder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
        }
    }

    private void setNotificationLedColor(Bundle extras, NotificationCompat.Builder mBuilder) {
        String ledColor = getString(extras, LED_COLOR);

        try {
            if (ledColor == null) {
                JSONObject json = new JSONObject(getString(extras, MESSAGE));
                ledColor = json.getString(LED_COLOR);
            }
        } catch (JSONException e) {}

        if (ledColor != null) {
            // Converts parse Int Array from ledColor
            String[] items = ledColor.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
            int[] results = new int[items.length];
            for (int i = 0; i < items.length; i++) {
                try {
                    results[i] = Integer.parseInt(items[i]);
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
        String priorityStr = getString(extras, PRIORITY);

        try {
            if (priorityStr == null) {
                JSONObject json = new JSONObject(getString(extras, MESSAGE));
                priorityStr = json.getString(PRIORITY);
            }
        } catch (JSONException e) {}

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
        String gcmLargeIcon = getString(extras, IMAGE); // from gcm

        try {
            if (gcmLargeIcon == null) {
                JSONObject json = new JSONObject(getString(extras, MESSAGE));
                gcmLargeIcon = json.getString(IMAGE);
            }
        } catch (JSONException e) {}

        if (gcmLargeIcon != null) {
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
        String icon = getString(extras, ICON);

        try {
            if (icon == null) {
                JSONObject json = new JSONObject(getString(extras, MESSAGE));
                icon = json.getString(ICON);
            }
        } catch (JSONException e) {}

        if (icon != null) {
            iconId = resources.getIdentifier(icon, DRAWABLE, packageName);
            Log.d(LOG_TAG, "using icon from plugin options");
        }
        else if (localIcon != null) {
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
        if (color != null) {
            try {
                iconColor = Color.parseColor(color);
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "couldn't parse color from android options");
            }
        }
        else if (localIconColor != null) {
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

    private static String getAppName(Context context) {
        CharSequence appName =  context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
        return (String)appName;
    }

    private int parseInt(String value, Bundle extras) {
        int retval = 0;

        try {
            retval = Integer.parseInt(getString(extras, value));
        }
        catch(NumberFormatException e) {
            Log.e(LOG_TAG, "Number format exception - Error parsing " + value + ": " + e.getMessage());
        }
        catch(Exception e) {
            Log.e(LOG_TAG, "Number format exception - Error parsing " + value + ": " + e.getMessage());
        }

        return retval;
    }
}
