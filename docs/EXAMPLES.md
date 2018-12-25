# Creating a Project From a Template

If you want to get started with a sample project you can create a new project from the example template.

```
phonegap create my-app --template phonegap-template-push
```

## Quick Example

```javascript
const push = PushNotification.init({
	android: {
	},
	browser: {
		applicationServerKey: "..."
	},
	ios: {
		alert: "true",
		badge: "true",
		sound: "true"
	},
	windows: {}
});

push.on('registration', (data) => {
	// data.registrationType,
	// data.registrationId,
	// data.subscription (on browser platform) 
});

push.on('notification', (data) => {
	// data.message,
	// data.title,
	// data.count,
	// data.sound,
	// data.image,
	// data.additionalData
});

push.on('error', (e) => {
	// e.message
});
```
## Browser Web Push

Some users may wish to use `phonegap-plugin-push` to deliver push notifications to browsers via a 
[WebPush](https://tools.ietf.org/html/rfc8030) compliant service, such as:
 
 * [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/) (FCM) for  Chrome and Samsung browser
 * [Mozilla Cloud Services](https://support.mozilla.org/en-US/kb/push-notifications-firefox) (MCS)  for Mozilla Firefox
 * [Windows Push Notification Services](https://docs.microsoft.com/en-us/windows/uwp/design/shell/tiles-and-notifications/windows-push-notification-services--wns--overview) (WNS) for Microsoft Edge

The plumbing for WebPush is quite complex, so we'll walk through an example of how to set it up. We'll create the 
following baseline artifacts to spin up a basic WebPush service:

* **A VAPID public key:** to register our browser with the browser vendor's push platform (i.e. FCM, MCS, WNS)
* **A `PushNotification` `registration` event handler** to pass the vendor endpoint details to our web app after our browser has registered with the vendor's push platform
* **A service worker** to process incoming notifications from the vendor's push platform

We'll be using FCM to generate our VAPID key, but you could also get 
one elsewhere or generate your own - the approach is vendor neutral.

#### VAPID public key

VAPID stands for 
[_Voluntary Application Server Identification for Web Push_](https://datatracker.ietf.org/doc/draft-ietf-webpush-vapid/).
A _VAPID public key_ is a Base64 string containing an encryption key that the vendor push platform (FCM, MCS, WNS) will use to 
authenticate your application server. 

If you're using FCM you can generate a VAPID public key by browsing to your project home and 
selecting _Settings_ > _Cloud Messaging_ > _Web Configuration_ > _Web Push certificates_ and generating a key pair.

The Base64 string under the heading 'key pair' is what needs to go into your `PushNotification.init` configuration as 
the browser platform's `applicationServerKey`:
```
{
  ...
  browser: {
    applicationServerKey: '<VAPID PUBLIC KEY GOES HERE>'
  },
  ...
}
```

#### Registration event handler

Calling `PushNotification.init` from a browser when an `applicationServerKey` is provided will trigger an attempt to 
subscribe the browser instance to its vendor's push service (FCM for Chrome, MCS for Firefox, WNS for Edge).

If our attempt to register with the vendor's push platform is successful, it triggers a `registration` event from 
`PushNotification`. The payload for the `registration` event is a dict that describes our subscription endpoint on the vendor's push
platform. It will look similar to the dicts you receive for iOS and Android, but with an additional `subscription` 
element. For example:
```
{
  "registrationType": "WEB_PUSH",
  "registrationId": "blahblahblah",
  "subscription": { 
    "endpoint": "https://fcm.googleapis.com/fcm/send/blahblah:blahblahblah",
    "expirationTime":null,
    "keys": {
      "p256dh": "blahblahblah"
    }
  }
}
```
Our server-side code needs this data to send a notification to that user. We can post it to our server API when the 
`registration` event fires:
```
const push = PushNotification.init({
  // Your push configuration
});

push.on('registration', regData => {
  ...
  axios('https://api.myservice.com/webpush/subscriptions', {
    headers: {' content-type': 'application/json' },
    method: 'POST',
    data: regData,
  })
    .then(resp => ...)
    .catch(err => ...);
  ...
}));
```
We can then store the user's subscription data on our server along with the user's ID, and retrieve it when we want to push 
a notification to that user. 

Pushing a notification from our server to the user becomes straightforward, especially 
using a WebPush library like [web-push](https://github.com/web-push-libs/web-push). 

For example, using Node.js:
```
const webPush = require('web-push');

const data = <data object posted from client>

await webPush.sendNotification(data.subscription, "This is a test");
```
The above code pushes a simple string notification to a specific browser subscription. However, the browser does not 
yet know what to do with our notifications, so we need to create a service worker that can handle them.

#### Service Worker

To process notifications, we must provide an event handler for `push` events in our app's
[service worker](https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API) (see how to create a service 
worker [here](https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API/Using_Service_Workers)).
 
Here is an example handler that assumes your push payload is a simple string (as sent in the earlier server-side example) and displays it as a standard notification:
```
...
self.addEventListener('push', function (event) {
  console.log(event.data);
  event.waitUntil(self.registration.showNotification(event.data));
});
...
``` 
See your browser's `push` documentation for more details and more complex examples.

Once you've implemented the above patterns you should be able to receive notifications in Chrome-based browsers, Mozilla Firefox and Microsoft Edge.
