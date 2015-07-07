var myApp = {};
var pushNotifications = Windows.Networking.PushNotifications;

var createNotificationJSON = function (e) {
    var result = {};
    var notificationPayload;

    switch (e.notificationType) {
        case pushNotifications.PushNotificationType.toast:
        notificationPayload = e.toastNotification.content.getXml();
        break;

        case pushNotifications.PushNotificationType.tile:
        notificationPayload = e.tileNotification.content.getXml();
        break;

        case pushNotifications.PushNotificationType.badge:
        notificationPayload = e.badgeNotification.content.getXml();
        break;

        case pushNotifications.PushNotificationType.raw:
        notificationPayload = e.rawNotification.content;
        break;
    }
    result.message = "";
    result.xmlContent = notificationPayload;
    result.objectReference = e;
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


