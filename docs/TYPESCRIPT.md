# Typescript definitions

For those of you who use Typescript, we're glad to say that we provide the complete definition file along with our package.

## Example usage

All objects will be understood as having a defined type, including init options and eventHandler parameters.
All available attributes and properties will have autocomplete support and type checkings.

```typescript
import 'phonegap-plugin-push/types';

const push = PushNotification.init({
	android: {
	},
	ios: {
		alert: "true",
		badge: true,
		sound: 'false'
	},
	windows: {}
});

push.on('registration', (data) => {
	console.log(data.registrationId);
});

push.on('notification', (data) => {
	console.log(data.message);
	console.log(data.title);
	console.log(data.count);
	console.log(data.sound);
	console.log(data.image);
	console.log(data.additionalData);
});

push.on('error', (e) => {
	console.log(e.message);
});
```

If you have custom attributes being sent from the server on the payload, you can define them on a custom interface extending the standard one:

```typescript
module my.custom {
	export interface NotificationEventResponse extends PhonegapPluginPush.NotificationEventResponse {
		additionalData: NotificationEventAdditionalData;
	}

	export interface NotificationEventAdditionalData extends PhonegapPluginPush.NotificationEventAdditionalData {
		bacon?: boolean;
	}
}

push.on('notification', (data: my.custom.NotificationEventResponse) => {
	//standard attributes
	console.log(data.message);
	console.log(data.title);
	console.log(data.count);
	console.log(data.sound);
	console.log(data.image);
	console.log(data.additionalData);

	//custom attributes
	console.log(data.additionalData.bacon);
});
```
