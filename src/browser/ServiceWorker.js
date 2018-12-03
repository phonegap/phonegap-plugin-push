var messageChannel;

self.addEventListener('install', function(event) {
    self.skipWaiting();
});

self.addEventListener('push', function(event) {
    // parse incoming message
    var obj = {};
    var pushData = {
        image: 'https://avatars1.githubusercontent.com/u/60365?v=3&s=200',
        additionalData: {}
    };
    if (event.data) {
        obj = event.data.json();
    }

    console.log(obj);

    // convert to push plugin API
    for (var key in obj) {
        if (key === 'title') {
            pushData.title = obj[key];
        } else if (key === 'message' || key === 'body') {
            pushData.message = obj[key];
        } else if (key === 'count' || key === 'msgcnt' || key === 'badge') {
            pushData.count = obj[key];
        } else if (key === 'sound' || key === 'soundname') {
            pushData.sound = obj[key];
        } else if (key === 'image') {
            pushData.image = obj[key];
        } else {
            pushData.additionalData[key] = obj[key];
        }
    }

    event.waitUntil(
        self.registration.showNotification(pushData.title, {
            body: pushData.message,
            icon: pushData.image,
            tag: 'simple-push-demo-notification-tag'
        })
    );

    messageChannel.ports[0].postMessage(pushData);

});

self.addEventListener('message', function(event) {
    messageChannel = event;
});
