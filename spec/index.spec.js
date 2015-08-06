/*!
 * Module dependencies.
 */

var cordova = require('./helper/cordova'),
    PushNotification = require('../www/push'),
    execSpy,
    execWin,
    options;

/*!
 * Specification.
 */

describe('phonegap-plugin-push', function() {
    beforeEach(function() {
        execWin = jasmine.createSpy();
        execSpy = spyOn(cordova.required, 'cordova/exec').andCallFake(execWin);
    });

    describe('PushNotification', function() {
        it("should exist", function() {
            expect(PushNotification).toBeDefined();
            expect(typeof PushNotification == 'object').toBe(true);
        });

        it("should contain a init function", function() {
            expect(PushNotification.init).toBeDefined();
            expect(typeof PushNotification.init == 'function').toBe(true);
        });

        it("should contain a unregister function", function() {
            var push = PushNotification.init({});
            expect(push.unregister).toBeDefined();
            expect(typeof push.unregister == 'function').toBe(true);
        });

        it("should contain a setApplicationIconBadgeNumber function", function() {
            var push = PushNotification.init({});
            expect(push.setApplicationIconBadgeNumber).toBeDefined();
            expect(typeof push.setApplicationIconBadgeNumber == 'function').toBe(true);
        });
    });
});
