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
        options = { android: {}, ios: {}, windows: {} };
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

        it("should contain a getApplicationIconBadgeNumber function", function() {
            var push = PushNotification.init({});
            expect(push.getApplicationIconBadgeNumber).toBeDefined();
            expect(typeof push.getApplicationIconBadgeNumber == 'function').toBe(true);
        });

        it("should contain a setApplicationIconBadgeNumber function", function() {
            var push = PushNotification.init({});
            expect(push.setApplicationIconBadgeNumber).toBeDefined();
            expect(typeof push.setApplicationIconBadgeNumber == 'function').toBe(true);
        });
    });
    
    describe('PushNotification instance', function() {
        describe('cordova.exec', function() {
            it('should call cordova.exec on next process tick', function(done) {
                PushNotification.init(options);
                setTimeout(function() {
                    expect(execSpy).toHaveBeenCalledWith(
                        jasmine.any(Function),
                        jasmine.any(Function),
                        'PushNotification',
                        'init',
                        jasmine.any(Object)
                    );
                    done();
                }, 100);
            });
        });

        describe('on "registration" event', function() {
            it('should be emitted with an argument', function(done) {
                execSpy.andCallFake(function(win, fail, service, id, args) {
                    win({ 'registrationId': 1 });
                });
                var push = PushNotification.init(options);
                push.on('registration', function(data) {
                    expect(data.registrationId).toEqual(1);
                    done();
                });
            });
        });

        describe('on "notification" event', function() {
            beforeEach(function() {
                execSpy.andCallFake(function(win, fail, service, id, args) {
                    win({
                        message: 'Message',
                        title: 'Title',
                        count: 1,
                        sound: 'beep',
                        image: 'Image',
                        additionalData: {}
                    });
                });
            });

            it('should be emitted on success', function(done) {
                var push = PushNotification.init(options);
                push.on('notification', function(data) {
                    done();
                });
            });

            it('should provide the data.message argument', function(done) {
                var push = PushNotification.init(options);
                push.on('notification', function(data) {
                    expect(data.message).toEqual('Message');
                    done();
                });
            });

            it('should provide the data.title argument', function(done) {
                var push = PushNotification.init(options);
                push.on('notification', function(data) {
                    expect(data.title).toEqual('Title');
                    done();
                });
            });

            it('should provide the data.count argument', function(done) {
                var push = PushNotification.init(options);
                push.on('notification', function(data) {
                    expect(data.count).toEqual(1);
                    done();
                });
            });

            it('should provide the data.sound argument', function(done) {
                var push = PushNotification.init(options);
                push.on('notification', function(data) {
                    expect(data.sound).toEqual('beep');
                    done();
                });
            });

            it('should provide the data.image argument', function(done) {
                var push = PushNotification.init(options);
                push.on('notification', function(data) {
                    expect(data.image).toEqual('Image');
                    done();
                });
            });

            it('should provide the data.additionalData argument', function(done) {
                var push = PushNotification.init(options);
                push.on('notification', function(data) {
                    expect(data.additionalData).toEqual({});
                    done();
                });
            });
        });

        describe('on "error" event', function() {
            it('should be emitted with an Error', function(done) {
                execSpy.andCallFake(function(win, fail, service, id, args) {
                    fail('something went wrong');
                });
                var push = PushNotification.init(options);
                push.on('error', function(e) {
                    expect(e).toEqual(jasmine.any(Error));
                    expect(e.message).toEqual('something went wrong');
                    done();
                });
            });
        });
    });
});
