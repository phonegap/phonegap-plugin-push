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
`data.image (windows only)` | `String` The path of the image file to be displayed in the notification.
`data.additionalData` | `JSON Object` An optional collection of data sent by the 3rd party push service that does not fit in the above properties.

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

## Android Behaviour

By default when using this plugin on Android each notification that your app receives will replace the previous notification in the shade. 

If you want to see multiple notifications in the shade you will need to provide a notification ID as part of the push data sent to the app. For instance if you send:

```
{ 
  title: "Test Push",
  message: "Push number 1"
}
```

Followed by:

```
{ 
  title: "Test Push",
  message: "Push number 2"
}
```

You will only see "Push number 2" in the shade. However, if you send:

```
{ 
  title: "Test Push",
  message: "Push number 1",
  nodId: 1
}
```

and:

```
{ 
  title: "Test Push",
  message: "Push number 2",
  nodId: 2
}
```

You will only see both "Push number 1" and "Push number 2" in the shade.

## Windows Notifications

The plugin supports all types of windows platform notifications namely Tile, Toast, Badge and Raw. The API supports the basic cases of the notification templates with title corresponding to the first text element and message corresponding to the second if title is present else the first one. The image corresponds to the first image element of the notification xml.

The count is present only for the badge notification in which it represent the value of the notification which could be a number from 0-99 or a status glyph.

For advanced templates and usage, the notification object is included in data.additionalData.pushNotificationReceivedEventArgs.

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
