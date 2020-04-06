/* globals require */

/*!
 * Module dependencies.
 */

const cordova = require('./helper/cordova');
const PushNotification = require('../www/push');
let execSpy;
let execWin;
let options;

/*!
 * Specification.
 */

describe('phonegap-plugin-push', () => {
  beforeEach(() => {
    options = { android: {}, ios: {}, windows: {} };
    execWin = jasmine.createSpy();
    execSpy = spyOn(cordova.required, 'cordova/exec').and.callFake(execWin);
  });

  describe('PushNotification', () => {
    it('should exist', () => {
      expect(PushNotification).toBeDefined();
      expect(typeof PushNotification === 'object').toBe(true);
    });

    it('should contain a init function', () => {
      expect(PushNotification.init).toBeDefined();
      expect(typeof PushNotification.init === 'function').toBe(true);
    });

    it('should contain a hasPermission function', () => {
      expect(PushNotification.hasPermission).toBeDefined();
      expect(typeof PushNotification.hasPermission === 'function').toBe(true);
    });

    it('should contain a createChannel function', () => {
      expect(PushNotification.createChannel).toBeDefined();
      expect(typeof PushNotification.createChannel === 'function').toBe(true);
    });

    it('should contain a deleteChannel function', () => {
      expect(PushNotification.deleteChannel).toBeDefined();
      expect(typeof PushNotification.deleteChannel === 'function').toBe(true);
    });

    it('should contain a listChannels function', () => {
      expect(PushNotification.listChannels).toBeDefined();
      expect(typeof PushNotification.listChannels === 'function').toBe(true);
    });

    it('should contain a unregister function', () => {
      const push = PushNotification.init({});
      expect(push.unregister).toBeDefined();
      expect(typeof push.unregister === 'function').toBe(true);
    });

    it('should contain a getApplicationIconBadgeNumber function', () => {
      const push = PushNotification.init({});
      expect(push.getApplicationIconBadgeNumber).toBeDefined();
      expect(typeof push.getApplicationIconBadgeNumber === 'function').toBe(true);
    });

    it('should contain a setApplicationIconBadgeNumber function', () => {
      const push = PushNotification.init({});
      expect(push.setApplicationIconBadgeNumber).toBeDefined();
      expect(typeof push.setApplicationIconBadgeNumber === 'function').toBe(true);
    });

    it('should contain a clearAllNotifications function', () => {
      const push = PushNotification.init({});
      expect(push.clearAllNotifications).toBeDefined();
      expect(typeof push.clearAllNotifications === 'function').toBe(true);
    });

    it('should contain a clearNotification function', () => {
      const push = PushNotification.init({});
      expect(push.clearNotification).toBeDefined();
      expect(typeof push.clearNotification === 'function').toBe(true);
    });

    it('should contain a subscribe function', () => {
      const push = PushNotification.init({});
      expect(push.subscribe).toBeDefined();
      expect(typeof push.subscribe === 'function').toBe(true);
    });

    it('should contain a unsubscribe function', () => {
      const push = PushNotification.init({});
      expect(push.unsubscribe).toBeDefined();
      expect(typeof push.unsubscribe === 'function').toBe(true);
    });
  });

  describe('PushNotification instance', () => {
    describe('cordova.exec', () => {
      it('should call cordova.exec on next process tick', (done) => {
        PushNotification.init(options);
        setTimeout(() => {
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

    describe('on "registration" event', () => {
      it('should be emitted with an argument', (done) => {
        execSpy.and.callFake((win, fail, service, id, args) => {
          win({ registrationId: 1 });
        });
        const push = PushNotification.init(options);
        push.on('registration', (data) => {
          expect(data.registrationId).toEqual(1);
          done();
        });
      });
    });

    describe('on "notification" event', () => {
      beforeEach(() => {
        execSpy.and.callFake((win, fail, service, id, args) => {
          win({
            message: 'Message',
            title: 'Title',
            count: 1,
            sound: 'beep',
            image: 'Image',
            additionalData: {},
          });
        });
      });

      it('should be emitted on success', (done) => {
        const push = PushNotification.init(options);
        push.on('notification', (data) => {
          done();
        });
      });

      it('should provide the data.message argument', (done) => {
        const push = PushNotification.init(options);
        push.on('notification', (data) => {
          expect(data.message).toEqual('Message');
          done();
        });
      });

      it('should provide the data.title argument', (done) => {
        const push = PushNotification.init(options);
        push.on('notification', (data) => {
          expect(data.title).toEqual('Title');
          done();
        });
      });

      it('should provide the data.count argument', (done) => {
        const push = PushNotification.init(options);
        push.on('notification', (data) => {
          expect(data.count).toEqual(1);
          done();
        });
      });

      it('should provide the data.sound argument', (done) => {
        const push = PushNotification.init(options);
        push.on('notification', (data) => {
          expect(data.sound).toEqual('beep');
          done();
        });
      });

      it('should provide the data.image argument', (done) => {
        const push = PushNotification.init(options);
        push.on('notification', (data) => {
          expect(data.image).toEqual('Image');
          done();
        });
      });

      it('should provide the data.additionalData argument', (done) => {
        const push = PushNotification.init(options);
        push.on('notification', (data) => {
          expect(data.additionalData).toEqual({});
          done();
        });
      });
    });

    describe('on "error" event', () => {
      it('should be emitted with an Error', (done) => {
        execSpy.and.callFake((win, fail, service, id, args) => {
          fail('something went wrong');
        });
        const push = PushNotification.init(options);
        push.on('error', (e) => {
          expect(e).toEqual(jasmine.any(Error));
          expect(e.message).toEqual('something went wrong');
          done();
        });
      });
    });

    describe('off "notification" event', () => {
      it('should exist and be registered a callback handle', (done) => {
        const push = PushNotification.init(options),
          eventHandler = () => {};

        push.on('notification', eventHandler);

        push.off('notification', eventHandler);

        expect(push.handlers.notification.indexOf(eventHandler)).toEqual(-1);
        done();
      });
    });

    describe('off "registration" event', () => {
      it('should exist and be registered a callback handle', (done) => {
        const push = PushNotification.init(options),
          eventHandler = () => {};

        push.on('registration', eventHandler);

        push.off('registration', eventHandler);

        expect(push.handlers.registration.indexOf(eventHandler)).toEqual(-1);
        done();
      });
    });

    describe('off "error" event', () => {
      it('should exist and be registered a callback handle', (done) => {
        const push = PushNotification.init(options),
          eventHandler = () => {};

        push.on('error', eventHandler);
        push.off('error', eventHandler);

        expect(push.handlers.error.indexOf(eventHandler)).toEqual(-1);
        done();
      });
    });

    describe('unregister method', () => {
      it('should clear "registration" event handlers', (done) => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        expect(push.handlers.registration.length).toEqual(0);

        push.on('registration', eventHandler);

        expect(push.handlers.registration.length).toEqual(1);
        expect(push.handlers.registration.indexOf(eventHandler)).toBeGreaterThan(-1);

        execSpy.and.callFake((win, fail, service, id, args) => {
          win();
        });
        push.unregister(() => {
          expect(push.handlers.registration.length).toEqual(0);
          expect(push.handlers.registration.indexOf(eventHandler)).toEqual(-1);
          done();
        });
      });

      it('should clear "notification" event handlers', (done) => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        expect(push.handlers.notification.length).toEqual(0);

        push.on('notification', eventHandler);

        expect(push.handlers.notification.length).toEqual(1);
        expect(push.handlers.notification.indexOf(eventHandler)).toBeGreaterThan(-1);

        execSpy.and.callFake((win, fail, service, id, args) => {
          win();
        });
        push.unregister(() => {
          expect(push.handlers.notification.length).toEqual(0);
          expect(push.handlers.notification.indexOf(eventHandler)).toEqual(-1);
          done();
        });
      });

      it('should clear "error" event handlers', (done) => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        expect(push.handlers.error.length).toEqual(0);

        push.on('error', eventHandler);

        expect(push.handlers.error.length).toEqual(1);
        expect(push.handlers.error.indexOf(eventHandler)).toBeGreaterThan(-1);

        execSpy.and.callFake((win, fail, service, id, args) => {
          win();
        });
        push.unregister(() => {
          expect(push.handlers.error.length).toEqual(0);
          expect(push.handlers.error.indexOf(eventHandler)).toEqual(-1);
          done();
        });
      });
    });

    describe('unregister topics method', () => {
      it('should not clear "registration" event handlers', (done) => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        expect(push.handlers.registration.length).toEqual(0);

        push.on('registration', eventHandler);

        expect(push.handlers.registration.length).toEqual(1);
        expect(push.handlers.registration.indexOf(eventHandler)).toBeGreaterThan(-1);

        execSpy.and.callFake((win, fail, service, id, args) => {
          win();
        });
        push.unregister(
          () => {
            expect(push.handlers.registration.length).toEqual(1);
            expect(push.handlers.registration.indexOf(eventHandler)).toBeGreaterThan(-1);
            done();
          },
          () => {},
          ['foo', 'bar']
        );
      });

      it('should not clear "notification" event handlers', (done) => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        expect(push.handlers.notification.length).toEqual(0);

        push.on('notification', eventHandler);

        expect(push.handlers.notification.length).toEqual(1);
        expect(push.handlers.notification.indexOf(eventHandler)).toBeGreaterThan(-1);

        execSpy.and.callFake((win, fail, service, id, args) => {
          win();
        });
        push.unregister(
          () => {
            expect(push.handlers.notification.length).toEqual(1);
            expect(push.handlers.notification.indexOf(eventHandler)).toBeGreaterThan(-1);
            done();
          },
          () => {},
          ['foo', 'bar']
        );
      });

      it('should not clear "error" event handlers', (done) => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        expect(push.handlers.error.length).toEqual(0);

        push.on('error', eventHandler);

        expect(push.handlers.error.length).toEqual(1);
        expect(push.handlers.error.indexOf(eventHandler)).toBeGreaterThan(-1);

        execSpy.and.callFake((win, fail, service, id, args) => {
          win();
        });
        push.unregister(
          () => {
            expect(push.handlers.error.length).toEqual(1);
            expect(push.handlers.error.indexOf(eventHandler)).toBeGreaterThan(-1);
            done();
          },
          () => {},
          ['foo', 'bar']
        );
      });
    });

    describe('subscribe topic method', () => {
      describe('cordova.exec', () => {
        it('should call cordova.exec on next process tick', (done) => {
          const push = PushNotification.init(options);
          push.subscribe('foo', () => {}, () => {});
          setTimeout(() => {
            expect(execSpy).toHaveBeenCalledWith(
              jasmine.any(Function),
              jasmine.any(Function),
              'PushNotification',
              'subscribe',
              jasmine.any(Object)
            );
            done();
          }, 100);
        });
      });
    });

    describe('unsubscribe topic method', () => {
      describe('cordova.exec', () => {
        it('should call cordova.exec on next process tick', (done) => {
          const push = PushNotification.init(options);
          push.unsubscribe('foo', () => {}, () => {});
          setTimeout(() => {
            expect(execSpy).toHaveBeenCalledWith(
              jasmine.any(Function),
              jasmine.any(Function),
              'PushNotification',
              'unsubscribe',
              jasmine.any(Object)
            );
            done();
          }, 100);
        });
      });
    });

    describe('clear notification method', () => {
      describe('cordova.exec', () => {
        it('should call cordova.exec on next process tick using number argument', (done) => {
          const push = PushNotification.init(options);
          push.clearNotification(() => {}, () => {}, 145);
          setTimeout(() => {
            expect(execSpy).toHaveBeenCalledWith(
              jasmine.any(Function),
              jasmine.any(Function),
              'PushNotification',
              'clearNotification',
              [145]
            );
            done();
          }, 100);
        });

        it('should call cordova.exec on next process tick using string argument', (done) => {
          const push = PushNotification.init(options);
          push.clearNotification(() => {}, () => {}, '145');
          setTimeout(() => {
            expect(execSpy).toHaveBeenCalledWith(
              jasmine.any(Function),
              jasmine.any(Function),
              'PushNotification',
              'clearNotification',
              [145]
            );
            done();
          }, 100);
        });
      });
    });
  });
});
