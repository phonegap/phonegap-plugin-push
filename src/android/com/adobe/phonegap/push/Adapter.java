package com.adobe.phonegap.push;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import org.apache.cordova.CordovaActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by tfischbach on 18.04.16.
 */
public interface Adapter {
    void setup(Context context, Activity activity);

    String getOptionsKey();

    String getToken(Context context, String senderID) throws IOException;

    Boolean isSupported(Context context);

    Boolean hasPermission(Context context);

    void subscribe(Context context, JSONArray topics, String token);

    void unsubscribe(Context context, JSONArray topics, String token) throws IOException;

    void deleteToken(Context context) throws IOException;

    Bundle retrieveOfflineMessages();

    void cancelNotifcation(Activity activity);
}
