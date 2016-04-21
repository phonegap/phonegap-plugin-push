/*global require, module, __dirname, console*/

var insertAfter = require('./utils/insertAfter');

var manifest = __dirname + '/../platforms/android/AndroidManifest.xml';

module.exports = function(context) {
  console.log('Adding amazon xmlns to AndroidManifest.xml...');
  insertAfter(manifest,
              '<manifest ',
              'xmlns:amazon="http://schemas.amazon.com/apk/res/android" '
  );
};
