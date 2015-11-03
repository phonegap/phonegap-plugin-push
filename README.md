#phonegap-plugin-push [![Build Status](https://travis-ci.org/phonegap/phonegap-plugin-push.svg)](https://travis-ci.org/phonegap/phonegap-plugin-push)

> Register and receive push notifications

## Installation

This requires phonegap/cordova CLI 5.0+ ( current stable v1.4.2 )

```
phonegap plugin add phonegap-plugin-push
```
or 

```
cordova plugin add phonegap-plugin-push
```

It is also possible to install via repo url directly ( unstable )

```
phonegap plugin add https://github.com/phonegap/phonegap-plugin-push
```

or 

```
cordova plugin add https://github.com/phonegap/phonegap-plugin-push
```

## Supported Platforms

- Android
- iOS
- Windows Universal

## Quick Example

```javascript
    var push = PushNotification.init({ "android": {"senderID": "12345679"},
         "ios": {"alert": "true", "badge": "true", "sound": "true"}, "windows": {} } );

    push.on('registration', function(data) {
        // data.registrationId
    });

    push.on('notification', function(data) {
        // data.message,
        // data.title,
        // data.count,
        // data.sound,
        // data.image,
        // data.additionalData
    });

    push.on('error', function(e) {
        // e.message
    });
```

## API

### PushNotification.init(options)

Parameter | Description
--------- | ------------
`options` | `JSON Object` platform specific initialization options.
`options.android` | `JSON Object` Android specific initialization options.
`options.android.senderID` | `String` Maps to the project number in the Google Developer Console.
`options.android.icon` | `String` Optional. The name of a drawable resource to use as the small-icon.
`options.android.iconColor` | `String` Optional. Sets the background color of the small icon on Android 5.0 and greater. [Supported Formats](http://developer.android.com/reference/android/graphics/Color.html#parseColor(java.lang.String))
`options.android.sound` | `Boolean` Optional. If `true` it plays the sound specified in the push data or the default system sound. Default is `true`.
`options.android.vibrate` | `Boolean` Optional. If `true` the device vibrates on receipt of notification. Default is `true`.
`options.android.clearNotifications` | `Boolean` Optional. If `true` the app clears all pending notifications when it is closed. Default is `true`.
`options.android.forceShow` | `Boolean` Optional. If `true` will always show a notification, even when the app is on the foreground. Default is `false`.
`options.ios` | `JSON Object` iOS specific initialization options.
`options.ios.alert` | `Boolean`\|`String` Optional. If `true`\|`"true"` the device shows an alert on receipt of notification. Default is `false`\|`"false"`. **Note:** the value you set this option to the first time you call the init method will be how the application always acts. Once this is set programmatically in the init method it can only be changed manually by the user in Settings>Notifications>`App Name`. This is normal iOS behaviour.
`options.ios.badge` | `Boolean`\|`String` Optional. If `true`\|`"true"` the device sets the badge number on receipt of notification. Default is `false`\|`"false"`. **Note:** the value you set this option to the first time you call the init method will be how the application always acts. Once this is set programmatically in the init method it can only be changed manually by the user in Settings>Notifications>`App Name`. This is normal iOS behaviour.
`options.ios.sound` | `Boolean`\|`String` Optional. If `true`\|`"true"` the device plays a sound on receipt of notification. Default is `false`\|`"false"`. **Note:** the value you set this option to the first time you call the init method will be how the application always acts. Once this is set programmatically in the init method it can only be changed manually by the user in Settings>Notifications>`App Name`. This is normal iOS behaviour.
`options.ios.clearBadge` | `Boolean`\|`String` Optional. If `true`\|`"true"` the badge will be cleared on app startup. Default is `false`\|`"false"`.
`options.windows` | `JSON Object` Windows specific initialization options.

#### Returns

- Instance of `PushNotification`.

#### Example

```javascript
    var push = PushNotification.init({ "android": {"senderID": "12345679"},
         "ios": {"alert": "true", "badge": "true", "sound": "true"}, "windows": {} } );
```

### push.on(event, callback)

Parameter | Description
--------- | ------------
`event` | `String` Name of the event to listen to. See below for all the event names.
`callback` | `Function` is called when the event is triggered.

### push.on('registration', callback)

The event `registration` will be triggered on each successful registration with the 3rd party push service.

Callback Parameter | Description
------------------ | -----------
`data.registrationId` | `String` The registration ID provided by the 3rd party remote push service.

#### Example

```javascript
push.on('registration', function(data) {
    // data.registrationId
});
```

### push.on('notification', callback)

The event `notification` will be triggered each time a push notification is received by a 3rd party push service on the device.

Callback Parameter | Description
------------------ | -----------
`data.message` | `String` The text of the push message sent from the 3rd party service.
`data.title` | `String` The optional title of the push message sent from the 3rd party service.
`data.count` | `String` The number of messages to be displayed in the badge iOS or message count in the notification shade in Android. For windows, it represents the value in the badge notification which could be a number or a status glyph.
`data.sound` | `String` The name of the sound file to be played upon receipt of the notification.
`data.image` | `String` The path of the image file to be displayed in the notification.
`data.additionalData` | `JSON Object` An optional collection of data sent by the 3rd party push service that does not fit in the above properties.
`data.additionalData.foreground` | `Boolean` Whether the notification was received while the app was in the foreground

#### Example

```javascript
    push.on('notification', function(data) {
        // data.message,
        // data.title,
        // data.count,
        // data.sound,
        // data.image,
        // data.additionalData
    });
```

### push.on('error', callback)

The event `error` will trigger when an internal error occurs and the cache is aborted.

Callback Parameter | Description
------------------ | -----------
`e` | `Error` Standard JavaScript error object that describes the error.

#### Example

```javascript
push.on('error', function(e) {
    // e.message
});
```

### push.unregister(successHandler, errorHandler)

The unregister method is used when the application no longer wants to receive push notifications.

#### Example

```javascript
push.unregister(successHandler, errorHandler);
```

### push.setApplicationIconBadgeNumber(successHandler, errorHandler, count) - iOS only

Set the badge count visible when the app is not running

The `count` is an integer indicating what number should show up in the badge. Passing 0 will clear the badge. Each `notification` event contains a `data.count` value which can be used to set the badge to correct number.

#### Example

```javascript
push.setApplicationIconBadgeNumber(successHandler, errorHandler, count);
```

### push.getApplicationIconBadgeNumber(successHandler, errorHandler) - iOS only

Get the current badge count visible when the app is not running

successHandler gets called with an integer which is the current badge count

#### Example

```javascript
push.getApplicationIconBadgeNumber(successHandler, errorHandler);
```

### push.finish(successHandler, errorHandler) - iOS only

Tells the OS that you are done processing a background push notification.

successHandler gets called when background push processing is successfully completed.

#### Example

```javascript
push.finish(successHandler, errorHandler);
```

## PhoneGap Build Support

Including this plugin in a project that is built by PhoneGap Build is as easy as adding:

```
<gap:plugin name="phonegap-plugin-push" source="npm" />
```

into your apps `config.xml` file. PhoneGap Build will pick up the latest version of phonegap-plugin-push published on npm. If you want to specify a particular version of the plugin you can add the `version` attribute to the `gap` tag.

```
<gap:plugin name="phonegap-plugin-push" source="npm" version="1.2.3" />
```

Note: version 1.3.0 of this plugin begins to use Gradle to install the Android Support Framework. Support for Gradle has recently been added to PhoneGap Build. Please read [this blog post](http://phonegap.com/blog/2015/09/28/android-using-gradle/) for more information.

## Android Behaviour

### Compiling

As of version 1.3.0 the plugin has been switched to using Gradle/Maven for building. You will need to ensure that you have installed the Android Support Library version 23 or greater, Android Support Repository version 20 or greater, Google Play Services version 27 or greater and Google Repository version 22 or greater.

![android support library](https://cloud.githubusercontent.com/assets/353180/10230226/0627931e-684a-11e5-9a6b-72d72997f655.png)

For more detailed instructions on how to install the Android Support Library visit [Google's documentation](https://developer.android.com/tools/support-library/setup.html).

### Images

By default the icon displayed in your push notification will be your apps icon. So when you initialize the plugin like this:

```javascript
    var push = PushNotification.init({ "android": {"senderID": "12345679"},
         "ios": {"alert": "true", "badge": "true", "sound": "true"}, "windows": {} } );
```

The result will look much like this:

![2015-07-24 02 52 00](https://cloud.githubusercontent.com/assets/353180/8866899/2df00c3c-3190-11e5-8552-96201fb4424b.png)

This is because Android now uses Material design and the default icon for push will be completely white.

In order to get a better user experience you can specify an alternate icon and background color to be shown when receiving a push notification. The code would look like this:

```javascript
	var push = PushNotification.init({ 
		"android": { 
			"senderID": "123456789", "icon": "phonegap", "iconColor": "blue"}, 
		"ios": {"alert": "true", "badge": "true", "sound": "true"}, "windows": {} 
	});
```

Where *icon* is the name of an image in the Android *drawables* folder. Writing a hook to describe how to copy an image to the Android *drawables* folder is out of scope for this README but there is an [excellent tutorial](http://devgirl.org/2013/11/12/three-hooks-your-cordovaphonegap-project-needs/) that you can copy.

*iconColor* is one of the supported formats #RRGGBB or #AARRGGBB or one of the following names: 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'darkgray', 'grey', 'lightgrey', 'darkgrey', 'aqua', 'fuchsia', 'lime', 'maroon', 'navy', 'olive', 'purple', 'silver', 'teal'. *iconColor* is supported on Android 5.0 and greater.

Please follow the [Android icon design guidelines](https://www.google.com/design/spec/style/icons.html#) when creating your icon.

![2015-07-24 02 46 58](https://cloud.githubusercontent.com/assets/353180/8866902/2df3276e-3190-11e5-842a-c8cd95615ab0.png)

Additionally, each push can include a large icon which is used to personalize each push. The location of the image may one of three types.

The first is the *drawables* folder in your app. This JSON sent from GCM:

```javascript
{
	title:"Large Icon",
	message: "Loaded from drawables folder",
	image: "twitter"
}
```

Would look for the *twitter* image in the drawables folder and produce the following notification.

![2015-07-24 02 34 41](https://cloud.githubusercontent.com/assets/353180/8866903/2df48028-3190-11e5-8176-fe8b3f7c5aab.png)

The second is the *assets* folder in your app. This JSON sent from GCM:

```javascript
{
	title:"Large Icon",
	message: "Loaded from assets folder",
	image: "www/image/logo.png"
}
```

Would look for the *logo.png* file in the assets/www/img folder. Since your apps www folder gets copied into the Android assets folder it is an excellent spot to store the images without needing to write a hook to copy them to the *drawables* folder. It produces the following notification.

![2015-07-24 02 20 02](https://cloud.githubusercontent.com/assets/353180/8866901/2df19052-3190-11e5-8c16-a355c59209f3.png)


The third is the remote *URL*. This JSON sent from GCM:

```javascript
{
	title:"Large Icon",
	message: "Loaded from URL",
	image: "https://dl.dropboxusercontent.com/u/887989/antshot.png"
}
```

Produces the following notification.

![2015-07-24 02 17 55](https://cloud.githubusercontent.com/assets/353180/8866900/2df0ab06-3190-11e5-9a81-fdb85bb0f5a4.png)

### Sound

In order for your your notification to play a custom sound you will need to add the files to your Android project's `res/raw` directory. Then send the follow JSON from GCM:

```javascript
{
	title:"Sound Test",
	message: "Loaded res/raw",
	soundname: "test"
}
```

*Note:* when you specify the custom sound file name omit the file's extension.

### Stacking

By default when using this plugin on Android each notification that your app receives will replace the previous notification in the shade.

If you want to see multiple notifications in the shade you will need to provide a notification ID as part of the push data sent to the app. For instance if you send:

```javascript
{
  title: "Test Push",
  message: "Push number 1"
}
```

Followed by:

```javascript
{
  title: "Test Push",
  message: "Push number 2"
}
```

You will only see "Push number 2" in the shade. However, if you send:

```javascript
{
  title: "Test Push",
  message: "Push number 1",
  notId: 1
}
```

and:

```javascript
{
  title: "Test Push",
  message: "Push number 2",
  notId: 2
}
```

You will only see both "Push number 1" and "Push number 2" in the shade.

### Inbox Stacking ###

A better alternative to stacking your notifications is to use the inbox style to have up to 8 lines of notification text in a single notification. If you send the following JSON from GCM you will see:

```javascript
{
	title:"My Title",
	message: "My first message",
	style: "inbox",
	summaryText: "There are %n% notifications"
}
```

It will produce a normal looking notification:

![2015-08-25 14 11 27](https://cloud.githubusercontent.com/assets/353180/9468840/c9c5d43a-4b11-11e5-814f-8dc995f47830.png)

But, if you follow it up with subsequent notifications like:

```javascript
{
	title:"My Title",
	message: "My second message",
	style: "inbox",
	summaryText: "There are %n% notifications"
}
```

You will get an inbox view so you can display multiple notifications in a single panel.

![2015-08-25 14 01 35](https://cloud.githubusercontent.com/assets/353180/9468727/2d658bee-4b11-11e5-90fa-248d54c8f3f6.png)

If you use `%n%` in the `summaryText` of the JSON coming down from GCM it will be replaced by the number of messages that are currently in the queue.

### Action Buttons

Your notification can include action buttons. If you wish to include an icon along with the button name they must be placed in the `res/drawable` directory of your Android project. Then you can send the following JSON from GCM:

```javascript
{
	title:"AUX Scrum",
	message: "Scrum: Daily touchbase @ 10am Please be on time so we can cover everything on the agenda.",
	actions: [
		{ icon: "emailGuests", title: "EMAIL GUESTS", callback: "app.emailGuests"},
		{ icon: "snooze", title: "SNOOZE", callback: "app.snooze"},
	]
}
```

This will produce the following notification in your tray:

![action_combo](https://cloud.githubusercontent.com/assets/353180/9313435/02554d2a-44f1-11e5-8cd9-0aadd1e02b18.png)

If your users clicks on the main body of the notification your app will be opened. However if they click on either of the action buttons the app will open (or start) and the specified JavaScript callback will be executed. In this case it is `app.emailGuests` and `app.snooze` respectively.

### Led in Notifications

You can use a Led notifcation and choose the color of it. Just add a `ledColor` field in your notification in the ARGB format array:

```javascript
{
  title:"Green LED",
  message: "This is my message with a Green LED",
  ledColor: [0, 0, 255, 0]
}
```

### Vibration Pattern in Notifications

You can set a Vibration Pattern for your notifications. Just add a `vibrationPattern` field in your notification:

```javascript
{
  title:"Vibration Pattern",
  message: "Device should wait for 2 seconds, vibrate for 1 second then be silent for 500 ms then vibrate for 500 ms",
  vibrationPattern: [2000, 1000, 500, 500]
}
```

### Priority in Notifications

You can set a priority parameter for your notifications. Just add a `priority` field in your notification. -2: minimum, -1: low, 0: default , 1: high, 2: maximum priority:

```javascript
{
  title:"This is a maximum priority Notification",
  message: "This notification should appear in front of all others",
  priority: 2
}
```

### Picture Messages

Perhaps you want to include a large picture in the notification that you are sending to your users. Luckily you can do that too buy sending the following JSON from GCM.

```javascript
{
	title:"Big Picture",
	message: "This is my big picture message",
	style: "picture",
    picture: "http://36.media.tumblr.com/c066cc2238103856c9ac506faa6f3bc2/tumblr_nmstmqtuo81tssmyno1_1280.jpg",
	summaryText: "The internet is built on cat pictures"
}
```

This will produce the following notification in your tray:

![2015-08-25 16 08 00](https://cloud.githubusercontent.com/assets/353180/9472260/3655fa7a-4b22-11e5-8d87-20528112de16.png)

### Co-existing with FaceBook Plugin

There are a number of Cordova FaceBook Plugins available but the one that we recommend is [Jeduan's fork](https://github.com/jeduan/cordova-plugin-facebook4) of the original Wizcorp plugin. It is setup to use Gradle/Maven properly and the latest FaceBook SDK.

To add to your app:

```
phonegap plugin add https://github.com/jeduan/cordova-plugin-facebook4 --variable APP_ID="App ID" --variable APP_NAME="App Name"
```
or 

```
cordova plugin add https://github.com/jeduan/cordova-plugin-facebook4 --variable APP_ID="App ID" --variable APP_NAME="App Name"
```

If you have an issue compiling the app and you are getting this error:

```
* What went wrong:
Execution failed for task ':processDebugManifest'.
> Manifest merger failed : uses-sdk:minSdkVersion 14 cannot be smaller than version 15 declared in library /Users/smacdona/code/bookface/platforms/android/build/intermediates/exploded-aar/com.facebook.android/facebook-android-sdk/4.6.0/AndroidManifest.xml
  	Suggestion: use tools:overrideLibrary="com.facebook" to force usage
```

Then you can add the following entry into your config.xml file in the android platform tag.

```
<platform name="android">
    <preference name="android-minSdkVersion" value="15"/>
 </platform>
 ```


### Background Notifications

On Android if you want your `on('notification')` event handler to be called when your app is in the background it is relatively simple.

The JSON you send to GCM should not contain a title or message parameter. For instance the following JSON:

```javascript
{
  title: "Test Push",
  message: "Push number 1",
  info: "super secret info"
}
```

will produce a notification in the notification shade and call your `on('notification')` event handler.

However if you want your `on('notification')` event handler called but no notification to be shown in the shader you would omit the `alert` property and send the following JSON to GCM:

```javascript
{
  info: "super secret info"
}
```

Omitting the message and title properties will keep your push from being added to the notification shade but it will still trigger your `on('notification')` event handler.

## iOS Behaviour

### Sound

In order for your your notification to play a custom sound you will need to add the files to root of your iOS project. The files must be in the proper format. See the [Local and Remote Notification Programming Guide](https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/IPhoneOSClientImp.html#//apple_ref/doc/uid/TP40008194-CH103-SW6) for more info on proper file formats and how to convert existing sound files.

Then send the follow JSON from APNS:

```javascript
{
    "aps": {
        "alert": "Test sound",
        "sound": "sub.caf"
    }
}
```

### Background Notifications

On iOS if you want your `on('notification')` event handler to be called when your app is in the background you will need to do a few things.

First the JSON you send from APNS will need to include `content-available: 1` to the `aps` object. The `content-available: 1` property in your push message is a signal to iOS to wake up your app and give it up to 30 seconds of background processing. If do not want this type of behaviour just omit `content-available: 1` from your push data.


For instance the following JSON:

```javascript
{
    "aps": {
        "alert": "Test background push",
        "content-available": "1"
    }
}
```

will produce a notification in the notification shade and call your `on('notification')` event handler.

However if you want your `on('notification')` event handler called but no notification to be shown in the shader you would omit the `alert` property and send the following JSON to APNS:

```javascript
{
    "aps": {
        "data": "Test silent background push",
        "moredata": "Do more stuff",
        "content-available": "1"
    }
}
```

That covers what you need to do on the server side to accept background pushes on iOS. However, it is critically important that you continue reading as there will be a change in your `on('notification')`. When you receive a background push on iOS you will be given 30 seconds of time in which to complete a task. If you spend longer than 30 seconds on the task the OS may decide that your app is misbehaving and kill it. In order to signal iOS that your `on('notification')` handler is done you will need to call the new `push.finish()` method. 

For example:

```javascript
        var push = PushNotification.init({
            "ios": {
              "sound": true,
              "vibration": true,
              "badge": true,
              "clearBadge": true
            }
        });
        
        push.on('registration', function(data) {
        	// send data.registrationId to push service
        });
        

        push.on('notification', function(data) {
        	// do something with the push data
        	// then call finish to let the OS know we are done
            push.finish(function() {
                console.log("processing of push data is finished");
            });
        });
```

It is absolutely critical that you call `push.finish()` when you have successfully processed your background push data.

## Windows Behaviour

###Notifications

The plugin supports all types of windows platform notifications namely [Tile, Toast, Badge and Raw](https://msdn.microsoft.com/en-us/library/windows/apps/Hh779725.aspx). The API supports the basic cases of the notification templates with title corresponding to the first text element and message corresponding to the second if title is present else the first one. The image corresponds to the first image element of the notification xml.

The count is present only for the badge notification in which it represent the value of the notification which could be a number from 0-99 or a status glyph.

For advanced templates and usage, the notification object is included in [`data.additionalData.pushNotificationReceivedEventArgs`](https://msdn.microsoft.com/en-us/library/windows/apps/windows.networking.pushnotifications.pushnotificationreceivedeventargs).

### Setting Toast Capable Option for Windows

This plugin automatically sets the toast capable flag to be true for Cordova 5.1.1+. For lower versions, you must declare that it is Toast Capable in your app's manifest file. 

### Disabling the default processing of notifications by Windows

The default handling can be disabled by setting the 'cancel' property in the notification object. 

```
data.additionalData.pushNotificationReceivedEventArgs.cancel = true
```

## Native Requirements

- There should be no dependency on any other plugins.
- All platforms should use the same API!

## Running Tests

```
npm test
```

## Contributing

### Editor Config

The project uses [.editorconfig](http://editorconfig.org/) to define the coding
style of each file. We recommend that you install the Editor Config extension
for your preferred IDE.

### JSHint

The project uses [.jshint](http://jshint.com/docs) to define the JavaScript
coding conventions. Most editors now have a JSHint add-on to provide on-save
or on-edit linting.

#### Install JSHint for vim

1. Install [jshint](https://www.npmjs.com/package/jshint).
1. Install [jshint.vim](https://github.com/wookiehangover/jshint.vim).

#### Install JSHint for Sublime

1. Install [Package Control](https://packagecontrol.io/installation)
1. Restart Sublime
1. Type `CMD+SHIFT+P`
1. Type _Install Package_
1. Type _JSHint Gutter_
1. Sublime -> Preferences -> Package Settings -> JSHint Gutter
1. Set `lint_on_load` and `lint_on_save` to `true`
