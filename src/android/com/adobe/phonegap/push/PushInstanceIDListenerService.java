package com.adobe.phonegap.push;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;  //ADD FirebaseMessagingService

import org.json.JSONException;

import java.io.IOException;

public class PushInstanceIDListenerService extends FirebaseMessagingService implements PushConstants {
    public static final String LOG_TAG = "Push_InsIdService";


    @Override
    public void onNewToken(String token) { //Added onNewToken method
        // Get updated InstanceID token.
       FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {

            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(LOG_TAG, "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                String refreshedToken = task.getResult().getToken();
                Log.d(LOG_TAG, "Refreshed token: " + refreshedToken);
            }
        });
    }
}
