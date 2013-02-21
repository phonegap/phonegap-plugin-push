package com.plugin.GCM;


//import java.io.*;
//import java.util.*;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.PluginResult.Status;
import com.google.android.gcm.*;


/**
 * @author awysocki
 *
 */

public class PushPlugin extends Plugin {

  public static final String ME="PushPlugin";

  public static final String REGISTER="register";
  public static final String UNREGISTER="unregister";

  public static Plugin gwebView;
  private static String gECB;
  private static String gSenderID;

  @SuppressWarnings("deprecation")
@Override
  public PluginResult execute(String action, JSONArray data, String callbackId)
  {

    PluginResult result = null;

    Log.v(ME + ":execute", "action=" + action);

    if (REGISTER.equals(action)) {

      Log.v(ME + ":execute", "data=" + data.toString());

      try {

        JSONObject jo= new JSONObject(data.toString().substring(1, data.toString().length()-1));

        gwebView = this;

        Log.v(ME + ":execute", "jo=" + jo.toString());

        gECB = (String)jo.get("ecb");
        gSenderID = (String)jo.get("senderID");

        Log.v(ME + ":execute", "ECB="+gECB+" senderID="+gSenderID );

        GCMRegistrar.register(this.ctx.getContext(), gSenderID);


        Log.v(ME + ":execute", "GCMRegistrar.register called ");

        result = new PluginResult(Status.OK);
      }
      catch (JSONException e) {
        Log.e(ME, "Got JSON Exception "
          + e.getMessage());
        result = new PluginResult(Status.JSON_EXCEPTION);
      }
    }
    else if (UNREGISTER.equals(action)) {

      GCMRegistrar.unregister(this.ctx.getContext());
      GCMRegistrar.onDestroy(this.ctx.getContext());
      
      Log.v(ME + ":" + UNREGISTER, "GCMRegistrar.unregister called ");
      result = new PluginResult(Status.OK);
    }
    else
    {
      result = new PluginResult(Status.INVALID_ACTION);
      Log.e(ME, "Invalid action : "+action);
    }

    return result;
  }


  public static void sendJavascript( JSONObject _json )
  {
    String _d =  "javascript:"+gECB+"(" + _json.toString() + ")";
        Log.v(ME + ":sendJavascript", _d);

        if (gECB != null ) {
          gwebView.sendJavascript( _d );
        }
  }


  /**
   * Gets the Directory listing for file, in JSON format
   * @param file The file for which we want to do directory listing
   * @return JSONObject representation of directory list. e.g {"filename":"/sdcard","isdir":true,"children":[{"filename":"a.txt","isdir":false},{..}]}
   * @throws JSONException
   */


}
