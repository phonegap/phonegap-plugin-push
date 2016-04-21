package com.adobe.phonegap.push.gcm;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.adobe.phonegap.push.Adapter;
import com.adobe.phonegap.push.PermissionUtils;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONArray;

import java.io.IOException;

public class GCMAdapter implements Adapter{
    public static final String LOG_TAG = "GCMAdapter";

    static final String GCM = "GCM";
    static final String ANDROID = "android";

    @Override
    public void setup(Context context, Activity activity) {}

    @Override
    public String getOptionsKey() {
        return ANDROID;
    }

    @Override
    public String getToken(Context context, String senderID) throws IOException {
        return InstanceID.getInstance(context).getToken(senderID, GCM);
    }

    @Override
    public Boolean isSupported(Context context) {
        return true;
    }

    @Override
    public Boolean hasPermission(Context context) {
        return PermissionUtils.hasPermission(context, "OP_POST_NOTIFICATION");
    }

    @Override
    public void subscribe(Context context, JSONArray topics, String token) {
        if (topics != null) {
            String topic = null;
            for (int i=0; i<topics.length(); i++) {
                try {
                    topic = topics.optString(i, null);
                    if (topic != null) {
                        Log.d(LOG_TAG, "Subscribing to topic: " + topic);
                        GcmPubSub.getInstance(context).subscribe(token, "/topics/" + topic, null);
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Failed to subscribe to topic: " + topic, e);
                }
            }
        }
    }

    @Override
    public void unsubscribe(Context context, JSONArray topics, String token) throws IOException {
        if (topics != null) {
            String topic = null;
            for (int i=0; i<topics.length(); i++) {
                try {
                    topic = topics.optString(i, null);
                    if (topic != null) {
                        Log.d(LOG_TAG, "Unsubscribing to topic: " + topic);
                        GcmPubSub.getInstance(context).unsubscribe(token, "/topics/" + topic);
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Failed to unsubscribe to topic: " + topic, e);
                }
            }
        }
    }

    @Override
    public void deleteToken(Context context) throws IOException {
        InstanceID.getInstance(context).deleteInstanceID();
    }

    @Override
    public Bundle retrieveOfflineMessages() {
        return null;
    }

    @Override
    public void cancelNotifcation(Activity activity) {}
}
