module.exports = function (context) {
  console.log('Updating manifest.json with push properties…');
  var path = require('path');
  var fs = require('fs');

  var platformProjPath = path.join(
    context.opts.projectRoot,
    'platforms/browser'
  );

  if (!fs.existsSync(platformProjPath)) {
    platformProjPath = context.opts.projectRoot;
  }

  var platformManifestJson = path.join(platformProjPath, 'www/manifest.json');

  if (!fs.existsSync(platformManifestJson)) {
    return;
  }

  fs.readFile(platformManifestJson, 'utf8', function (err, platformJson) {
    if (err) throw err; // we'll not consider error handling for now
    var platformManifest = JSON.parse(platformJson);

    var pluginManifestPath = path.join(
      context.opts.projectRoot,
      'plugins/cordova-plugin-push/src/browser/manifest.json'
    );

    fs.readFile(pluginManifestPath, 'utf8', function (err, pluginJson) {
      if (err) throw err; // we'll not consider error handling for now
      var pluginManifest = JSON.parse(pluginJson);

      platformManifest.gcm_sender_id = pluginManifest.gcm_sender_id;

      fs.writeFile(
        platformManifestJson,
        JSON.stringify(platformManifest),
        function (err) {
          if (err) {
            return console.log(err);
          }

          console.log('Manifest updated with push sender ID');
        }
      );
    });
  });
};
