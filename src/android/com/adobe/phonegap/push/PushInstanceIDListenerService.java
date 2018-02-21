package com.adobe.phonegap.push;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import io.intercom.android.sdk.push.IntercomPushClient;

import org.json.JSONException;

import java.io.IOException;

public class PushInstanceIDListenerService extends FirebaseInstanceIdService implements PushConstants {
    public static final String LOG_TAG = "Push_InsIdService";

    private final IntercomPushClient intercomPushClient = new IntercomPushClient();

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        intercomPushClient.sendTokenToIntercom(getApplication(), refreshedToken);
    }
}
