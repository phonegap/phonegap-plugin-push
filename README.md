#phonegap-plugin-push [![Build Status](https://travis-ci.org/phonegap/phonegap-plugin-push.svg)](https://travis-ci.org/phonegap/phonegap-plugin-push)

> Register and receive push notifications

## Installation

This requires phonegap 5.0+ ( current stable v1.0.0 )

```
phonegap plugin add phonegap-plugin-push
```

It is also possible to install via repo url directly ( unstable )

```
phonegap plugin add https://github.com/phonegap/phonegap-plugin-push
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
`data.image (android/windows only)` | `String` The path of the image file to be displayed in the notification.
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
<gap:plugin name="phonegap-plugin-push" source="npm" />`
```

into your apps `config.xml` file. PhoneGap Build will pick up the latest version of phonegap-plugin-push published on npm. If you want to specify a particular version of the plugin you can add the `version` attribute to the `gap` tag.

```
<gap:plugin name="phonegap-plugin-push" source="npm" version="1.1.1" />`
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
  nodId: 1
}
```

and:

```javascript
{ 
  title: "Test Push",
  message: "Push number 2",
  nodId: 2
}
```

You will only see both "Push number 1" and "Push number 2" in the shade.

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

For your app to communicate through a toast notification, you must declare that it is Toast Capable in your app's manifest file. Cordova-windows 4.0.0 release adds this property to config.xml. You can use:
`<preference name="WindowsToastCapable" value="true" />` in config.xml. However, you will need Cordova 5.1.1 which pins Cordova-windows 4.0.0.

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
