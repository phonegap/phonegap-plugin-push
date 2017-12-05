# Installation

- [Installation Requirements](#installation-requirements)
- [Android details](#android-details)
  - [Compilation](#compilation)
  - [Co-existing with Facebook Plugin](#co-existing-with-facebook-plugin)
  - [Co-existing with plugins that use Firebase](#co-existing-with-plugins-that-use-firebase)
  - [Common errors](#common-errors)
    - [minSdkVersion === 14](#minsdkversion--14)
	- [Multidex](#multidex)
	- [More than one library with package name 'com.google.android.gms'](#more-than-one-library-with-package-name-comgoogleandroidgms)
- [Browser details](#browser-details)
  - [Browser quirks](#browser-quirks)
  - [Browser Support](#browser-support)
- [iOS details](#ios-details)
  - [Xcode](#xcode)
  - [Bitcode](#bitcode)
  - [CocoaPods](#cocoapods)
    - [Common CocoaPod Installation issues](#common-cocoapod-installation-issues)
    - [CocoaPod Disk Space](#cocoapod-disk-space)
    - [Library not found for -lPods-Appname](#library-not-found-for--lpods-appname)
    - [Library not found for -lGoogleToolboxForMac](#library-not-found-for--lgoogletoolboxformac)
    - [Module FirebaseInstanceID not found](#module-firebaseinstanceid-not-found)
- [Additional Resources](#additional-resources)

## Installation Requirements

Plugin version | Cordova CLI | Cordova Android | Cordova iOS | CocoaPods
---- | ---- | ---- | ---- | ----
2.0.0 | 7.0.0 | 6.2.1 | 4.4.0 | 1.1.1
1.9.0 | 6.4.0 | 6.0.0 | 4.3.0 | 1.1.1
1.8.0 | 3.6.3 | 4.0.0 | 4.1.0 | N/A

To install from the command line:

```
phonegap plugin add phonegap-plugin-push
```
or

```
cordova plugin add phonegap-plugin-push
```

It is also possible to install via repo url directly ( unstable )

```
phonegap plugin add https://github.com/phonegap/phonegap-plugin-push
```

or

```
cordova plugin add https://github.com/phonegap/phonegap-plugin-push
```

As of version 2.0.0 the SENDER_ID parameter has been removed at install time. Instead you put your google-services.json (Android) and/or GoogleService-Info.plist in the root folder of your project and then add the following lines into your config.xml.

In the platform tag for Android add the following resource-file tag if you are using cordova-android 7.0 or greater:

```
<platform name="android">
  <resource-file src="google-services.json" target="app/google-services.json" />
</platform>
```

If you are using cordova-android 6.x or earlier, add the following resource-file tag:

```
<platform name="android">
  <resource-file src="google-services.json" target="google-services.json" />
</platform>
```

By default, on iOS, the plugin will register with APNS. If you want to use FCM on iOS, in the platform tag for iOS add the resource-file tag:

```
<platform name="ios">
  <resource-file src="GoogleService-Info.plist" />
</platform>
```

> Note: if you are using Ionic you may need to specify the SENDER_ID variable in your package.json.

```
  "cordovaPlugins": [
    {
      "locator": "phonegap-plugin-push"
    }
  ]
```

> Note: You need to specify the SENDER_ID variable in your config.xml if you plan on installing/restoring plugins using the prepare method.  The prepare method will skip installing the plugin otherwise.

```
<plugin name="phonegap-plugin-push" spec="2.0.0" />
```

## Android details

### Compilation

As of version 2.1.0 the plugin has been switched to using pinned version of Gradle libraries. You will need to ensure that you have installed the following items through the Android SDK Manager:

- Android Support Repository version 47+

![android support library](https://user-images.githubusercontent.com/353180/33042340-7ea60aaa-ce0f-11e7-99f7-4631e4c3d7be.png)

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

### Co-existing with plugins that use Firebase

Problems may arise when push plugin is used along plugins that implement Firebase functionality (cordova-plugin-firebase-analytics, for example). Firebase uses `@string/google_app_id`, as does the push plugin, though the value format differs, causing problems like this: `Invalid google_app_id. Firebase Analytics disabled`.

To make the two work together, you need to migrate your GCM project from Google console to Firebase console:

1) In Firebase console - [import your existing GCM project](https://firebase.google.com/support/guides/google-android#migrate_your_console_project), don't create a new one.
2) Set your `SENDER_ID` variable to match the id of your imported Firebase project. In case of cordova, your `config.xml` would look something like this:
```xml
<plugin name="phonegap-plugin-push" spec="~1.10.0">
    <variable name="SENDER_ID" value="1:956432534015:android:df201d13e7261425" />
</plugin>
```
3) In your JavaScript, when you init the PushPlugin, senderID remains the same format as before:
```javascript
PushNotification.init({
    android: {
        senderID: 956432534015
    }
});
```

*Note:* No changes on the back-end side are needed: [even though recommended](https://developers.google.com/cloud-messaging/android/android-migrate-fcm#update_server_endpoints), it isn't yet required and sending messages through GCM gateway should work just fine.

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

When you run `phonegap serve` to test browser push point your browser at `http://localhost:3000`. The browser push implementation uses the W3C Push Spec's implementation which relies on ServiceWorkers and ServiceWorkers can only be accessed via the `https` protocol or via `http://localhost`. Pointing your browser at `localhost` will be the easiest way to test.

### Browser Support

Chrome  49+
Firefox 46+

## iOS details

### Xcode

Xcode version 8.0 or greater is required for building this plugin.

### Bitcode

If you are running into a problem where the linker is complaining about bit code. For instance:

```
ld: '<file.o>' does not contain bitcode. You must rebuild it with bitcode enabled (Xcode setting ENABLE_BITCODE), obtain an updated library from the vendor, or disable bitcode for this target. for architecture arm64 clang: error: linker command failed with exit code 1 (use -v to see invocation)
```

You have two options. The first is to [disable bitcode as per this StackOverflow answer](http://stackoverflow.com/a/32466484/41679) or [upgrade to cordova-ios 4 or greater](https://cordova.apache.org/announcements/2015/12/08/cordova-ios-4.0.0.html).

```
cordova platform update ios@4.0.0
```

### CocoaPods

Required `cordova-cli` minimum version: `6.4.0`

Required `cordova-ios` minimum version: `4.3.0`

Required `CocoaPods` minimum version: `1.0.1`


To install CocoaPods, please follow the installation instructions [here](https://guides.cocoapods.org/using/getting-started). After installing CocoaPods, please run:

    pod setup

This will clone the required CocoaPods specs-repo into your home folder at `~/.cocoapods/repos`, so it might take a while. See the [CocoaPod Disk Space](#cocoapod-disk-space) section below for more information.


Version `2.0.0` (and above) of this plugin supports [CocoaPods](https://cocoapods.org) installation of the [Firebase Cloud Messaging](https://cocoapods.org/pods/FirebaseMessaging) library.

If you are installing this plugin using `npm`, and you are using version `6.1.0` or greater of the `cordova-cli`, it will automatically download the right version of this plugin for both your platform and cli.

If you are on a `cordova-cli` version less than `6.1.0`, you will either have to upgrade your `cordova-cli` version, or install the plugin explicitly:

i.e.
```
cordova plugin add phonegap-plugin-push@1.8.1
```

If you are installing this plugin using a `local file reference` or a `git url`, you will have to specify the version of this plugin explicitly (see above) if you don't fulfill the `cordova-cli` and `cordova-ios` requirements.

#### Common CocoaPod Installation issues

If you are attempting to install this plugin and you run into this error:

```
Installing "phonegap-plugin-push" for ios
Failed to install 'phonegap-plugin-push':Error: pod: Command failed with exit code 1
    at ChildProcess.whenDone (/Users/smacdona/code/push151/platforms/ios/cordova/node_modules/cordova-common/src/superspawn.js:169:23)
    at emitTwo (events.js:87:13)
    at ChildProcess.emit (events.js:172:7)
    at maybeClose (internal/child_process.js:818:16)
    at Process.ChildProcess._handle.onexit (internal/child_process.js:211:5)
Error: pod: Command failed with exit code 1
```

Please run the command `pod repo update` and re-install the plugin. You would only run `pod repo update` if you have the specs-repo already cloned on your machine through `pod setup`.

##### CocoaPod Disk Space

Running `pod setup` can take over 1 GB of disk space and that can take quite some time to download over a slow internet connection. If you are having issues with disk space/network try this neat hack from @VinceOPS.

```
git clone --verbose --depth=1 https://github.com/CocoaPods/Specs.git ~/.cocoapods/repos/master
pod setup --verbose
```

##### Library not found for -lPods-Appname

If you open the app in Xcode and you get an error like:

```
ld: library not found for -lPods-Appname
clang: error: linker command failed with exit code 1
```

Then you are opening the .xcodeproj file when you should be opening the .xcworkspace file.

##### Library not found for -lGoogleToolboxForMac

Trying to build for iOS using the latest cocoapods (1.2.1) but failed with the following error (from terminal running cordova build ios):

```
ld: library not found for -lGoogleToolboxForMac
```

Workarounds are to add the platform first and install the plugins later, or to manually run pod install on projectName/platforms/ios.

##### Module FirebaseInstanceID not found

If you run into an error like:

```
module FirebaseInstanceID not found
```

You may be running into a bug in cordova-ios. The current workaround is to run `pod install` manually.

```
cd platforms/ios
pod install
```

## Additional Resources

The push plugin enables you to play sounds and display different icons during push (Android only). These additional resources need to be added to your projects `platforms` directory in order for them to be included into your final application binary.

You can now use the `resource-file` tag to deliver the image and sound files to your application. For example if you wanted to include an extra image file for only your Android build you would add the `resource-file` tag to your android `platform` tag:

```
<platform name="android">
  <resource-file src="myImage.png" target="res/drawable/myImage.png" />
</platform>
```

or if you wanted to include a sound file for iOS:


```
<platform name="ios">
  <resource-file src="mySound.caf" />
</platform>
```
