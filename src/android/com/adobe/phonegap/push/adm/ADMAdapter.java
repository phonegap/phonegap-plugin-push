package com.adobe.phonegap.push.adm;

import android.app.Activity;
import android.content.Context;

import com.adobe.phonegap.push.Adapter;
import com.amazon.device.messaging.ADM;

import android.os.Bundle;
import android.util.Log;
import org.json.JSONArray;

import java.io.IOException;

public class ADMAdapter implements Adapter {
    private ADM adm;
    private Boolean hasPermission = false;
    private Boolean checkedPermission = false;

    public static String TAG = "ADMAdapter";
    private static final String MODEL_FIRST_GEN = "Kindle Fire";

    @Override
    public void setup(Context context, Activity activity) {
        if (!isFirstGenKindleFireDevice() && hasPermission(context)) {
            adm = new ADM(activity);
            ADMMessageHandler.saveConfigOptions(activity);
        }
    }

    private static boolean isFirstGenKindleFireDevice() {
        return android.os.Build.MODEL.equals(MODEL_FIRST_GEN);
    }

    @Override
    public String getOptionsKey() {
        return "android";
    }

    @Override
    public String getToken(Context context, String senderID) throws IOException {
        Log.e(TAG, "about to");
        String regId = adm.getRegistrationId();
        Log.e(TAG, "regId = " + regId);

        if (regId == null) {
            adm.startRegister();
            return null;
        } else {
            return regId;
        }
    }

    @Override
    public Boolean isSupported(Context context) {
        return adm != null && adm.isSupported();
    }

    @Override
    public Boolean hasPermission(Context context) {
        if (!checkedPermission) {
            checkedPermission = true;

            try {
                Class.forName("com.amazon.device.messaging.ADM");
                hasPermission = true;
            } catch (ClassNotFoundException e) {
                // Not permitted by user
            }
        }

        return hasPermission;
    }

    @Override
    public void subscribe(Context context, JSONArray topics, String token) {}

    @Override
    public void unsubscribe(Context context, JSONArray topics, String token) throws IOException {}

    @Override
    public void deleteToken(Context context) throws IOException {
        adm.startUnregister();
    }

    @Override
    public Bundle retrieveOfflineMessages() {
        Bundle result = ADMMessageHandler.getOfflineMessage();
        ADMMessageHandler.cleanupNotificationIntent();
        return result;
    }

    @Override
    public void cancelNotifcation(Activity activity) {
        ADMMessageHandler.cancelNotification(activity);
    }
}
