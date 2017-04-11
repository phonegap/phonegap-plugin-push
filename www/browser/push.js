/* global cordova:false */
/* globals window, document, navigator */

/*!
 * Module dependencies.
 */

var exec = cordova.require('cordova/exec');

/**
 * PushNotification constructor.
 *
 * @param {Object} options to initiate Push Notifications.
 * @return {PushNotification} instance that can be monitored and cancelled.
 */
var serviceWorker, subscription;
var PushNotification = function(options) {
    this._handlers = {
        'registration': [],
        'notification': [],
        'error': []
    };

    // require options parameter
    if (typeof options === 'undefined') {
        throw new Error('The options argument is required.');
    }

    // store the options to this object instance
    this.options = options;

    // subscription options
    var subOptions = {userVisibleOnly: true};
    if (this.options.browser.applicationServerKey) {
        subOptions.applicationServerKey = urlBase64ToUint8Array(this.options.browser.applicationServerKey);
    }

    // triggered on registration and notification
    var that = this;

    // Add manifest.json to main HTML file
    var linkElement = document.createElement('link');
    linkElement.rel = 'manifest';
    linkElement.href = 'manifest.json';
    document.getElementsByTagName('head')[0].appendChild(linkElement);

    if ('serviceWorker' in navigator && 'MessageChannel' in window) {
        var result;
        var channel = new MessageChannel();
        channel.port1.onmessage = function(event) {
            that.emit('notification', event.data);
        };

        navigator.serviceWorker.register('ServiceWorker.js').then(function() {
            return navigator.serviceWorker.ready;
        })
        .then(function(reg) {
            serviceWorker = reg;
            reg.pushManager.subscribe(subOptions).then(function(sub) {
                subscription = sub;
                result = { 'registrationId': sub.endpoint.substring(sub.endpoint.lastIndexOf('/') + 1) };
                that.emit('registration', result);

                // send encryption keys to push server
                var xmlHttp = new XMLHttpRequest();
                var xmlURL = (options.browser.pushServiceURL || 'http://push.api.phonegap.com/v1/push') + '/keys';
                xmlHttp.open('POST', xmlURL, true);

                var formData = new FormData();
                formData.append('subscription', JSON.stringify(sub));

                xmlHttp.send(formData);

                navigator.serviceWorker.controller.postMessage(result, [channel.port2]);
            }).catch(function(error) {
                if (navigator.serviceWorker.controller === null) {
                    // When you first register a SW, need a page reload to handle network operations
                    window.location.reload();
                    return;
                }

                throw new Error('Error subscribing for Push notifications.');
            });
        }).catch(function(error) {
            console.log(error);
            throw new Error('Error registering Service Worker');
        });
    } else {
        throw new Error('Service Workers are not supported on your browser.');
    }
};

/**
 * Unregister from push notifications
 */

PushNotification.prototype.unregister = function(successCallback, errorCallback, options) {
    if (!errorCallback) { errorCallback = function() {}; }

    if (typeof errorCallback !== 'function')  {
        console.log('PushNotification.unregister failure: failure parameter not a function');
        return;
    }

    if (typeof successCallback !== 'function') {
        console.log('PushNotification.unregister failure: success callback parameter must be a function');
        return;
    }

    var that = this;
    if (!options) {
        that._handlers = {
            'registration': [],
            'notification': [],
            'error': []
        };
    }

    if (serviceWorker) {
        serviceWorker.unregister().then(function(isSuccess) {
            if (isSuccess) {
                var deviceID = subscription.endpoint.substring(subscription.endpoint.lastIndexOf('/') + 1);
                var xmlHttp = new XMLHttpRequest();
                var xmlURL = (that.options.browser.pushServiceURL || 'http://push.api.phonegap.com/v1/push')
                    + '/keys/' + deviceID;
                xmlHttp.open('DELETE', xmlURL, true);
                xmlHttp.send();

                successCallback();
            } else {
                errorCallback();
            }
        });
    }
};

/**
 * subscribe to a topic
 * @param   {String}      topic               topic to subscribe
 * @param   {Function}    successCallback     success callback
 * @param   {Function}    errorCallback       error callback
 * @return  {void}
 */
PushNotification.prototype.subscribe = function(topic, successCallback, errorCallback) {
    if (!errorCallback) { errorCallback = function() {}; }

    if (typeof errorCallback !== 'function')  {
        console.log('PushNotification.subscribe failure: failure parameter not a function');
        return;
    }

    if (typeof successCallback !== 'function') {
        console.log('PushNotification.subscribe failure: success callback parameter must be a function');
        return;
    }

    successCallback();
};

/**
 * unsubscribe to a topic
 * @param   {String}      topic               topic to unsubscribe
 * @param   {Function}    successCallback     success callback
 * @param   {Function}    errorCallback       error callback
 * @return  {void}
 */
PushNotification.prototype.unsubscribe = function(topic, successCallback, errorCallback) {
    if (!errorCallback) { errorCallback = function() {}; }

    if (typeof errorCallback !== 'function')  {
        console.log('PushNotification.unsubscribe failure: failure parameter not a function');
        return;
    }

    if (typeof successCallback !== 'function') {
        console.log('PushNotification.unsubscribe failure: success callback parameter must be a function');
        return;
    }

    successCallback();
};

/**
 * Call this to set the application icon badge
 */

PushNotification.prototype.setApplicationIconBadgeNumber = function(successCallback, errorCallback, badge) {
    if (!errorCallback) { errorCallback = function() {}; }

    if (typeof errorCallback !== 'function')  {
        console.log('PushNotification.setApplicationIconBadgeNumber failure: failure parameter not a function');
        return;
    }

    if (typeof successCallback !== 'function') {
        console.log('PushNotification.setApplicationIconBadgeNumber failure: success callback parameter must be a function');
        return;
    }

    successCallback();
};

/**
 * Get the application icon badge
 */

PushNotification.prototype.getApplicationIconBadgeNumber = function(successCallback, errorCallback) {
    if (!errorCallback) { errorCallback = function() {}; }

    if (typeof errorCallback !== 'function')  {
        console.log('PushNotification.getApplicationIconBadgeNumber failure: failure parameter not a function');
        return;
    }

    if (typeof successCallback !== 'function') {
        console.log('PushNotification.getApplicationIconBadgeNumber failure: success callback parameter must be a function');
        return;
    }

    successCallback();
};

/**
 * Get the application icon badge
 */

PushNotification.prototype.clearAllNotifications = function(successCallback, errorCallback) {
    if (!errorCallback) { errorCallback = function() {}; }

    if (typeof errorCallback !== 'function')  {
        console.log('PushNotification.clearAllNotifications failure: failure parameter not a function');
        return;
    }

    if (typeof successCallback !== 'function') {
        console.log('PushNotification.clearAllNotifications failure: success callback parameter must be a function');
        return;
    }

    successCallback();
};

/**
 * Listen for an event.
 *
 * The following events are supported:
 *
 *   - registration
 *   - notification
 *   - error
 *
 * @param {String} eventName to subscribe to.
 * @param {Function} callback triggered on the event.
 */

PushNotification.prototype.on = function(eventName, callback) {
    if (this._handlers.hasOwnProperty(eventName)) {
        this._handlers[eventName].push(callback);
    }
};

/**
 * Remove event listener.
 *
 * @param {String} eventName to match subscription.
 * @param {Function} handle function associated with event.
 */

PushNotification.prototype.off = function (eventName, handle) {
    if (this._handlers.hasOwnProperty(eventName)) {
        var handleIndex = this._handlers[eventName].indexOf(handle);
        if (handleIndex >= 0) {
            this._handlers[eventName].splice(handleIndex, 1);
        }
    }
};

/**
 * Emit an event.
 *
 * This is intended for internal use only.
 *
 * @param {String} eventName is the event to trigger.
 * @param {*} all arguments are passed to the event listeners.
 *
 * @return {Boolean} is true when the event is triggered otherwise false.
 */

PushNotification.prototype.emit = function() {
    var args = Array.prototype.slice.call(arguments);
    var eventName = args.shift();

    if (!this._handlers.hasOwnProperty(eventName)) {
        return false;
    }

    for (var i = 0, length = this._handlers[eventName].length; i < length; i++) {
        var callback = this._handlers[eventName][i];
        if (typeof callback === 'function') {
            callback.apply(undefined,args);
        } else {
            console.log('event handler: ' + eventName + ' must be a function');
        }
    }

    return true;
};

PushNotification.prototype.finish = function(successCallback, errorCallback, id) {
    if (!successCallback) { successCallback = function() {}; }
    if (!errorCallback) { errorCallback = function() {}; }
    if (!id) { id = 'handler'; }

    if (typeof successCallback !== 'function') {
        console.log('finish failure: success callback parameter must be a function');
        return;
    }

    if (typeof errorCallback !== 'function')  {
        console.log('finish failure: failure parameter not a function');
        return;
    }

    successCallback();
};

/*!
 * Push Notification Plugin.
 */

/**
 * Converts the server key to an Uint8Array
 *
 * @param base64String
 *
 * @returns {Uint8Array}
 */
function urlBase64ToUint8Array(base64String) {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding)
        .replace(/\-/g, '+')
        .replace(/_/g, '/');

    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);

    for (var i = 0; i < rawData.length; ++i) {
        outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
}


module.exports = {
    /**
     * Register for Push Notifications.
     *
     * This method will instantiate a new copy of the PushNotification object
     * and start the registration process.
     *
     * @param {Object} options
     * @return {PushNotification} instance
     */

    init: function(options) {
        return new PushNotification(options);
    },

    hasPermission: function(successCallback, errorCallback) {
        successCallback(true);
    },

    unregister: function(successCallback, errorCallback, options) {
        PushNotification.unregister(successCallback, errorCallback, options);
    },

    /**
     * PushNotification Object.
     *
     * Expose the PushNotification object for direct use
     * and testing. Typically, you should use the
     * .init helper method.
     */

    PushNotification: PushNotification
};
