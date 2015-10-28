package com.adobe.phonegap.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;
import com.google.android.gms.gcm.GcmPubSub;

import org.json.JSONException;

import java.io.IOException;

public class PushInstanceIDListenerService extends InstanceIDListenerService implements PushConstants {
    public static final String LOG_TAG = "PushPlugin_PushInstanceIDListenerService";
    private static final String[] TOPICS = {"questions"};

    public void onTokenRefresh() {
        // re-register
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(COM_ADOBE_PHONEGAP_PUSH, Context.MODE_PRIVATE);
        String senderID = sharedPref.getString(SENDER_ID, "");
        if (!"".equals(senderID)) {
            try {
                String token = InstanceID.getInstance(getApplicationContext()).getToken(senderID, GCM);

                // save new token
                SharedPreferences.Editor editor = sharedPref.edit();
                subscribeTopics(token);
                editor.putString(REGISTRATION_ID, token);
                editor.commit();
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
            }

        }
    }

    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(getApplicationContext());
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}