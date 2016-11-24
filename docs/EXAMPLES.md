# Creating a Project From a Template

If you want to get started with a sample project you can create a new project from the example template.

```
phonegap create my-app --template phonegap-template-push
```

## Quick Example

```javascript
var push = PushNotification.init({
	android: {
	},
    browser: {
        pushServiceURL: 'http://push.api.phonegap.com/v1/push'
    },
	ios: {
		alert: "true",
		badge: "true",
		sound: "true"
	},
	windows: {}
});

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
