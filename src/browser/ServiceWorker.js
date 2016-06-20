var messageChannel;

self.addEventListener('install', function(event) {
    self.skipWaiting();
});

self.addEventListener('push', function(event) {
    // parse incoming message
    var obj = {};
    if (event.data) {
        obj = event.data.json();
    }

    // Need to figure out a way to make these configurable
    var title = obj.title || 'Default title';
    var body = obj.body || 'This is the default body';
    var icon = 'https://avatars1.githubusercontent.com/u/60365?v=3&s=200';
    var tag = 'simple-push-demo-notification-tag';

    event.waitUntil(
        self.registration.showNotification(title, {
            body: body,
            icon: icon,
            tag: tag
        })
    );

    var pushData = {
        title: title,
        message: body,
        count: 1,
        sound: 'default',
        additionalData: {}
    };
    messageChannel.ports[0].postMessage(pushData);

});

self.addEventListener('message', function(event) {
    messageChannel = event;
});
