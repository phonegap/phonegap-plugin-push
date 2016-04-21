/*global require, module, __dirname, console*/

var insertAfter = require('./utils/insertAfter');

var gradleFile = __dirname + '/../platforms/android/build.gradle';
var marker = "SUB-PROJECT DEPENDENCIES END\n";

module.exports = function(context) {
  console.log('Adding amazon device messaging jar as provided dependency...');
  insertAfter(gradleFile,
              marker,
              "    provided files('ext_libs/amazon-device-messaging-1.0.1.jar')\n"
  );
};
