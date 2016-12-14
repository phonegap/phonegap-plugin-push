# API

- [.init()](#pushnotificationinitoptions)
- [.hasPermission() - Android & iOS only](#pushnotificationhaspermissionsuccesshandler---android--ios-only)
- [push.on()](#pushonevent-callback)
  - [push.on('registration')](#pushonregistration-callback)
  - [push.on('notification')](#pushonnotification-callback)
  - [push.on('error')](#pushonerror-callback)
- [push.off()](#pushoffevent-callback)
- [push.unregister()](#pushunregistersuccesshandler-errorhandler-topics)
- [push.subscribe()](#pushsubscribetopic-successhandler-errorhandler)
- [push.unsubscribe()](#pushunsubscribetopic-successhandler-errorhandler)
- [push.setApplicationIconBadgeNumber() - iOS & Android only](#pushsetapplicationiconbadgenumbersuccesshandler-errorhandler-count---ios--android-only)
- [push.getApplicationIconBadgeNumber() - iOS only](#pushgetapplicationiconbadgenumbersuccesshandler-errorhandler---ios-only)
- [push.finish() - iOS only](#pushfinishsuccesshandler-errorhandler-id---ios-only)
- [push.clearAllNotifications() - iOS & Android only](#pushclearallnotificationssuccesshandler-errorhandler---ios--android-only)

## PushNotification.init(options)

Initializes the plugin on the native side.

**Note:** like all plugins you must wait until you receive the [`deviceready`](https://cordova.apache.org/docs/en/5.4.0/cordova/events/events.deviceready.html) event before calling `PushNotification.init()`.

**Note:** you will want to call `PushNotification.init()` each time your app starts. The remote push service can periodically reset your registration ID so this ensures you have the correct value.

### Returns

- Instance of `PushNotification`.

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`options` | `Object` | `{}` | An object describing relevant specific options for all target platforms.

All available option attributes are described bellow. Currently, there are no Windows specific options.

#### Android

Attribute | Type | Default | Description
--------- | ---- | ------- | -----------
`android.senderID` | `string` | | Maps to the project number in the Google Developer Console.
`android.icon` | `string` | | Optional. The name of a drawable resource to use as the small-icon. The name should not include the extension.
`android.iconColor` | `string` | | Optional. Sets the background color of the small icon on Android 5.0 and greater. [Supported Formats](http://developer.android.com/reference/android/graphics/Color.html#parseColor(java.lang.String))
`android.sound` | `boolean` | `true` | Optional. If `true` it plays the sound specified in the push data or the default system sound.
`android.vibrate` | `boolean` | `true` | Optional. If `true` the device vibrates on receipt of notification.
`android.clearBadge` | `boolean` | `false` | Optional. If `true` the icon badge will be cleared on init and before push messages are processed.
`android.clearNotifications` | `boolean` | `true` | Optional. If `true` the app clears all pending notifications when it is closed.
`android.forceShow` | `boolean` | `false` | Optional. Controls the behavior of the notification when app is in foreground. If `true` and app is in foreground, it will show a notification in the notification drawer, the same way as when the app is in background (and `on('notification')` callback will be called *only when the user clicks the notification*). When `false` and app is in foreground, the `on('notification')` callback will be called immediately.
`android.topics` | `array` | `[]` | Optional. If the array contains one or more strings each string will be used to subscribe to a GcmPubSub topic.

#### Browser

Attribute | Type | Default | Description
--------- | ---- | ------- | -----------
`browser.pushServiceURL` | `string` | `http://push.api.phonegap.com/v1/push` | Optional. URL for the push server you want to use.

#### iOS

All iOS boolean options can also be specified as `string`

Attribute | Type | Default | Description
--------- | ---- | ------- | -----------
`ios.alert` | `boolean` | `false` | Optional. If `true` the device shows an alert on receipt of notification. **Note:** the value you set this option to the first time you call the init method will be how the application always acts. Once this is set programmatically in the init method it can only be changed manually by the user in Settings>Notifications>`App Name`. This is normal iOS behaviour.
`ios.badge` | `boolean` | `false` | Optional. If `true` the device sets the badge number on receipt of notification. **Note:** the value you set this option to the first time you call the init method will be how the application always acts. Once this is set programmatically in the init method it can only be changed manually by the user in Settings>Notifications>`App Name`. This is normal iOS behaviour.
`ios.sound` | `boolean` | `false` | Optional. If `true` the device plays a sound on receipt of notification. **Note:** the value you set this option to the first time you call the init method will be how the application always acts. Once this is set programmatically in the init method it can only be changed manually by the user in Settings>Notifications>`App Name`. This is normal iOS behaviour.
`ios.clearBadge` | `boolean` | `false` | Optional. If `true` the badge will be cleared on app startup.
`ios.categories` | `Object` | `{}` | Optional. The data required in order to enabled Action Buttons for iOS. See [Action Buttons on iOS](https://github.com/phonegap/phonegap-plugin-push/blob/master/docs/PAYLOAD.md#action-buttons-1) for more details.

#### iOS GCM support

The following properties are used if you want use GCM on iOS.

Attribute | Type | Default | Description
--------- | ---- | ------- | -----------
`ios.senderID` | `string` | `undefined` (Native) | Maps to the project number in the Google Developer Console.  Setting this uses GCM for notifications instead of native
`ios.gcmSandbox` | `boolean` | `false` | Whether to use prod or sandbox GCM setting.  Defaults to false.
`ios.topics` | `array` | `[]` | Optional. If the array contains one or more strings each string will be used to subscribe to a GcmPubSub topic. Note: only usable in conjunction with `senderID`.

##### How GCM on iOS works.

First it is kind of a misnomer as GCM does not send push messages directly to devices running iOS.

What happens is on the device side is that it registers with APNS, then that registration ID is sent to GCM which returns a different GCM specific ID. That is the ID you get from the push plugin `registration` event.

When you send a message to GCM using that ID, what it does is look up the APNS registration ID on it's side and forward the message you sent to GCM on to APSN to deliver to your iOS device.

Make sure that the certificate you build with matches your `gcmSandbox` value.

- If you build your app as development and set `gcmSandbox: false` it will fail.
- If you build your app as production and set `gcmSandbox: true` it will fail.
- If you build your app as development and set `gcmSandbox: true` but haven't uploaded the development certs to Google it will fail.
- If you build your app as production and set `gcmSandbox: false` but haven't uploaded the production certs to Google it will fail.

> Note: The integration between GCM and APNS is a bit finicky. Personally, I feel it is much better to send pushes to Android using GCM and pushes to iOS using APNS which this plugin does support.

### Example

```javascript
var push = PushNotification.init({
	android: {
		senderID: "12345679"
	},
    browser: {
        pushServiceURL: 'http://push.api.phonegap.com/v1/push'
    },
	ios: {
		alert: "true",
		badge: true,
		sound: 'false'
	},
	windows: {}
});
```

## PushNotification.hasPermission(successHandler) - Android & iOS only

Checks whether the push notification permission has been granted.

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`successHandler` | `Function` | | Is called when the api successfully retrieves the details on the permission.

### Callback parameters

#### `successHandler`

Parameter | Type | Description
--------- | ---- | -----------
`data.isEnabled` | `Boolean` | Whether the permission for push notifications has been granted.

### Example

```javascript
PushNotification.hasPermission(function(data) {
    if (data.isEnabled) {
        console.log('isEnabled');
    }
});
```

## push.on(event, callback)

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`event` | `string` | | Name of the event to listen to. See below for all the event names.
`callback` | `Function` | | Is called when the event is triggered.

## push.on('registration', callback)

The event `registration` will be triggered on each successful registration with the 3rd party push service.

### Callback parameters

Parameter | Type | Description
--------- | ---- | -----------
`data.registrationId` | `string` | The registration ID provided by the 3rd party remote push service.

### Example

```javascript
push.on('registration', function(data) {
	console.log(data.registrationId);
});
```

For APNS users: the `registrationId` you will get will be a production or sandbox id according to how the app was built. ([Source](https://developer.apple.com/library/ios/technotes/tn2265/_index.html))

> Note: There is a separate persistent connection to the push service for each environment. The operating system establishes a persistent connection to the sandbox environment for development builds; ad hoc and distribution builds connect to the production environment.



### Common Problems

#### Got JSON Exception TIMEOUT

If you run this plugin on older versions of Android and you get an error:

```
E/PushPlugin(20077): execute: Got JSON Exception TIMEOUT
```

It means you are running an older version of Google Play Services. You will need to open the Google Play Store app and update your version of Google Play Services.

## push.on('notification', callback)

The event `notification` will be triggered each time a push notification is received by a 3rd party push service on the device.

### Callback parameters

Parameter | Type | Description
--------- | ---- | -----------
`data.message` | `string` | The text of the push message sent from the 3rd party service.
`data.title` | `string` | The optional title of the push message sent from the 3rd party service.
`data.count` | `string` | The number of messages to be displayed in the badge in iOS/Android or message count in the notification shade in Android. For windows, it represents the value in the badge notification which could be a number or a status glyph.
`data.sound` | `string` | The name of the sound file to be played upon receipt of the notification.
`data.image` | `string` | The path of the image file to be displayed in the notification.
`data.launchArgs` | `string` | The args to be passed to the application on launch from push notification. This works when notification is received in background. (Windows Only)
`data.additionalData` | `Object` | An optional collection of data sent by the 3rd party push service that does not fit in the above properties.
`data.additionalData.foreground` | `boolean` | Whether the notification was received while the app was in the foreground
`data.additionalData.coldstart` | `boolean` | Will be `true` if the application is started by clicking on the push notification, `false` if the app is already started.

### Example

```javascript
push.on('notification', function(data) {
	console.log(data.message);
	console.log(data.title);
	console.log(data.count);
	console.log(data.sound);
	console.log(data.image);
	console.log(data.additionalData);
});
```

## push.on('error', callback)

The event `error` will trigger when an internal error occurs and the cache is aborted.

### Callback parameters

Parameter | Type | Description
--------- | ---- | -----------
`e` | `Error` | Standard JavaScript error object that describes the error.

### Example

```javascript
push.on('error', function(e) {
	console.log(e.message);
});
```

## push.off(event, callback)

Removes a previously registered callback for an event.

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`event` | `string` | | Name of the event type. The possible event names are the same as for the `push.on` function.
`callback` | `Function` | | The same callback used to register with `push.on`.

### Example
```javascript
var callback = function(data){ /*...*/};

//Adding handler for notification event
push.on('notification', callback);

//Removing handler for notification event
push.off('notification', callback);
```

**WARNING**: As stated in the example, you will have to store your event handler if you are planning to remove it.

## push.unregister(successHandler, errorHandler, topics)

The unregister method is used when the application no longer wants to receive push notifications. Beware that this cleans up all event handlers previously registered, so you will need to re-register them if you want them to function again without an application reload.

If you provide a list of topics as an optional parameter then the application will unsubscribe from these topics but continue to receive other push messages.

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`successHandler` | `Function` | | Is called when the api successfully unregisters.
`errorHandler` | `Function` | | Is called when the api encounters an error while unregistering.
`topics` | `Array` | | A list of topics to unsubscribe from.

### Example

```javascript
push.unregister(function() {
	console.log('success');
}, function() {
	console.log('error');
});
```

## push.subscribe(topic, successHandler, errorHandler)

The subscribe method is used when the application wants to subscribe a new topic to receive push notifications.

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`topic` | `String` | | Topic to subscribe to.
`successHandler` | `Function` | | Is called when the api successfully subscribes.
`errorHandler` | `Function` | | Is called when the api encounters an error while subscribing.

### Example

```javascript
push.subscribe('my-topic', function() {
	console.log('success');
}, function(e) {
	console.log('error:');
	console.log(e);
});
```
## push.unsubscribe(topic, successHandler, errorHandler)

The unsubscribe method is used when the application no longer wants to receive push notifications from a specific topic but continue to receive other push messages.

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`topic` | `String` | | Topic to unsubscribe from.
`successHandler` | `Function` | | Is called when the api successfully unsubscribe.
`errorHandler` | `Function` | | Is called when the api encounters an error while unsubscribing.

### Example

```javascript
push.unsubscribe('my-topic', function() {
	console.log('success');
}, function(e) {
	console.log('error:');
	console.log(e);
});
```

## push.setApplicationIconBadgeNumber(successHandler, errorHandler, count) - iOS & Android only

Set the badge count visible when the app is not running

> Note: badges are not supported on all Android devices. See [our payload documentation](PAYLOAD.md#badges) for more details.

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`successHandler` | `Function` | | Is called when the api successfully sets the icon badge number.
`errorHandler` | `Function` | | Is called when the api encounters an error while trying to set the icon badge number.
`count` | `number` | | Indicates what number should show up in the badge. Passing 0 will clear the badge. Each `notification` event contains a `data.count` value which can be used to set the badge to correct number.

### Example

```javascript
push.setApplicationIconBadgeNumber(function() {
	console.log('success');
}, function() {
	console.log('error');
}, 2);
```

## push.getApplicationIconBadgeNumber(successHandler, errorHandler) - iOS only

Get the current badge count visible when the app is not running

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`successHandler` | `Function` | | Is called when the api successfully retrieves the icon badge number.
`errorHandler` | `Function` | | Is called when the api encounters an error while trying to retrieve the icon badge number.

### Callback parameters

#### `successHandler`

Parameter | Type | Description
--------- | ---- | -----------
`n` | `number` | An integer which is the current badge count.

### Example

```javascript
push.getApplicationIconBadgeNumber(function(n) {
	console.log('success', n);
}, function() {
	console.log('error');
});
```

## push.finish(successHandler, errorHandler, id) - iOS only

Tells the OS that you are done processing a background push notification.

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`successHandler` | `Function` | | Is called when the api successfully completes background push processing.
`errorHandler` | `Function` | | Is called when the api encounters an error while processing and completing the background push.
`id` | `String` | | Tells the OS which background process is complete.

### Example

```javascript
push.finish(function() {
	console.log('success');
}, function() {
	console.log('error');
}, 'push-1');
```

## push.clearAllNotifications(successHandler, errorHandler) - iOS & Android only

Tells the OS to clear all notifications from the Notification Center

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`successHandler` | `Function` | | Is called when the api successfully clears the notifications.
`errorHandler` | `Function` | | Is called when the api encounters an error when attempting to clears the notifications.

### Example

```javascript
push.clearAllNotifications(function() {
	console.log('success');
}, function() {
	console.log('error');
});
```
