module.exports = function(context) {
    console.log('Updating appxmanifests with ToastCapable=true ...');
    var path = require('path');
    var fs = require('fs');

    var platformProjPath = path.join(context.opts.projectRoot, 'platforms/windows');
    if (!fs.existsSync(platformProjPath)) {
        platformProjPath = context.opts.projectRoot;
    }
    
    var AppxManifest = require(path.join(platformProjPath, 'cordova/lib/AppxManifest'));

    ['package.phone.appxmanifest', 'package.windows.appxmanifest'].forEach(function(manifestPath) {
        var manifest = AppxManifest.get(path.join(platformProjPath, manifestPath));
        manifest.getVisualElements().setToastCapable(true);
        manifest.write();
    });
}
