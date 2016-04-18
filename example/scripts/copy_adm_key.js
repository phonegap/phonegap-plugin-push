var ncp = require('ncp').ncp;

module.exports = function(context) {

  var Q = context.requireCordovaModule('q');
  var deferral = new Q.defer();

  ncp.limit = 16;
  var source = __dirname + '/../../../deploy/amazon/adm_api_key.txt';
  var destination = __dirname + '/../platforms/android/assets/api_key.txt';

  ncp(source, destination, function (err) {
    if (err) {
      console.error(err);
    }
    console.log('Copying ADM api key...');
    deferral.resolve();
  });

  return deferral.promise;
}
