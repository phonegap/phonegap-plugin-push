- [Android Behaviour](#android-behaviour)
  - [Images](#images)
  - [Sound](#sound)
  - [Stacking](#stacking)
  - [Inbox Stacking](#inbox-stacking)
  - [Action Buttons](#action-buttons)
  - [Led in Notifications](#led-in-notifications)
  - [Vibration Pattern in Notifications](#vibration-pattern-in-notifications)
  - [Priority in Notifications](#priority-in-notifications)
  - [Picture Messages](#picture-messages)
  - [Background Notifications](#background-notifications)
    - [Use of content-available: true](#use-of-content-available-true)
- [iOS Behaviour](#ios-behaviour)
  - [Sound](#sound-1)
  - [Background Notifications](#background-notifications-1)
  - [Action Buttons](#action-buttons-1)
    - [Action Buttons using GCM on iOS](#action-buttons-using-gcm-on-ios)
    - [Huawei and Xiaomi Phones](#huawei-and-xiaomi-phones)
- [Windows Behaviour](#windows-behaviour)
  - [Notifications](#notifications)
  - [Setting Toast Capable Option for Windows](#setting-toast-capable-option-for-windows)
  - [Disabling the default processing of notifications by Windows](#disabling-the-default-processing-of-notifications-by-windows)


# Android Behaviour

## Images

By default the icon displayed in your push notification will be your apps icon. So when you initialize the plugin like this:

```javascript
var push = PushNotification.init({
	"android": {
		"senderID": "12345679"
	},
	"ios": {
		"alert": "true",
		"badge": "true",
		"sound": "true"
	},
	"windows": {}
});
```

The result will look much like this:

![2015-07-24 02 52 00](https://cloud.githubusercontent.com/assets/353180/8866899/2df00c3c-3190-11e5-8552-96201fb4424b.png)

This is because Android now uses Material design and the default icon for push will be completely white.

In order to get a better user experience you can specify an alternate icon and background color to be shown when receiving a push notification. The code would look like this:

```javascript
var push = PushNotification.init({
	"android": {
		"senderID": "123456789",
		"icon": "phonegap",
		"iconColor": "blue"
	},
    "ios": {
		"alert": "true",
		"badge": "true",
		"sound": "true"
	},
	"windows": {}
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
    "registration_ids": ["my device id"],
    "data": {
        "title": "Large Icon",
    	"message": "Loaded from drawables folder",
    	"image": "twitter"
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'Large Icon');
message.addData('message', 'Loaded from drawables folder.');
message.addData('image', 'twitter');
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

Would look for the *twitter* image in the drawables folder and produce the following notification.

![2015-07-24 02 34 41](https://cloud.githubusercontent.com/assets/353180/8866903/2df48028-3190-11e5-8176-fe8b3f7c5aab.png)

The second is the *assets* folder in your app. This JSON sent from GCM:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Large Icon",
    	"message": "Loaded from assets folder",
    	"image": "www/image/logo.png"
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'Large Icon');
message.addData('message', 'Loaded from assets folder.');
message.addData('image', 'www/image/logo.png');
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

Would look for the *logo.png* file in the assets/www/img folder. Since your apps www folder gets copied into the Android assets folder it is an excellent spot to store the images without needing to write a hook to copy them to the *drawables* folder. It produces the following notification.

![2015-07-24 02 20 02](https://cloud.githubusercontent.com/assets/353180/8866901/2df19052-3190-11e5-8c16-a355c59209f3.png)


The third is the remote *URL*. This JSON sent from GCM:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Large Icon",
    	"message": "Loaded from URL",
    	"image": "https://dl.dropboxusercontent.com/u/887989/antshot.png"
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'Large Icon');
message.addData('message', 'Loaded from URL');
message.addData('image', 'https://dl.dropboxusercontent.com/u/887989/antshot.png');
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

Produces the following notification.

![2015-07-24 02 17 55](https://cloud.githubusercontent.com/assets/353180/8866900/2df0ab06-3190-11e5-9a81-fdb85bb0f5a4.png)

## Sound

For Android there are two special values for sound you can use. The first is `default` which will play the phones default notification sound. Then second is `ringtone` which will play the phones default ringtone sound.

In order for your your notification to play a custom sound you will need to add the files to your Android project's `res/raw` directory. Then send the follow JSON from GCM:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Sound Test",
    	"message": "Loaded res/raw",
    	"soundname": "test"
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'Sound Test');
message.addData('message', 'Loaded res/raw');
message.addData('soundname', 'test');
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

*Note:* when you specify the custom sound file name omit the file's extension.

## Stacking

By default when using this plugin on Android each notification that your app receives will replace the previous notification in the shade.

If you want to see multiple notifications in the shade you will need to provide a notification ID as part of the push data sent to the app. For instance if you send:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Test Push",
    	"message": "Push number 1"
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'Test Push');
message.addData('message', 'Push number 1');
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

Followed by:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Test Push",
    	"message": "Push number 2"
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'Test Push');
message.addData('message', 'Push number 2');
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

You will only see "Push number 2" in the shade. However, if you send:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Test Push",
    	"message": "Push number 1",
    	"notId": 1
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'Test Push');
message.addData('message', 'Push number 1');
message.addData('notId', 1);
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

and:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Test Push",
    	"message": "Push number 2",
    	"notId": 2
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'Test Push');
message.addData('message', 'Push number 2');
message.addData('notId', 2);
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

You will only see both "Push number 1" and "Push number 2" in the shade.

## Inbox Stacking

A better alternative to stacking your notifications is to use the inbox style to have up to 8 lines of notification text in a single notification. If you send the following JSON from GCM you will see:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "My Title",
    	"message": "My first message",
    	"style": "inbox",
    	"summaryText": "There are %n% notifications"
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'My Title');
message.addData('message', 'My first message');
message.addData('style', 'inbox');
message.addData('summaryText', 'There are %n% notifications');
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

It will produce a normal looking notification:

![2015-08-25 14 11 27](https://cloud.githubusercontent.com/assets/353180/9468840/c9c5d43a-4b11-11e5-814f-8dc995f47830.png)

But, if you follow it up with subsequent notifications like:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "My Title",
    	"message": "My second message",
    	"style": "inbox",
    	"summaryText": "There are %n% notifications"
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'My Title');
message.addData('message', 'My second message');
message.addData('style', 'inbox');
message.addData('summaryText', 'There are %n% notifications');
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

You will get an inbox view so you can display multiple notifications in a single panel.

![2015-08-25 14 01 35](https://cloud.githubusercontent.com/assets/353180/9468727/2d658bee-4b11-11e5-90fa-248d54c8f3f6.png)

If you use `%n%` in the `summaryText` of the JSON coming down from GCM it will be replaced by the number of messages that are currently in the queue.

## Action Buttons

Your notification can include action buttons. If you wish to include an icon along with the button name they must be placed in the `res/drawable` directory of your Android project. Then you can send the following JSON from GCM:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "AUX Scrum",
    	"message": "Scrum: Daily touchbase @ 10am Please be on time so we can cover everything on the agenda.",
        "actions": [
    		{ "icon": "emailGuests", "title": "EMAIL GUESTS", "callback": "app.emailGuests", "foreground": true},
    		{ "icon": "snooze", "title": "SNOOZE", "callback": "app.snooze", "foreground": false}
    	]
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'AUX Scrum');
message.addData('message', 'Scrum: Daily touchbase @ 10am Please be on time so we can cover everything on the agenda.');
message.addData('actions', [
    { "icon": "emailGuests", "title": "EMAIL GUESTS", "callback": "app.emailGuests", "foreground": true},
    { "icon": "snooze", "title": "SNOOZE", "callback": "app.snooze", "foreground": false},
]);
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

This will produce the following notification in your tray:

![action_combo](https://cloud.githubusercontent.com/assets/353180/9313435/02554d2a-44f1-11e5-8cd9-0aadd1e02b18.png)

If your users clicks on the main body of the notification your app will be opened. However if they click on either of the action buttons the app will open (or start) and the specified JavaScript callback will be executed. In this case it is `app.emailGuests` and `app.snooze` respectively. If you set the `foreground` property to `true` the app will be brought to the front, if `foreground` is `false` then the callback is run without the app being brought to the foreground.

### Attributes

Attribute | Type | Default | Description
--------- | ---- | ------- | -----------
`icon` | `string` | | Optional. The name of a drawable resource to use as the small-icon. The name should not include the extension.
`title` | `string` | | Required. The label to display for the action button.
`callback` | `string` | | Required. The function to be executed when the action button is pressed.
`foreground` | `boolean` | `true` | Optional. Whether or not to bring the app to the foreground when the action button is pressed.

## Led in Notifications

You can use a Led notifcation and choose the color of it. Just add a `ledColor` field in your notification in the ARGB format array:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Green LED",
    	"message": "This is my message with a Green LED",
    	"ledColor": [0, 0, 255, 0]
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'Green LED');
message.addData('message', 'This is my message with a Green LED');
message.addData('ledColor', [0, 0, 255, 0]);
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

## Vibration Pattern in Notifications

You can set a Vibration Pattern for your notifications. Just add a `vibrationPattern` field in your notification:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Vibration Pattern",
    	"message": "Device should wait for 2 seconds, vibrate for 1 second then be silent for 500 ms then vibrate for 500 ms",
    	"vibrationPattern": [2000, 1000, 500, 500]
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'Vibration Pattern');
message.addData('message', 'Device should wait for 2 seconds, vibrate for 1 second then be silent for 500 ms then vibrate for 500 ms');
message.addData('vibrationPattern', [2000, 1000, 500, 500]);
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

## Priority in Notifications

You can set a priority parameter for your notifications. Just add a `priority` field in your notification. -2: minimum, -1: low, 0: default , 1: high, 2: maximum priority:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "This is a maximum priority Notification",
    	"message": "This notification should appear in front of all others",
    	"priority": 2
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'This is a maximum priority Notification');
message.addData('message', 'This notification should appear in front of all others');
message.addData('priority', 2);
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

## Picture Messages

Perhaps you want to include a large picture in the notification that you are sending to your users. Luckily you can do that too by sending the following JSON from GCM.

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Big Picture",
    	"message": "This is my big picture message",
    	"style": "picture",
    	"picture": "http://36.media.tumblr.com/c066cc2238103856c9ac506faa6f3bc2/tumblr_nmstmqtuo81tssmyno1_1280.jpg",
    	"summaryText": "The internet is built on cat pictures"
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'Big Picture');
message.addData('message', 'This is my big picture message');
message.addData('style', 'picture');
message.addData('picture', 'http://36.media.tumblr.com/c066cc2238103856c9ac506faa6f3bc2/tumblr_nmstmqtuo81tssmyno1_1280.jpg');
message.addData('summaryText', 'The internet is built on cat pictures');
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

This will produce the following notification in your tray:

![2015-08-25 16 08 00](https://cloud.githubusercontent.com/assets/353180/9472260/3655fa7a-4b22-11e5-8d87-20528112de16.png)


## Background Notifications

On Android if you want your `on('notification')` event handler to be called when your app is in the background it is relatively simple.

First the JSON you send from GCM will need to include `"content-available": "1"`. This will tell the push plugin to call your `on('notification')` event handler no matter what other data is in the push notification.

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Test Push",
    	"message": "Push number 1",
    	"info": "super secret info",
    	"content-available": "1"
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('title', 'Test Push');
message.addData('message', 'Push number 1');
message.addData('info', 'super secret info');
message.addData('content-available', '1');
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

or


```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"info": "super secret info",
    	"content-available": "1"
    }
}
```

Here is an example using node-gcm that sends the above JSON:

```javascript
var gcm = require('node-gcm');
// Replace these with your own values.
var apiKey = "replace with API key";
var deviceID = "my device id";
var service = new gcm.Sender(apiKey);
var message = new gcm.Message();
message.addData('info', 'super secret info');
message.addData('content-available', '1');
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

If do not want this type of behaviour just omit `"content-available": 1` from your push data and your `on('notification')` event handler will not be called.

### Use of content-available: true

The GCM docs will tell you to send a data payload of:

```javascript
{
    "registration_ids": ["my device id"],
    "content_available": true,
    "data": {
        "title": "Test Push",
        "message": "Push number 1",
        "info": "super secret info",
    }
}
```

Where the `content-available` property is part of the main payload object. Setting the property in this part of the payload will result in the PushPlugin not getting the data correctly. Setting `content-available: true` will cause the Android OS to handle the push payload for you and not pass the data to the PushPlugin.

Instead move `content-available: true` into the `data` object of the payload and set it to `1` as per the example below:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
        "title": "Test Push",
        "message": "Push number 1",
        "info": "super secret info",
        "content-available": "1"
    }
}
```

### Huawei and Xiaomi Phones

These phones have a particular quirk that when the app is force closed that you will no longer be able to receive notifications until the app is restarted. In order for you to receive background notifications:

- On your Huawei device go to Settings > Protected apps > check "My App" where.
- On your Xiaomi makes sure your phone has the "Auto-start" property enabled for your app.


# iOS Behaviour

## Sound

In order for your notification to play a custom sound you will need to add the files to root of your iOS project. The files must be in the proper format. See the [Local and Remote Notification Programming Guide](https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/IPhoneOSClientImp.html#//apple_ref/doc/uid/TP40008194-CH103-SW6) for more info on proper file formats and how to convert existing sound files.

Then send the follow JSON from APNS:

```javascript
{
	"aps": {
		"alert": "Test sound",
		"sound": "sub.caf"
	}
}
```

If you want the default sound to play upon receipt of push use this payload:

```
{
    "aps": {
        "alert": "Test sound",
        "sound": "default"
    }
}
```

## Background Notifications

On iOS if you want your `on('notification')` event handler to be called when your app is in the background you will need to do a few things.

First the JSON you send from APNS will need to include `"content-available": 1` to the `aps` object. The `"content-available": 1` property in your push message is a signal to iOS to wake up your app and give it up to 30 seconds of background processing. If do not want this type of behaviour just omit `"content-available": 1` from your push data.


For instance the following JSON:

```javascript
{
	"aps": {
		"alert": "Test background push",
		"content-available": 1
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
		"content-available": 1
	}
}
```

That covers what you need to do on the server side to accept background pushes on iOS. However, it is critically important that you continue reading as there will be a change in your `on('notification')`. When you receive a background push on iOS you will be given 30 seconds of time in which to complete a task. If you spend longer than 30 seconds on the task the OS may decide that your app is misbehaving and kill it. In order to signal iOS that your `on('notification')` handler is done you will need to call the new `push.finish()` method.

For example:

```javascript
var push = PushNotification.init({
	"ios": {
		"sound": "true",
		"alert": "true",
		"badge": "true",
		"clearBadge": "true"
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

## Action Buttons

Your notification can include action buttons. For iOS 8+ you must setup the possible actions when you initialize the plugin:

```javascript
var push = PushNotification.init({
	"ios": {
		"sound": true,
		"alert": true,
		"badge": true,
		"categories": {
			"invite": {
				"yes": {
					"callback": "app.accept", "title": "Accept", "foreground": true, "destructive": false
				},
				"no": {
					"callback": "app.reject", "title": "Reject", "foreground": true, "destructive": false
				},
				"maybe": {
					"callback": "app.maybe", "title": "Maybe", "foreground": true, "destructive": false
				}
			},
			"delete": {
				"yes": {
					"callback": "app.doDelete", "title": "Delete", "foreground": true, "destructive": true
				},
				"no": {
					"callback": "app.cancel", "title": "Cancel", "foreground": true, "destructive": false
				}
			}
		}
	}
});
```

You’ll notice that we’ve added a new parameter to the iOS object of our init code called categories. Each category is a named object, invite and delete in this case. These names will need to match the one you send via your payload to APNS if you want the action buttons to be displayed. Each category can have up to three buttons which must be labeled `yes`, `no` and `maybe`. In turn each of these buttons has four properties, `callback` the javascript function you want to call, `title` the label for the button, `foreground` whether or not to bring your app to the foreground and `destructive` which doesn’t actually do anything destructive it just colors the button red as a warning to the user that the action may be destructive.

Just like with background notifications it is absolutely critical that you call `push.finish()` when you have successfully processed the button callback. For instance:

```javascript
app.accept = function(data) {
    // do something with the notification data

    push.finish(function() {
        console.log('accept callback finished');
    }, function() {
        console.log('accept callback failed');
    }, data.additionalData.notId);    
};
```

You may notice that the `finish` method now takes `success`, `failure` and `id` parameters. The `id` parameter let's the operating system know which background process to stop. You'll set it in the next step.

Then you will need to set the `category` value in your `aps` payload to match one of the objects in the `categories` object. As well you *should* set a `notId` property in the root of payload object. This is the parameter you pass to the `finish` method in order to tell the operating system that the processing of the push event is done.

```javascript
{
	"aps": {
		"alert": "This is a notification that will be displayed ASAP.",
		"category": "invite"
	},
    "notId": "1"
}
```

This will produce the following notification in your tray:

![push6-ios](https://cloud.githubusercontent.com/assets/353180/12754125/12d13020-c998-11e5-98b4-b245fda30490.png)

If your users clicks on the main body of the notification your app will be opened. However if they click on either of the action buttons the app will open (or start) and the specified JavaScript callback will be executed.

### Action Buttons using GCM on iOS

If you are using GCM to send push messages on iOS you will need to send a different payload in order for the action buttons to be present in the notification shade. You'll need to use the `click-action` property in order to specify the category.

```javascript
{
    "registration_ids": ["my device id"],
    "notification": {
    	"title": "AUX Scrum",
    	"body": "Scrum: Daily touchbase @ 10am Please be on time so we can cover everything on the agenda.",
        "click-action": "invite"
    }
}
```

# Windows Behaviour

## Notifications

The plugin supports all types of windows platform notifications namely [Tile, Toast, Badge and Raw](https://msdn.microsoft.com/en-us/library/windows/apps/Hh779725.aspx). The API supports the basic cases of the notification templates with title corresponding to the first text element and message corresponding to the second if title is present else the first one. The image corresponds to the first image element of the notification xml.

The count is present only for the badge notification in which it represent the value of the notification which could be a number from 0-99 or a status glyph.

For advanced templates and usage, the notification object is included in [`data.additionalData.pushNotificationReceivedEventArgs`](https://msdn.microsoft.com/en-us/library/windows/apps/windows.networking.pushnotifications.pushnotificationreceivedeventargs).

## Setting Toast Capable Option for Windows

This plugin automatically sets the toast capable flag to be true for Cordova 5.1.1+. For lower versions, you must declare that it is Toast Capable in your app's manifest file.

## Disabling the default processing of notifications by Windows

The default handling can be disabled by setting the 'cancel' property in the notification object.

```javascript
data.additionalData.pushNotificationReceivedEventArgs.cancel = true
```
