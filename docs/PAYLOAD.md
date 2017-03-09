- [Overview](#overview)
   - [Foreground Events](#push-message-arrives-with-app-in-foreground)
   - [Background Events](#push-message-arrives-with-app-in-background)
   - [Tap Events](#user-clicks-on-notification-in-notification-center)
- [Android Behaviour](#android-behaviour)
  - [Notification vs Data Payloads](#notification-vs-data-payloads)
  - [Localization](#localization)
  - [Images](#images)
  - [Sound](#sound)
  - [Stacking](#stacking)
  - [Inbox Stacking](#inbox-stacking)
  - [Action Buttons](#action-buttons)
    - [In Line Replies](#in-line-replies)
  - [Led in Notifications](#led-in-notifications)
  - [Vibration Pattern in Notifications](#vibration-pattern-in-notifications)
  - [Priority in Notifications](#priority-in-notifications)
  - [Picture Messages](#picture-messages)
  - [Background Notifications](#background-notifications)
    - [Use of content-available: true](#use-of-content-available-true)
  - [Caching](#caching)
  - [Huawei and Xiaomi Phones](#huawei-and-xiaomi-phones)
  - [Application force closed](#application-force-closed)
  - [Visibility](#visibility-of-notifications)
  - [Badges](#badges)
  - [Support for Twilio Notify](#support-for-twilio-notify)
- [iOS Behaviour](#ios-behaviour)
  - [Sound](#sound-1)
  - [Background Notifications](#background-notifications-1)
  - [Action Buttons](#action-buttons-1)
    - [Action Buttons using GCM on iOS](#action-buttons-using-gcm-on-ios)
  - [GCM and Additional Data](#gcm-and-additional-data)
- [Windows Behaviour](#windows-behaviour)
  - [Notifications](#notifications)
  - [Setting Toast Capable Option for Windows](#setting-toast-capable-option-for-windows)
  - [Disabling the default processing of notifications by Windows](#disabling-the-default-processing-of-notifications-by-windows)
  - [Background Notifications](#background-notifications-2)


# Overview

The following flowchart attempts to give you a picture of what happens when a push message arrives on your device when you have an app using phonegap-plugin-push.

![push-flowchart](https://cloud.githubusercontent.com/assets/353180/15752003/36b80afa-28ba-11e6-818b-c6f5f2966d8f.png)

## Push message arrives with app in foreground

- The push plugin receives the data from the remote push service and calls all of your `notification`  event handlers.
- The message is *not* displayed in the devices notification center as that is not normal behaviour for Android or iOS.

## Push message arrives with app in background

- The push plugin receives the data from the remote push service and checks to see if there is a title or message in the data received. If there is then the message will be displayed in the devices notification center.
- Then the push plugin checks to see if the app is running. If the user has killed the application then no further processing of the push data will occur.
- If the app is running in the background the push plugin then checks to see if `content-available` exists in the push data.
- If `content-available` is set to `1` then the plugin calls all of your `notification` event handlers.

## User clicks on notification in notification center

- The app starts.
- Then the plugin calls all of your `notification` event handlers.

> Note: if the push payload contained `content-available: 1` then your `notification` event handler has already been called. It is up to you to handle the double event.

Some ways to handle this *double* event are:

- don't include title/message in the push so it doesn't show up in the shader.
- send two pushes, one to be processed in the background the other to show up in the shade.
- include a unique ID in your push so you can check to see if you've already processed this event.

# Android Behaviour

## Notification vs Data Payloads

Notifications behave differently depending on the foreground/background state of the receiving app and the payload you send to the app.

For instance if you send the following payload:

```
{
    "notification": {
        "title": "Test Notification",
        "body": "This offer expires at 11:30 or whatever",
        "notId": 10
    }
}
```

When your app is in the foreground any `on('notification')` handlers you have registered will be called. However if your app is in the background the notification will show up in the system tray. Clicking on the notification in the system tray will start the app but your `on('notification')` handler will not be called as messages with only `notification` payloads will not cause the plugins `onMessageReceived` method to be called.

If you send a payload with a mix of `notification` & `data` objects like this:

```
{
    "notification": {
        "title": "Test Notification",
        "body": "This offer expires at 11:30 or whatever",
        "notId": 10
    },
    "data" : {
        "surveyID": "ewtawgreg-gragrag-rgarhthgbad"
    }
}
```

When your app is in the foreground any `on('notification')` handlers you have registered will be called. If your app is in the background the notification will show up in the system tray. Clicking on the notification in the system tray will start the app and your `on('notification')` handler will not be called as messages with only `notification` payloads will not cause the plugins `onMessageReceived` method to be called.

My recommended format for your push payload when using this plugin (while it differs from Google's docs) works 100% of the time:

```
{
    "data" : {
        "title": "Test Notification",
        "body": "This offer expires at 11:30 or whatever",
        "notId": 10,
        "surveyID": "ewtawgreg-gragrag-rgarhthgbad"
    }
}
```

When your app is in the foreground any `on('notification')` handlers you have registered will be called. If your app is in the background the notification will show up in the system tray. Clicking on the notification in the system tray will start the app and your `on('notification')` handler will be called and the event received by your `on('notification')` handler will get the following data:

```
{
    "message": "This offer expires at 11:30 or whatever",
    "title": "Test Notification",
    "additionalData": {
        "surveyID": "ewtawgreg-gragrag-rgarhthgbad"
    }
}
```

## Localization

Plugin supported localization from resources for: title, message and summaryText.

You may use simple link to locale constant.

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
        "title": {"locKey": "push_app_title"},
        "message": "Simple non-localizable text for message!"
    }
}
```

Or use localization with formatted constants.

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
        "title": {"locKey": "push_app_title"},
        "message": {"locKey": "push_message_fox", "locData": ["fox", "dog"]}
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
message.addData('title', {"locKey": "push_app_title"});
message.addData('message', 'Simple non-localizable text for message!');
// Constant with formatted params
// message.addData('message', {"locKey": "push_message_fox", "locData": ["fox", "dog"]});
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
    if(err) console.error(err);
    else    console.log(response);
});
```

Localization must store in strings.xml

```xml
<string name="push_app_title">@string/app_name</string>
<string name="push_message_fox">The quick brown %1$s jumps over the lazy %2$s</string>
<string name="push_summary_text">%%n%% new message(s)</string>
```

## Images

By default the icon displayed in your push notification will be your apps icon. So when you initialize the plugin like this:

```javascript
var push = PushNotification.init({
	"android": {
		"senderID": "12345679"
	},
    browser: {
        pushServiceURL: 'http://push.api.phonegap.com/v1/push'
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
    browser: {
        pushServiceURL: 'http://push.api.phonegap.com/v1/push'
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

Finally the Material UI guidelines recommend using a circular icon for the large icon if the subject of the image is a person. This JSON sent from GCM:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Large Circular Icon",
    	"message": "Loaded from URL",
        "image": "https://pbs.twimg.com/profile_images/837060031895896065/VHIQ4oUf_400x400.jpg",
        "image-type": "circle"
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
message.addData('title', 'Large Circular Icon');
message.addData('message', 'Loaded from URL');
message.addData('image', 'https://pbs.twimg.com/profile_images/837060031895896065/VHIQ4oUf_400x400.jpg');
message.addData('image-type', 'circular');
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

Produces the following notification.

![screenshot_20170308-214947](https://cloud.githubusercontent.com/assets/353180/23733917/902a4650-0449-11e7-924e-d45a38030c74.png)

## Sound

For Android there are three special values for sound you can use. The first is `default` which will play the phones default notification sound.

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Default",
    	"message": "Plays default notification sound",
    	"soundname": "default"
    }
}
```

Then second is `ringtone` which will play the phones default ringtone sound.

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Ringtone",
    	"message": "Plays default ringtone sound",
    	"soundname": "ringtone"
    }
}
```
The third is the empty string which will cause for the playing of sound to be skipped.

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Silece",
    	"message": "Skips playing any sound",
    	"soundname": ""
    }
}
```

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

You will see both "Push number 1" and "Push number 2" in the shade.

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

Your notification can include a maximum of three action buttons. If you wish to include an icon along with the button name they must be placed in the `res/drawable` directory of your Android project. Then you can send the following JSON from GCM:

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

If your user clicks on the main body of the notification your app will be opened. However if they click on either of the action buttons the app will open (or start) and the specified JavaScript callback will be executed if there is a function defined, and if there isn't an event will be emitted with the callback name. In this case it is `app.emailGuests` and `app.snooze` respectively. If you set the `foreground` property to `true` the app will be brought to the front, if `foreground` is `false` then the callback is run without the app being brought to the foreground.

### In Line Replies

Android N introduces a new capability for push notifications, the in line reply text field. If you wish to get some text data from the user when the action button is called send the following type of payload:

Your notification can include action buttons. If you wish to include an icon along with the button name they must be placed in the `res/drawable` directory of your Android project. Then you can send the following JSON from GCM:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "AUX Scrum",
    	"message": "Scrum: Daily touchbase @ 10am Please be on time so we can cover everything on the agenda.",
        "actions": [
    		{ "icon": "emailGuests", "title": "EMAIL GUESTS", "callback": "app.emailGuests", "foreground": false, "inline": true },
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
    { "icon": "emailGuests", "title": "EMAIL GUESTS", "callback": "app.emailGuests", "foreground": false, "inline": true},
    { "icon": "snooze", "title": "SNOOZE", "callback": "app.snooze", "foreground": false},
]);
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

On Android N and greater when the user clicks on the Email Guests button they will see the following:

![inline_reply](https://cloud.githubusercontent.com/assets/353180/17107608/f35c208e-525d-11e6-94de-a3590c6f500d.png)

Then your app's `on('notification')` event handler will be called without the app being brought to the foreground and the event data would be:

```
{
  "title": "AUX Scrum",
  "message": "Scrum: Daily touchbase @ 10am Please be on time so we can cover everything on the agenda.",
  "additionalData": {
    "inlineReply": "Sounds good",
    "actions": [
      {
        "inline": true,
        "callback": "app.accept",
        "foreground": false,
        "title": "Accept"
      },
      {
        "icon": "snooze",
        "callback": "app.reject",
        "foreground": false,
        "title": "Reject"
      }
    ],
    "actionCallback": "app.accept",
    "coldstart": false,
    "collapse_key": "do_not_collapse",
    "foreground": false
  }
}
```

and the text data that the user typed would be located in `data.additionalData.inlineReply`.

**Note:** On Android M and earlier the above in line behavior is not supported. As a fallback when `inline` is set to `true` the `foreground` setting will be changed to the default `true` setting. This allows your app to be launched from a closed state into the foreground where any behavior desired as a result of the user selecting the in line reply action button can be handled through the associated `callback`.

#### Attributes

Attribute | Type | Default | Description
--------- | ---- | ------- | -----------
`icon` | `string` | | Optional. The name of a drawable resource to use as the small-icon. The name should not include the extension.
`title` | `string` | | Required. The label to display for the action button.
`callback` | `string` | | Required. The function to be executed or the event to be emitted when the action button is pressed. The function must be accessible from the global namespace. If you provide `myCallback` then it amounts to calling `window.myCallback`. If you provide `app.myCallback` then there needs to be an object call `app`, with a function called `myCallback` accessible from the global namespace, i.e. `window.app.myCallback`. If there isn't a function with the specified name an event will be emitted with the callback name.
`foreground` | `boolean` | `true` | Optional. Whether or not to bring the app to the foreground when the action button is pressed.
`inline` | `boolean` | `false` | Optional. Whether or not to provide a quick reply text field to the user when the button is clicked.

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

You can set a priority parameter for your notifications. This priority value determines where the push notification will be put in the notification shade. Low-priority notifications may be hidden from the user in certain situations, while the user might be interrupted for a higher-priority notification. Add a `priority` field in your notification. -2: minimum, -1: low, 0: default , 1: high, 2: maximum priority.

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

Do not confuse this with the GCM option of setting the [delivery priority of the message](https://developers.google.com/cloud-messaging/concept-options#setting-the-priority-of-a-message). Which is used by GCM to tell the device whether or not it should wake up to deal with the message.

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

> Note: When the notification arrives you will see the title and message like normally. You will only see the picture when the notification is expanded. Once expanded not only will you see the picture but the message portion will disappear and you'll see the summary text portion.

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

or if you want the payload to be delivered directly to your app without anything showing up in the notification center omit the tite/message from the payload like so:


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

### Application force closed

In order to take advantage of this feature you will need to be using cordova-android 6.0.0 or higher. In order to check if the change has been properly applied look at `platforms/android/**/MainActivity.java`. You should see an `onCreate` method that looks like this:

```java
@Override
public void onCreate(Bundle savedInstanceState)
{
    super.onCreate(savedInstanceState);

    // enable Cordova apps to be started in the background
    Bundle extras = getIntent().getExtras();
    if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
        moveTaskToBack(true);
    }

    // Set by <content src="index.html" /> in config.xml
    loadUrl(launchUrl);
}
```

If you don't see the `if` statement that checks for the appearance of `cdvStartInBackground` you will probably need to do:

```
phonegap platform rm android
phonegap platform add android
phonegap build android
```

This should add the correct code to the `MainActivity` class.

If you add `force-start: 1` to the data payload the application will be restarted in background even if it was force closed.

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Force Start",
    	"message": "This notification should restart the app",
    	"force-start": 1
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
message.addData('title', 'Force Start');
message.addData('message', 'This notification should restart the app');
message.addData('force-start', 1);
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

### Caching

By default, when a notification arrives and 'content-available' is set to '1', the plugin will try to deliver the data payload even if the app is not running. In that case, the payload is cached and may be delivered when the app is started again. To disable this behavior, you can set a `no-cache` flag in the notification payload. 0: caching enabled (default), 1: caching disabled.

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
        "title": "Push without cache",
        "message": "When the app is closed, this notification will not be cached",
        "content-available": "1",
        "no-cache": "1"
    }
}
```

## Visibility of Notifications

You can set a visibility parameter for your notifications. Just add a `visibility` field in your notification. -1: secret, 0: private (default), 1: public. `Secret` shows only the most minimal information, excluding even the notification's icon. `Private` shows basic information about the existence of this notification, including its icon and the name of the app that posted it. The rest of the notification's details are not displayed. `Public` Shows the notification's full content.

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "This is a maximum public Notification",
    	"message": "This notification should appear in front of all others",
    	"visibility": 1
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
message.addData('title', 'This is a public Notification');
message.addData('message', 'You should be able to read this notification on your lock screen');
message.addData('visibility', 1);
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

## Badges

On Android not all launchers support badges. In order for us to set badges we use [ShortcutBadger](https://github.com/leolin310148/ShortcutBadger) in order to set the badge. Check out their website to see which launchers are supported.

In order to set the badge number you will need to include the `badge` property in your push payload as below:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Badge Test",
    	"message": "Badges, we don't need no stinking badges",
    	"badge": 7
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
message.addData('title', 'Badge Test');
message.addData('message', 'Badges, we don\'t need no stinking badges');
message.addData('badge', 7);
service.send(message, { registrationTokens: [ deviceID ] }, function (err, response) {
	if(err) console.error(err);
	else 	console.log(response);
});
```

## Support for Twilio Notify

This plugin seamlessly supports payloads generated by Twilio Notify on Android. Specifically the parameters passed in to the Twilio REST API are available in the message payload passed to your app as follows:

- `Title` --> `data.title`
- `Body` --> `data.message`
- `Sound` --> `data.sound`

Here is an example request to Twilio REST API and the corresponding JSON received by your app.

```
curl 'https://notify.twilio.com/v1/Services/IS1e928b239609199df31d461071fd3d23/Notifications' -X POST \
--data-urlencode 'Identity=Bob' \
--data-urlencode 'Body=Hello Bob! Twilio Notify + Phonegap is awesome!' \
--data-urlencode 'Title=Hello Bob!' \
--data-urlencode 'Sound=chime' \
-u [AccountSID]:[AuthToken]
```

The JSON received by your app will comply with the standards described in the sections above:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "Hello Bob!",
    	"message": "Hello Bob! Twilio Notify + Phonegap is awesome!",
    	"sound": "chime"
    }
}
```

Note: "sound" and "soundname" are equivalent and are considered to be the same by the plugin.

# iOS Behaviour

## Sound

In order for your notification to play a custom sound you will need to add the files to root of your iOS project. The files must be in the proper format. See the [Local and Remote Notification Programming Guide](https://developer.apple.com/library/content/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/SupportingNotificationsinYourApp.html#//apple_ref/doc/uid/TP40008194-CH4-SW10) for more info on proper file formats and how to convert existing sound files.

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

First the JSON you send from APNS will need to include `"content-available": 1` to the `aps` object. The `"content-available": 1` property in your push message is a signal to iOS to wake up your app and give it up to 30 seconds of background processing. If do not want this type of behaviour just omit `"content-available": 1` from your push data. As well you *should* set a `notId` property in the root of payload object. This is the parameter you pass to the `finish` method in order to tell the operating system that the processing of the push event is done.

For instance the following JSON:

```javascript
{
	"aps": {
		"alert": "Test background push",
		"content-available": 1
	},
  "notId": 1 // unique ID you generate
}
```

will produce a notification in the notification shade and call your `on('notification')` event handler.

**NOTE:** The `on('notification')` event handler will **not** be called if Background App Refresh is disabled on the user's iOS device. (Settings > General > Background App Refresh)

However if you want your `on('notification')` event handler called but no notification to be shown in the shader you would omit the `alert` property and send the following JSON to APNS:

```javascript
{
	"aps": {
		"data": "Test silent background push",
		"moredata": "Do more stuff",
		"content-available": 1
	},
  "notId": 2 // unique ID you generate
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
	}, function() {
    console.log("something went wrong with push.finish for ID = " + data.additionalData.notId)
  }, data.additionalData.notId);
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

## GCM and Additional Data

GCM on iOS is a different animal. The way you send data via GCM on Android is like:

```javascript
{
    "registration_ids": ["my device id"],
    "data": {
    	"title": "My Title",
    	"message": "My message",
    	"key1": "data 1",
    	"key2": "data 2"
    }
}
```

will produce a `notification` event with the following data:

```javascript
{
    "title": "My Title",
    "message": "My message",
    "additionalData": {
        "key1": "data 1",
        "key2": "data 2"
    }
}
```

but in order for the same `notification` event you would need to send your push to GCM iOS in a slight different format:

```javascript
{
    "registration_ids": ["my device id"],
    "notification": {
        "title": "My Title",
    	"body": "My message"
    }
    "data": {
    	"key1": "data 1",
    	"key2": "data 2"
    }
}
```

The `title` and `body` need to be in the `notification` part of the payload in order for the OS to pick them up correctly. Everything else should be in the `data` part of the payload.

## GCM Messages Not Arriving

For some users of the plugin they are unable to get messages sent via GCM to show up on their devices. If you are running into this issue try setting the `priority` of the message to `high` in the payload.

```javascript
{
    "registration_ids": ["my device id"],
    "notification": {
        "title": "My Title",
    	"body": "My message"
    },
    "priority": "high"
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

## Background Notifications

On Windows, to trigger the on('notification') event handler when your app is in the background and it is launched through the push notification, you will have to include `activation` data in the payload of the notification. This is done by using the `launch` attribute, which can be any string that can be understood by the app. However it should not cause the XML payload to become invalid.

If you do not include a launch attribute string, your app will be launched normally, as though the user had launched it from the Start screen, and the notification event handler won't be called.

Here is an example of a sample toast notification payload containing the launch attribute:

```xml
<toast launch="{&quot;myContext&quot;:&quot;12345&quot;}">
    <visual>
        <binding template="ToastImageAndText01">
            <image id="1" src="ms-appx:///images/redWide.png" alt="red graphic"/>
            <text id="1">Hello World!</text>
        </binding>
    </visual>
</toast>
```

This launch attribute string is passed on to the app as data.launchArgs through the on('notification') handler. It's important to note that due to the Windows platform design, the other visual payload is not available to the handler on cold start. So notification attributes like message, title etc. which are available through the on('notification') handler when the app is running, won't be available for background notifications.
