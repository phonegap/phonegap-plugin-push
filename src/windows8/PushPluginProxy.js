var myApp = {};
var pushNotifications = Windows.Networking.PushNotifications;

var createNotificationJSON = function (e) {
    var result = {};
    var notificationPayload;

    switch (e.notificationType) {
        case pushNotifications.PushNotificationType.toast:
            notificationPayload = e.toastNotification.content;
            break;

        case pushNotifications.PushNotificationType.tile:
            notificationPayload = e.tileNotification.content;
            break;

        case pushNotifications.PushNotificationType.badge:
            notificationPayload = e.badgeNotification.content;
            result.message = '';
            result.count = notificationPayload.getElementsByTagName("badge")[0].getAttribute("value");
            break;

        case pushNotifications.PushNotificationType.raw:
            result.message = e.rawNotification.content;
            break;
    }
    
    if (e.notificationType === pushNotifications.PushNotificationType.toast || e.notificationType === pushNotifications.PushNotificationType.tile) {
        var texts = notificationPayload.getElementsByTagName("text");
        if (texts.length > 1) {
            result.title = texts[0].innerText;
            result.message = texts[1].innerText;
        }
        else {
            result.message = texts[0].innerText;
        }
        var images = notificationPayload.getElementsByTagName("image");
        if (images.length > 0) {
            result.image = images[0].getAttribute("src");
        }
        var soundFile = notificationPayload.getElementsByTagName("audio");
        if (soundFile.length > 0) {
            result.sound = soundFile[0].getAttribute("src");
        }
    }
    result.additionalData = {};
    result.additionalData.objectReference = e;
    return result;
}

module.exports = {
    init: function (onSuccess, onFail, args) {

        var onNotificationReceived = function (e) {
            var result = createNotificationJSON(e);
            onSuccess(result, { keepCallback: true });
        }

        try {
            pushNotifications.PushNotificationChannelManager.createPushNotificationChannelForApplicationAsync().done(
                function (channel) {
                    var result = {};
                    result.registrationId = channel.uri;
                    myApp.channel = channel;
                    channel.addEventListener("pushnotificationreceived", onNotificationReceived);
                    myApp.notificationEvent = onNotificationReceived;
                    onSuccess(result, { keepCallback: true });
                }, function (error) {
                    onFail(error);
                });
        } catch (ex) {
            onFail(ex);
        }
    },
    unregister: function (onSuccess, onFail, args) {
        try {
            myApp.channel.removeEventListener("pushnotificationreceived", myApp.notificationEvent);
            myApp.channel.close();
            onSuccess();
        } catch(ex) {
            onFail(ex);
        }
    }
};
require("cordova/exec/proxy").add("PushNotification", module.exports);


