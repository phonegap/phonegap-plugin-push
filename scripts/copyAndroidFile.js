module.exports = function(ctx) {
    var fs = ctx.requireCordovaModule('fs'),
        path = ctx.requireCordovaModule('path'),
        os = require("os"),
        readline = require("readline"),
        deferral = ctx.requireCordovaModule('q').defer();

    var lineReader = readline.createInterface({
        terminal: false,
        input : fs.createReadStream('platforms/android/build.gradle')
    });
    lineReader.on("line", function(line) {
        fs.appendFileSync('./build.gradle', line.toString() + os.EOL);
        if (/.*\ dependencies \{.*/.test(line)) {
            fs.appendFileSync('./build.gradle', '\t\tclasspath "com.google.gms:google-services:3.0.0"' + os.EOL);
        }
    }).on("close", function () {
        fs.rename('./build.gradle', 'platforms/android/build.gradle', deferral.resolve);
    });

    return deferral.promise;
};
