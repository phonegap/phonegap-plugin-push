# Cordova Push Notifications Plugin for Android and iOS

---

## DESCRIPTION

This plugin is for use with [Cordova](http://incubator.apache.org/cordova/), and allows your application to receive push notifications on both Android and iOS devices. The Android implementation uses [Google's GCM (Google Cloud Messaging) service](http://developer.android.com/guide/google/gcm/index.html), whereas the iOS version is based on [Apple APNS Notifications](http://developer.apple.com/library/mac/#documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/ApplePushService/ApplePushService.html)

**Important** - Push notifications are intended for real devices. The registration process will fail on the iOS simulator. Notifications can be made to work on the Android Emulator. However, doing so requires installation of some helper libraries, as outlined [here,](http://www.androidhive.info/2012/10/android-push-notifications-using-google-cloud-messaging-gcm-php-and-mysql/) under the section titled "Installing helper libraries and setting up the Emulator".

## LICENSE

	The MIT License
	
	Copyright (c) 2012 Adobe Systems, inc.
	portions Copyright (c) 2012 Olivier Louvignes
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.

## Manual Installation for Android


1) copy the contents of **src/android/com/** to your project's **src/com/** folder. The final hirearchy will likely look something like this;

	{project_folder}
		src
			com
				google
					android
						gcm
							GCMBaseIntentService.java
							GCMBroadcastReceiver.java
							GCMConstants.java
							GCMIntentService.java
							GCMRegistrar.java
				plugin
					GCM
						PushPlugin.java						
				{company_name}
					{intent_name}
						{intent_name}.java						

2) Modify your **AndroidManifest.xml** and add the following lines to your manifest tag, replacing **your_app_package** with your app's package path:


    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
    <permission android:name="your_app_package.permission.C2D_MESSAGE" android:protectionLevel="signature" />
    <uses-permission android:name="your_app_package.permission.C2D_MESSAGE" />


3) Modify your **AndroidManifest.xml** and add the following **activity**, **receiver** and **service** tags to your **application** section, replacing **your_app_package** with your app's package path: (See the Sample_AndroidManifest.xml file in the Example folder.)

	<activity android:name="com.plugin.GCM.PushHandlerActivity"/>
    <receiver android:name="com.google.android.gcm.GCMBroadcastReceiver" android:permission="com.google.android.c2dm.permission.SEND" >
      <intent-filter>
        <action android:name="com.google.android.c2dm.intent.RECEIVE" />
        <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
        <category android:name="your_app_package" />
      </intent-filter>
    </receiver>
    <service android:name="com.google.android.gcm.GCMIntentService" />

4) Modify your **res/xml/config.xml** to include the following line in order to tell Cordova to include this plugin and where it can be found: (See the Sample_config.xml file in the Example folder)

    <plugin name="PushPlugin" value="com.plugin.GCM.PushPlugin" />

5) Add the **PushNotification.js** script to your assets/www folder (or javascripts folder, wherever you want really) and reference it in your main index.html file. This file's usage is described in the **Plugin API** section below.

    <script type="text/javascript" charset="utf-8" src="PushNotification.js"></script>

## Manual Installation for iOS

Copy the following files to your project's Plugins folder:

	AppDelegate+notification.h
	AppDelegate+notification.m
	PushPlugin.h
	PushPlugin.m
	
Add a reference for this plugin to the plugins dictionary in **Cordove.plist**:

	<key>PushPlugin</key>
	<string>PushPlugin</string>

Add the **PushNotification.js** script to your assets/www folder (or javascripts folder, wherever you want really) and reference it in your main index.html file.

    <script type="text/javascript" charset="utf-8" src="PushNotification.js"></script>

## Automatic Installation
This plugin is based on [pluginstall](https://github.com/alunny/pluginstall). to install it to your app,
simply execute pluginstall as follows;

	pluginstall [PLATFORM] [TARGET-PATH] [PLUGIN-PATH]
	
	where
		[PLATFORM] = ios or android
		[TARGET-PATH] = path to folder containing your phonegap project
		[PLUGIN-PATH] = path to folder containing this plugin

For additional info, take a look at the [Cordova Pluginstall Specification](https://github.com/alunny/cordova-plugin-spec)

## Plugin API

In the Examples folder you will find a sample implementation showing how to interact with the PushPlugin. Modify it to suit your needs.

First create the plugin instance variable.

	var pushNotification;
	
When deviceReady fires, get the plugin reference

	pushNotification = window.plugins.pushNotification;
	
#### register
This should be called as soon as the device becomes ready. On success, you will get a call to tokenHandler (iOS), or  onNotificationGCM (Android), allowing you to obtain the device token or registration ID, respectively. Those values will typically get posted to your intermediary push server so it knows who it can send notifications to.


For Android, If you have not already done so, you'll need to set up a Google API project, to generate your senderID. [Follow these steps](http://developer.android.com/guide/google/gcm/gs.html) to do so. This is described more fully in the **Test Environment** section below.

In this example, be sure and substitute your own senderID. Get your senderID by signing into to your [google dashboard](https://code.google.com/apis/console/). The senderID is found at **Overview->Dashboard->Project Number**.

	if (device.platform == 'android' || device.platform == 'Android') {
		pushNotification.register(successHandler, errorHandler,{"senderID":"replace_with_sender_id","ecb":"onNotificationGCM"});
	} else {
		pushNotification.register(tokenHandler, errorHandler {"badge":"true","sound":"true","alert":"true","ecb":"onNotificationAPN"});
	}

**successHandler** - called when a plugin method returns without error

	// result contains any message sent from the plugin call
	function successHandler (result) {
		alert('result = '+result)
	}
	
**errorHandler** - called when the plugin returns an error

	// result contains any error description text returned from the plugin call
	function errorHandler (error) {
		alert('error = '+error)
	}
	
**tokenHandler (iOS ony)** - called when the device has registeredwith a unique device token.

	function tokenHandler (result) {
		// Your iOS push server needs to know the token before it can push to this device
		// here is where you might want to send it the token for later use.
		alert('device token = '+result)
	}

**senderID (Android only)** - This is the Google project ID you need to obtain by [registering your application](http://developer.android.com/guide/google/gcm/gs.html) for GCM

**ecb** - event callback that gets called when your device receives a notification
	
	// iOS
	function onNotificationAPN(event) {
		if (event.alert) {
			navigator.notification.alert(event.alert);
		}
	  
		if (event.sound) {
			var snd = new Media(event.sound);
			snd.play();
		}
	  
		if (event.badge) {
			pushNotification.setApplicationIconBadgeNumber(successHandler, event.badge);
		}
	}


	// Android
	function onNotificationGCM(e) {
		switch( e.event )
		{
			case 'registered':
				if ( e.regid.length > 0 )
				{
					// Your GCM push server needs to know the regID before it can push to this device
					// here is where you might want to send it the regID for later use.
					alert('registration id = e.regid);
				}
			break;
                
			case 'message':
				// if this flag is set, this notification happened while we were in the foreground.
				// you might want to play a sound to get the user's attention, throw up a dialog, etc.
				if (e.foreground)
				{
					$("#app-status-ul").append('<li>--INLINE NOTIFICATION--' + '</li>');
							
					// if the notification contains a soundname, play it.
					var my_media = new Media("/android_asset/www/"+e.soundname);
					my_media.play();
				}
				else	// otherwise we were launched because the user touched a notification in the notification tray.
					$("#app-status-ul").append('<li>--BACKGROUND NOTIFICATION--' + '</li>');
							
				$("#app-status-ul").append('<li>MESSAGE -> MSG: ' + e.payload.message + '</li>');
				$("#app-status-ul").append('<li>MESSAGE -> MSGCNT: ' + e.payload.msgcnt + '</li>');
			break;
                
			case 'error':
				alert('GCM error = e.msg);
			break;
                
			default:
				alert('An unknown GCM event has occurred);
			break;
		}
	}
	
Looking at the above message handling code for Android, a few things bear explaination. Your app may receive a notification while it is active (INLINE). If you background the app by hitting the Home button on your device, you may later receive a status bar notification. Selecting that notification from the status will bring your app to the front and allow you to process the notification (BACKGROUND). You can look at the **foreground** flag on the event to determine whether you are processing a background or na in-line notification. You may choose, for example to play a sound or show a dialog only for inline notifications since the user has already been allerted via the status bar.

Also make note of the **payload** object. Since the Android notification data model is much more flexible than that of iOS, there may be additional elements beyond **message**, **soundname**, and **msgcnt**. You can access those elements and any additional ones via the payload element. This means that if your data model should change in the future, there will be no need to change and recompile the plugin.
	
#### unregister
Call this when your app is exiting to cleanup any used resources.

	pushNotification.unregister(successHandler, errorHandler);
	
You'll probably want to trap on the **backbutton** event and only call this when the home page is showing. Remember, the back button on android is not the same as the Home button. When you hit the back button from the home page, your activity gets dismissed. While you will still receive status bar notifications thereafter, selecting that notification from the status tray will not sub-launch your app. If you, however, "exit" the app by pressing the home button, subsequent status bar touchs WILL sublaunch your app, giving you the opportunity to process the notification. Here is an example of how to trap the backbutton event;

	function onDeviceReady() {
		$("#app-status-ul").append('<li>deviceready event received</li>');
                
		document.addEventListener("backbutton", function(e)
		{
			$("#app-status-ul").append('<li>backbutton event received</li>');
  					
			if( $("#home").length > 0)
			{
				e.preventDefault();
				pushNotification.unregister(successHandler, errorHandler);
				navigator.app.exitApp();
			}
			else
			{
				navigator.app.backHistory();
			}
		}, false);
			
		// aditional onDeviceReady work…
	}
For the above to work, make sure the content for your home page is wrapped in an element with an id of home, like this;

	<div id="home">
		<div id="app-status-div">
			<ul id="app-status-ul">
				<li>Cordova PushNotification Plugin Demo</li>
			</ul>
		</div>
	</div>


	
#### setApplicationIconBadgeNumber (iOS only)
set the badge count visible when the app is not running
	
	pushNotification.setApplicationIconBadgeNumber(successCallback, badgeCount);

**badgeCount** -  an integer indicating what number should show up in the badge. Passing 0 will clear the badge.


## Test Environment
The notification system consists of several interdependent components.

	1) The client application which runs on a device and receives notifications.
	2) The notification service provider (APNS for Apple, GCM for Google)
	3) Intermediary servers that collect device IDs from clients and push notifications through APNS and/or GCM.
	
This plugin and its target Cordova application comprise the client application.The APNS and GCM infrastructure are maintained by Apple and Google, respectively. In order to send push notifications to your users, you would typically run an intermediary server or employ a 3rd party push service. This is true for both GCM (Android) and APNS (iOS) notifications. However, when testing the notification client applications, it may be desirable to be able to push notifications directly from your desktop, without having to design and build those server's first. There are a number of solutions out there to allow you to push from a desktop machine, sans server. The easiest I've found to work with is a ruby gem called [pushmeup](http://rubygems.org/gems/pushmeup). I've only tried this on Mac, but it probably works fine on Windows as well. Here's a rough outline;

**Prerequisites**.

- Ruby gems is installed and working.

- You have successfully built a client with this plugin, on both iOS and Android and have installed them on a device.


#### 1) [Get the gem](https://github.com/NicosKaralis/pushmeup)	
	$ sudo gem install pushmeup
	
#### 2) (iOS) [Follow this tutorial](http://www.raywenderlich.com/3443/apple-push-notification-services-tutorial-part-12) to create a file called ck.pem.
Start at the section entitled "Generating the Certificate Signing Request (CSR)", and substitute your own Bundle Identifier, and Description.
	
	a) go the this plugin's Example/server folder and open pushAPNS.rb in the text editor of your choice.
	b) set the APNS.pem variable to the path of the ck.pem file you just created
	c) set APNS.pass to the password associated with the certificate you just created. (warning this is cleartext, so don't share this file)
	d) set device_token to the token for the device you want to send a push to. (you can run the Cordova app / plugin in Xcode and extract the token from the log messages)
	e) save your changes.
	
#### 3) (Android) [Follow these steps](http://developer.android.com/guide/google/gcm/gs.html) to generate a project ID and a server based API key.

	a) go the this plugin's Example/server folder and open pushGCM.rb in the text editor of your choice.
	b) set the GCM.key variable to the API key you just generated.
	c) set the destination variable to the Registration ID of the device. (you can run the Cordova app / plugin in on a device via Eclipse and extract the regID from the log messages)
	
#### 4) Push a notification

	a) cd to the directory containing the two .rb files we just edited.
	b) Run the Cordova app / plugin on both the Android and iOS devices you used to obtain the regID  / device token, respectively.
	c) $ ruby pushGCM.rb
	d) $ ruby pushAPNS.rb

If all went well, you should see a notification show up on each device. If not, make sure you are not being blocked by a firewall, and that you have internet access. Check and recheck the token id, the registration ID and the certificate generating process.

In a production environment, your app, upon registration, would send the device id (iOS) or the registration id (Android), to your intermediary push server. For iOS, the push certificate would also be stored there, and would be used to authenticate push requests to the APNS server. When a push request is processed, this information is then used to target specific apps running on individual devices.

If you're not up to building and maintaining your own intermediary push server, there are a number of commercial push services out there which support both APNS and GCM.

[Urban Airship](http://urbanairship.com/products/push-notifications/)

[Pushwoosh](http://www.pushwoosh.com/)

[openpush](http://openpush.im)

[kony](http://www.kony.com/push-notification-services) and many others.


## Notes

If you run this demo using the emulator you will not receive notifications from GCM. You need to run it on an actual device to receive messages or install the proper libraries on your emulator (You can follow [this guide](http://www.androidhive.info/2012/10/android-push-notifications-using-google-cloud-messaging-gcm-php-and-mysql/) under the section titled "Installing helper libraries and setting up the Emulator")

If everything seems right and you are not receiving a registration id response back from Google, try uninstalling and reinstalling your app. That has worked for some devs out there.

While the data model for iOS is somewhat fixed, it should be noted that GCM is far more flexible. The Android implementation in this plugin, for example, assumes the incoming message will contain a '**message**' and a '**msgcnt**' node. This is reflected in both the plugin (see GCMIntentService.java) as well as in provided example ruby script (pushGCM.rb). Should you employ a commercial service, their data model may differ. As mentioned earlier, this is where you will want to take a look at the **payload** element of the message event. In addition to the cannonical message and msgcnt elements, any additional elements in the incoming JSON object will be accessible here, obviating the need to edit and recompile the plugin. Many thanks to Tobias Hößl for this functionality!

## Additional Resources

[Local and Push Notification Programming Guide](http://developer.apple.com/library/mac/#documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/ApplePushService/ApplePushService.html) (Apple)

[Google Cloud Messaging for Android](http://developer.android.com/guide/google/gcm/index.html) (Android)

[Apple Push Notification Services Tutorial: Part 1/2](http://www.raywenderlich.com/3443/apple-push-notification-services-tutorial-part-12)

[Apple Push Notification Services Tutorial: Part 2/2](http://www.raywenderlich.com/3525/apple-push-notification-services-tutorial-part-2)

[How to Implement Push Notifications for Android](http://tokudu.com/2010/how-to-implement-push-notifications-for-android/)

## Acknowledgments

Huge thanks to Mark Nutter whose [GCM-Cordova plugin](https://github.com/marknutter/GCM-Cordova) forms the basis for the Android side implimentation.

Likewise, the iOS side was inspired by Olivier Louvignes' [Cordova PushNotification Plugin](https://github.com/phonegap/phonegap-plugins/tree/master/iOS/PushNotification) (Copyright (c) 2012 Olivier Louvignes) for iOS.

Props to [Tobias Hößl](https://github.com/CatoTH), who provided the code to surface the full JSON object up to the JS layer.
