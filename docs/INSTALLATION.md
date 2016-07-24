# Installation

- [Android details](#android-details)
  - [Compilation](#compilation)
  - [Co-existing with Facebook Plugin](#co-existing-with-facebook-plugin)
  - [Common errors](#common-errors)
    - [minSdkVersion === 14](#minsdkversion--14)
	- [Multidex](#multidex)
	- [More than one library with package name 'com.google.android.gms'](#more-than-one-library-with-package-name-comgoogleandroidgms)
- [Browser details](#browser-details)
  - [Browser quirks](#browser-quirks)
  - [Browser Support](#browser-support)
- [iOS details](#ios-details)
  - [XCode](#xcode)
  - [Bitcode](#bitcode)
- [Additional Resources](#additional-resources)

This requires phonegap/cordova CLI 5.0+

```
phonegap plugin add phonegap-plugin-push --variable SENDER_ID="XXXXXXX"
```
or

```
cordova plugin add phonegap-plugin-push --variable SENDER_ID="XXXXXXX"
```

It is also possible to install via repo url directly ( unstable )

```
phonegap plugin add https://github.com/phonegap/phonegap-plugin-push --variable SENDER_ID="XXXXXXX"
```

or

```
cordova plugin add https://github.com/phonegap/phonegap-plugin-push --variable SENDER_ID="XXXXXXX"
```

Where the `XXXXXXX` in `SENDER_ID="XXXXXXX"` maps to the project number in the [Google Developer Console](https://www.google.ca/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0ahUKEwikqt3nyPjMAhXJ5iYKHR0qDcsQFggbMAA&url=https%3A%2F%2Fconsole.developers.google.com%2F&usg=AFQjCNF0eH059mv86nMIlRmfsf42kde-wA&sig2=BQ2BJpchw1CpGt87sk5p6w&bvm=bv.122852650,d.eWE). To find the project number login to the Google Developer Console, select your project and click the menu item in the screen shot below to display your project number.

![zzns8](https://cloud.githubusercontent.com/assets/353180/15588897/2fc14db2-235e-11e6-9326-f97fe0ec15ab.png)

If you are not creating an Android application you can put in anything for this value.

> Note: if you are using ionic you may need to specify the SENDER_ID variable in your package.json.

```
  "cordovaPlugins": [
    {
      "variables": {
        "SENDER_ID": "XXXXXXX"
      },
      "locator": "phonegap-plugin-push"
    }
  ]
```

> Note: You need to specify the SENDER_ID variable in your config.xml if you plan on installing/restoring plugins using the prepare method.  The prepare method will skip installing the plugin otherwise.

```
<plugin name="phonegap-plugin-push" spec="1.6.0">
    <param name="SENDER_ID" value="XXXXXXX" />
</plugin>
```

## Android details

### Compilation

As of version 1.3.0 the plugin has been switched to using Gradle/Maven for building.

You will need to ensure that you have installed the following items through the Android SDK Manager:

- Android Support Library version 23 or greater
- Local Maven repository for Support Libraries (formerly Android Support Repository) version 20 or greater
- Google Play Services version 27 or greater
- Google Repository version 22 or greater

![android support library](https://cloud.githubusercontent.com/assets/353180/10230226/0627931e-684a-11e5-9a6b-72d72997f655.png)

For more detailed instructions on how to install the Android Support Library visit [Google's documentation](https://developer.android.com/tools/support-library/setup.html).

*Note:* if you are using an IDE to like Eclipse, Xamarin, etc. then the Android SDK installed by those tools may not be the same version as the one used by the Cordova/PhoneGap CLI while building. Please make sure your command line tooling is up to date with the software versions above. An easy way to make sure you up to date is to run the following command:

```
android update sdk --no-ui --filter "extra"
```

### Co-existing with Facebook Plugin

There are a number of Cordova Facebook Plugins available but the one that we recommend is [Jeduan's fork](https://github.com/jeduan/cordova-plugin-facebook4) of the original Wizcorp plugin. It is setup to use Gradle/Maven and the latest Facebook SDK properly.

To add to your app:

```
phonegap plugin add --save cordova-plugin-facebook4 --variable APP_ID="App ID" --variable APP_NAME="App Name"
```
or

```
cordova plugin add --save cordova-plugin-facebook4 --variable APP_ID="App ID" --variable APP_NAME="App Name"
```

### Common errors

#### minSdkVersion === 14

If you have an issue compiling the app and you are getting an error similar to this:

```
* What went wrong:
Execution failed for task ':processDebugManifest'.
> Manifest merger failed : uses-sdk:minSdkVersion 14 cannot be smaller than version 15 declared in library .../platforms/android/build/intermediates/exploded-aar/com.facebook.android/facebook-android-sdk/4.6.0/AndroidManifest.xml
  	Suggestion: use tools:overrideLibrary="com.facebook" to force usage
```

Then you can add the following entry into your config.xml file in the android platform tag:

```xml
<platform name="android">
    <preference name="android-minSdkVersion" value="15"/>
 </platform>
```

or compile your project using the following command, if the solution above doesn't work for you. Basically add `-- --minSdkVersion=15` to the end of the command line (mind the extra `--`, it's needed):

```bash
cordova compile android -- --minSdkVersion=15
cordova build android -- --minSdkVersion=15
cordova run android -- --minSdkVersion=15
cordova emulate android -- --minSdkVersion=15
```

#### Multidex

If you have an issue compiling the app and you're getting an error similar to this (`com.android.dex.DexException: Multiple dex files define`):

```
UNEXPECTED TOP-LEVEL EXCEPTION:
com.android.dex.DexException: Multiple dex files define Landroid/support/annotation/AnimRes;
	at com.android.dx.merge.DexMerger.readSortableTypes(DexMerger.java:596)
	at com.android.dx.merge.DexMerger.getSortedTypes(DexMerger.java:554)
	at com.android.dx.merge.DexMerger.mergeClassDefs(DexMerger.java:535)
	at com.android.dx.merge.DexMerger.mergeDexes(DexMerger.java:171)
	at com.android.dx.merge.DexMerger.merge(DexMerger.java:189)
	at com.android.dx.command.dexer.Main.mergeLibraryDexBuffers(Main.java:502)
	at com.android.dx.command.dexer.Main.runMonoDex(Main.java:334)
	at com.android.dx.command.dexer.Main.run(Main.java:277)
	at com.android.dx.command.dexer.Main.main(Main.java:245)
	at com.android.dx.command.Main.main(Main.java:106)
```

Then at least one other plugin you have installed is using an outdated way to declare dependencies such as `android-support` or `play-services-gcm`.
This causes gradle to fail, and you'll need to identify which plugin is causing it and request an update to the plugin author, so that it uses the proper way to declare dependencies for cordova.
See [this for the reference on the cordova plugin specification](https://cordova.apache.org/docs/en/5.4.0/plugin_ref/spec.html#link-18), it'll be usefull to mention it when creating an issue or requesting that plugin to be updated.

Common plugins to suffer from this outdated dependency management are plugins related to *facebook*, *google+*, *notifications*, *crosswalk* and *google maps*.

#### More than one library with package name 'com.google.android.gms'

When some other packages include `cordova-google-play-services` as a dependency, such as is the case with the cordova-admob and cordova-plugin-analytics plugins, it is impossible to also add the phonegap-plugin-push, for the following error will rise during the build process:

```
:processDebugResources FAILED
FAILURE: Build failed with an exception.

What went wrong: Execution failed for task ':processDebugResources'. > Error: more than one library with package name 'com.google.android.gms'
```

Those plugins should be using gradle to include the Google Play Services package but instead they include the play services jar directly or via a plugin dependency. So all of that is bad news. These plugins should be updated to use gradle. Please raise issues on those plugins as the change is not hard to make.

In fact there is a PR open to do just that appfeel/analytics-google#11 for cordova-plugin-analytics. You should bug the team at appfeel to merge that PR.

Alternatively, switch to another plugin that provides the same functionality but uses gradle:

[https://github.com/danwilson/google-analytics-plugin](https://github.com/danwilson/google-analytics-plugin)
[https://github.com/cmackay/google-analytics-plugin](https://github.com/cmackay/google-analytics-plugin)

## Browser details

### Browser quirks

For the time being push support on the browser will only work using the PhoneGap push server.

### Browser Support

Chrome  49+
Firefox 46+

## iOS details

### XCode

XCode version 7.0 or greater is required for building this plugin.

### Bitcode

If you are running into a problem where the linker is complaining about bit code. For instance:

```
ld: '<file.o>' does not contain bitcode. You must rebuild it with bitcode enabled (Xcode setting ENABLE_BITCODE), obtain an updated library from the vendor, or disable bitcode for this target. for architecture arm64 clang: error: linker command failed with exit code 1 (use -v to see invocation)
```

You have two options. The first is to [disable bitcode as per this StackOverflow answer](http://stackoverflow.com/a/32466484/41679) or [upgrade to cordova-ios 4 or greater](https://cordova.apache.org/announcements/2015/12/08/cordova-ios-4.0.0.html).

```
cordova platform update ios@4.0.0
```

## Additional Resources

The push plugin enables you to play sounds and display different icons during push (Android only). These additional resources need to be added to your projects `platforms` directory in order for them to be included into your final application binary. One way of doing it is to create a hook to do the copying for you on each build.

First create a `scripts` directory in the root of your project. Next add a file to the scripts directory called `copy_resource_files.js`. The contents of the file will look something like this:

```javascript
#!/usr/bin/env node

// each object in the array consists of a key which refers to the source and
// the value which is the destination.
var filestocopy = [{
    "resources/android/images/logo.png":
    "platforms/android/res/drawable/logo.png"
}, {
    "resources/android/sounds/ring.mp3":
    "platforms/android/res/raw/ring.mp3"
}, {
    "resources/ios/sounds/ring.caf":
    "platforms/ios/YourAppName/ring.caf"
}, ];

var fs = require('fs');
var path = require('path');

// no need to configure below
var rootdir = process.argv[2];

filestocopy.forEach(function(obj) {
    Object.keys(obj).forEach(function(key) {
        var val = obj[key];
        var srcfile = path.join(rootdir, key);
        var destfile = path.join(rootdir, val);
        //console.log("copying "+srcfile+" to "+destfile);
        var destdir = path.dirname(destfile);
        if (fs.existsSync(srcfile) && fs.existsSync(destdir)) {
            fs.createReadStream(srcfile).pipe(
               fs.createWriteStream(destfile));
        }
    });
});
```

Obviously, you'll need to modify the `filestocopy` variable to suit your needs. Pay attention to the destination path on iOS where you will need to replace `YourAppName`.

Next open up your `config.xml` file and add the following line:

```xml
<hook type="before_build" src="scripts/copy_resource_files.js" />
```

Now, when you build your app the files will get copied into your platforms directory for you.

If you are using PhoneGap Build check out these instructions on [Additional Resources](PHONEGAP_BUILD.md#additional-resources)
