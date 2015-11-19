/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adobe.phonegap.push;

import android.content.Context;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

public class RegistrationIntentService extends IntentService implements PushConstants {
    public static final String LOG_TAG = "PushPlugin_RegistrationIntentService";

    public RegistrationIntentService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(COM_ADOBE_PHONEGAP_PUSH, Context.MODE_PRIVATE);

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String senderID = sharedPreferences.getString(SENDER_ID, "");
            String token = instanceID.getToken(senderID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(LOG_TAG, "new GCM Registration Token: " + token);

            // save new token
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(REGISTRATION_ID, token);
            editor.commit();

        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to complete token refresh", e);
        }
    }
}