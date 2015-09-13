package com.adobe.phonegap.push;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;
import static com.google.android.gcm.GCMConstants.DEFAULT_INTENT_SERVICE_CLASS_NAME;

/*
 * Implementation of GCMBroadcastReceiver that hard-wires the intent service to be 
 * com.plugin.gcm.GCMIntentService, instead of your_package.GCMIntentService 
 */
public class CordovaGCMBroadcastReceiver extends GCMBroadcastReceiver implements PushConstants {
	
	@Override
	protected String getGCMIntentServiceClassName(Context context) {
    	return COM_ADOBE_PHONEGAP_PUSH + DEFAULT_INTENT_SERVICE_CLASS_NAME;
    }
	
}