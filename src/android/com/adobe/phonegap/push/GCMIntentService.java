package com.adobe.phonegap.push;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

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
                String message = this.getMessageText(extras);
                if (message != null && message.length() != 0) {
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

        int requestCode = new Random().nextInt();
        PendingIntent contentIntent = PendingIntent.getActivity(this, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(extras.getString("title"))
                .setTicker(extras.getString("title"))
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        
        SharedPreferences prefs = context.getSharedPreferences("com.adobe.phonegap.push", Context.MODE_PRIVATE);
        String localIcon = prefs.getString("icon", null);
        String localIconColor = prefs.getString("iconColor", null);
        boolean soundOption = prefs.getBoolean("sound", true);
        boolean vibrateOption = prefs.getBoolean("vibrate", true);
        Log.d(LOG_TAG, "stored icon=" + localIcon);
        Log.d(LOG_TAG, "stored iconColor=" + localIconColor);
        Log.d(LOG_TAG, "stored sound=" + soundOption);
        Log.d(LOG_TAG, "stored vibrate=" + vibrateOption);

        /*
         * Notification Vibration
         */
        if (vibrateOption) {
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        }
        
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
         * Notification message
         */
        setNotificationMessage(extras, mBuilder);

        /*
         * Notification count
         */
        setNotificationCount(extras, mBuilder);
        
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

    private void setNotificationCount(Bundle extras, NotificationCompat.Builder mBuilder) {
        String msgcnt = extras.getString("msgcnt");
        if (msgcnt == null) {
            msgcnt = extras.getString("badge");
        }
        if (msgcnt != null) {
            mBuilder.setNumber(Integer.parseInt(msgcnt));
        }
    }

    private void setNotificationMessage(Bundle extras, NotificationCompat.Builder mBuilder) {
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();

        String message = getMessageText(extras);
        if (message != null) {
            mBuilder.setContentText(message);

            bigText.bigText(message);
            bigText.setBigContentTitle(extras.getString("title"));

            String summaryText = extras.getString("summaryText");
            if (summaryText != null) {
                bigText.setSummaryText(summaryText);
            }

            mBuilder.setStyle(bigText);
        } else {
            mBuilder.setContentText("<missing message content>");
        }
    }

    private String getMessageText(Bundle extras) {
        String message = extras.getString("message");
        if (message == null) {
            message = extras.getString("body");
        }
        return message;
    }

    private void setNotificationSound(Context context, Bundle extras, NotificationCompat.Builder mBuilder) {
        String soundname = extras.getString("soundname");
        if (soundname == null) {
            soundname = extras.getString("sound");
        }
        if (soundname != null) {
            Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                        + "://" + context.getPackageName() + "/raw/" + soundname);
            Log.d(LOG_TAG, sound.toString());
            mBuilder.setSound(sound);
        } else {
            mBuilder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
        }
    }

    private void setNotificationLargeIcon(Bundle extras, String packageName, Resources resources, NotificationCompat.Builder mBuilder) {
        String gcmLargeIcon = extras.getString("image"); // from gcm
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
                    largeIconId = resources.getIdentifier(gcmLargeIcon, "drawable", packageName);
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
        String icon = extras.getString("icon");
        if (icon != null) {
            iconId = resources.getIdentifier(icon, "drawable", packageName);
            Log.d(LOG_TAG, "using icon from plugin options");
        }
        else if (localIcon != null) {
            iconId = resources.getIdentifier(localIcon, "drawable", packageName);
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
                Log.e(LOG_TAG, "couldnt parse color from android options");
            }
        }
        else if (localIconColor != null) {
            try {
                iconColor = Color.parseColor(localIconColor);
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "couldnt parse color from android options");
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
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String getAppName(Context context) {
        CharSequence appName =  context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
        return (String)appName;
    }
    
    @Override
    public void onError(Context context, String errorId) {
        Log.e(LOG_TAG, "onError - errorId: " + errorId);
        // if we are in the foreground, just send the error
        if (PushPlugin.isInForeground()) {
            PushPlugin.sendError(errorId);
        }
    }

}