/* globals cordova, PushNotification */

/*!
 * Module dependencies.
 */

var execSpy,
    options;

/*!
 * Specification.
 */
exports.defineAutoTests = function() {
    describe('additionalData callback argument', function () {
        beforeEach(function () {
            options = {android: {}, ios: {}, windows: {}};
            execSpy = spyOn(cordova, 'exec');
        });

        it('should point to a function accessible from window', function (done) {
            var result = {'additionalData': {'callback': 'a.b.fn'}};

            execSpy.and.callFake(function (win, fail, service, id, args) {
                win(result);
            });

            window.a = {b:{fn:jasmine.createSpy('function accessible from window')}};

            PushNotification.init(options);

            setTimeout(function () {
                expect(window.a.b.fn).toHaveBeenCalledWith(result);
                delete window.a;
                done();
            }, 100);
        });
    });
};
