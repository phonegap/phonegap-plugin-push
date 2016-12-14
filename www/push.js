/* global cordova:false */
/* globals window */

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

    // triggered on registration and notification
    var that = this;
    var success = function(result) {
        if (result && typeof result.registrationId !== 'undefined') {
            that.emit('registration', result);
        } else if (result && result.additionalData && typeof result.additionalData.actionCallback !== 'undefined') {
            var executeFuctionOrEmitEventByName = function(callbackName, context, arg) {
              var namespaces = callbackName.split('.');
              var func = namespaces.pop();
              for (var i = 0; i < namespaces.length; i++) {
                context = context[namespaces[i]];
              }

              if (typeof context[func] === 'function') {
                context[func].call(context, arg);
              } else {
                that.emit(callbackName, arg);
              }
            };

            executeFuctionOrEmitEventByName(result.additionalData.actionCallback, window, result);
        } else if (result) {
            that.emit('notification', result);
        }
    };

    // triggered on error
    var fail = function(msg) {
        var e = (typeof msg === 'string') ? new Error(msg) : msg;
        that.emit('error', e);
    };

    // wait at least one process tick to allow event subscriptions
    setTimeout(function() {
        exec(success, fail, 'PushNotification', 'init', [options]);
    }, 10);
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
    var cleanHandlersAndPassThrough = function() {
        if (!options) {
            that._handlers = {
                'registration': [],
                'notification': [],
                'error': []
            };
        }
        successCallback();
    };

    exec(cleanHandlersAndPassThrough, errorCallback, 'PushNotification', 'unregister', [options]);
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

    exec(successCallback, errorCallback, 'PushNotification', 'subscribe', [topic]);
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

    exec(successCallback, errorCallback, 'PushNotification', 'unsubscribe', [topic]);
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

    exec(successCallback, errorCallback, 'PushNotification', 'setApplicationIconBadgeNumber', [{badge: badge}]);
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

    exec(successCallback, errorCallback, 'PushNotification', 'getApplicationIconBadgeNumber', []);
};

/**
 * Get the application icon badge
 */

PushNotification.prototype.clearAllNotifications = function(successCallback, errorCallback) {
    if (!successCallback) { successCallback = function() {}; }
    if (!errorCallback) { errorCallback = function() {}; }

    if (typeof errorCallback !== 'function')  {
        console.log('PushNotification.clearAllNotifications failure: failure parameter not a function');
        return;
    }

    if (typeof successCallback !== 'function') {
        console.log('PushNotification.clearAllNotifications failure: success callback parameter must be a function');
        return;
    }

    exec(successCallback, errorCallback, 'PushNotification', 'clearAllNotifications', []);
};

/**
 * Listen for an event.
 *
 * Any event is supported, but the following are built-in:
 *
 *   - registration
 *   - notification
 *   - error
 *
 * @param {String} eventName to subscribe to.
 * @param {Function} callback triggered on the event.
 */

PushNotification.prototype.on = function(eventName, callback) {
    if (!this._handlers.hasOwnProperty(eventName)) {
        this._handlers[eventName] = [];
    }
    this._handlers[eventName].push(callback);
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

    exec(successCallback, errorCallback, 'PushNotification', 'finish', [id]);
};

/*!
 * Push Notification Plugin.
 */

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
        exec(successCallback, errorCallback, 'PushNotification', 'hasPermission', []);
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
