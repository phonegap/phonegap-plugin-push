#phonegap-plugin-push [![Build Status](https://travis-ci.org/phonegap/phonegap-plugin-push.svg)](https://travis-ci.org/phonegap/phonegap-plugin-push)

> Register and receive push notifications

## Installation

This requires phonegap/cordova CLI 5.0+ ( current stable v1.2.2 )

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
         "ios": {}, "windows": {} } );

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
`options.android.iconColor` | `String` Optional. Sets the background color of the small icon. [Supported Formats](http://developer.android.com/reference/android/graphics/Color.html#parseColor(java.lang.String))
`options.android.sound` | `Boolean` Optional. If `true` it plays the sound specified in the push data or the default system sound. Default is `true`.
`options.android.vibrate` | `Boolean` Optional. If `true` the device vibrates on receipt of notification. Default is `true`.
`options.android.clearNotifications` | `Boolean` Optional. If `true` the app clears all pending notifications when it is closed. Default is `true`.
`options.ios` | `JSON Object` iOS specific initialization options.
`options.windows` | `JSON Object` Windows specific initialization options.

#### Returns

- Instance of `PushNotification`.

#### Example

```javascript
    var push = PushNotification.init({ "android": {"senderID": "12345679"},
         "ios": {}, "windows": {} } );
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

## PhoneGap Build Support

Including this plugin in a project that is built by PhoneGap Build is as easy as adding:

```
<gap:plugin name="phonegap-plugin-push" source="npm" />
```

into your apps `config.xml` file. PhoneGap Build will pick up the latest version of phonegap-plugin-push published on npm. If you want to specify a particular version of the plugin you can add the `version` attribute to the `gap` tag.

```
<gap:plugin name="phonegap-plugin-push" source="npm" version="1.1.1" />
```

## Android Behaviour

### Images

By default the icon displayed in your push notification will be your apps icon. So when you initialize the plugin like this:

```javascript
    var push = PushNotification.init({ "android": {"senderID": "12345679"},
         "ios": {}, "windows": {} } );
```

The result will look much like this:

![2015-07-24 02 52 00](https://cloud.githubusercontent.com/assets/353180/8866899/2df00c3c-3190-11e5-8552-96201fb4424b.png)

This is because Android now uses Material design and the default icon for push will be completely white.

In order to get a better user experience you can specify an alternate icon and background color to be shown when receiving a push notification. The code would look like this:

```javascript
	var push = PushNotification.init({
		"android": {
			"senderID": "123456789", "icon": "phonegap", "iconColor": "blue"},
		"ios": {}, "windows": {}
	});
```

Where *icon* is the name of an image in the Android *drawables* folder. Writing a hook to describe how to copy an image to the Android *drawables* folder is out of scope for this README but there is an [excellent tutorial](http://devgirl.org/2013/11/12/three-hooks-your-cordovaphonegap-project-needs/) that you can copy.

*iconColor* is one of the supported formats #RRGGBB or #AARRGGBB or one of the following names: 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'darkgray', 'grey', 'lightgrey', 'darkgrey', 'aqua', 'fuchsia', 'lime', 'maroon', 'navy', 'olive', 'purple', 'silver', 'teal'.

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
  message: "Device should vibrate during 2 seconds then holds during 1 second then vibrate during 500 ms",
  vibrationPattern: [2000, 1000, 500]
}
```

### Priority in Notifications

You can set a priority parameter for your notifications. Just add a `priority` field in your notification. 0 means low priority, 1: normal, 2: high priority:

```javascript
{
  title:"This is a high priority Notification",
  message: "This notification should appear in front of all others and should be displayed instantly on the user device",
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

## Windows Behaviour

###Notifications

The plugin supports all types of windows platform notifications namely [Tile, Toast, Badge and Raw](https://msdn.microsoft.com/en-us/library/windows/apps/Hh779725.aspx). The API supports the basic cases of the notification templates with title corresponding to the first text element and message corresponding to the second if title is present else the first one. The image corresponds to the first image element of the notification xml.

The count is present only for the badge notification in which it represent the value of the notification which could be a number from 0-99 or a status glyph.

For advanced templates and usage, the notification object is included in [`data.additionalData.pushNotificationReceivedEventArgs`](https://msdn.microsoft.com/en-us/library/windows/apps/windows.networking.pushnotifications.pushnotificationreceivedeventargs).

### Setting Toast Capable Option for Windows

This plugin automatically sets the toast capable flag to be true for Cordova 5.1.1+. For lower versions, you must declare that it is Toast Capable in your app's manifest file. 

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
