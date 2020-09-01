# Installation

- [Installation](#installation)
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
    - [System & Cordova Requirements](#system--cordova-requirements)
    - [Bitcode](#bitcode)
    - [CocoaPods](#cocoapods)
      - [Common CocoaPod Installation issues](#common-cocoapod-installation-issues)
        - [Library not found for -lPods-Appname](#library-not-found-for--lpods-appname)
        - [Library not found for -lGoogleToolboxForMac](#library-not-found-for--lgoogletoolboxformac)
  - [Additional Resources](#additional-resources)

## Installation Requirements

| Plugin version | Cordova CLI | Cordova Android | Cordova iOS | CocoaPods |
| -------------- | ----------- | --------------- | ----------- | --------- |
| 2.2.0          | 7.1.0       | 7.1.0           | 4.5.0       | 1.1.1     |
| 2.1.2          | 7.1.0       | 6.3.0           | 4.5.0       | 1.1.1     |
| 2.1.0          | 7.1.0       | 6.3.0           | 4.4.0       | 1.1.1     |
| 2.0.0          | 7.0.0       | 6.2.1           | 4.4.0       | 1.1.1     |
| 1.9.0          | 6.4.0       | 6.0.0           | 4.3.0       | 1.1.1     |
| 1.8.0          | 3.6.3       | 4.0.0           | 4.1.0       | N/A       |

To install from the command line:

```bash
phonegap plugin add phonegap-plugin-push
```

or

```bash
cordova plugin add phonegap-plugin-push
```

It is also possible to install via repo url directly ( unstable )

```bash
phonegap plugin add https://github.com/phonegap/phonegap-plugin-push
```

or

```bash
cordova plugin add https://github.com/phonegap/phonegap-plugin-push
```

As of version 2.0.0 the SENDER_ID parameter has been removed at install time. Instead you put your google-services.json (Android) and/or GoogleService-Info.plist in the root folder of your project and then add the following lines into your config.xml.

In the platform tag for Android add the following resource-file tag if you are using cordova-android 7.0 or greater:

```xml
<platform name="android">
  <resource-file src="google-services.json" target="app/google-services.json" />
</platform>
```

If you are using cordova-android 6.x or earlier, add the following resource-file tag:

```xml
<platform name="android">
  <resource-file src="google-services.json" target="google-services.json" />
</platform>
```

By default, on iOS, the plugin will register with APNS. If you want to use FCM on iOS, in the platform tag for iOS add the resource-file tag:

```xml
<platform name="ios">
  <resource-file src="GoogleService-Info.plist" />
</platform>
```

> Note: if you are using Ionic you may need to specify the SENDER_ID variable in your package.json.

```json
  "cordovaPlugins": [
    {
      "locator": "phonegap-plugin-push"
    }
  ]
```

> Note: You need to specify the SENDER_ID variable in your config.xml if you plan on installing/restoring plugins using the prepare method. The prepare method will skip installing the plugin otherwise.

```xml
<plugin name="phonegap-plugin-push" spec="2.0.0" />
```

## Android details

### Compilation

As of version 2.1.0 the plugin has been switched to using pinned version of Gradle libraries. You will need to ensure that you have installed the following items through the Android SDK Manager:

* Android Support Repository version 47+

![android support library](https://user-images.githubusercontent.com/353180/33042340-7ea60aaa-ce0f-11e7-99f7-4631e4c3d7be.png)

For more detailed instructions on how to install the Android Support Library visit [Google's documentation](https://developer.android.com/tools/support-library/setup.html).

_Note:_ if you are using an IDE to like Eclipse, Xamarin, etc. then the Android SDK installed by those tools may not be the same version as the one used by the Cordova/PhoneGap CLI while building. Please make sure your command line tooling is up to date with the software versions above. An easy way to make sure you up to date is to run the following command:

```bash
android update sdk --no-ui --filter "extra"
```

### Co-existing with Facebook Plugin

There are a number of Cordova Facebook Plugins available but the one that we recommend is [Jeduan's fork](https://github.com/jeduan/cordova-plugin-facebook4) of the original Wizcorp plugin. It is setup to use Gradle/Maven and the latest Facebook SDK properly.

To add to your app:

```bash
phonegap plugin add --save cordova-plugin-facebook4 --variable APP_ID="App ID" --variable APP_NAME="App Name"
```

or

```bash
cordova plugin add --save cordova-plugin-facebook4 --variable APP_ID="App ID" --variable APP_NAME="App Name"
```

### Co-existing with plugins that use Firebase

Problems may arise when push plugin is used along plugins that implement Firebase functionality (e.g. `cordova-plugin-firebase-analytics`). Both plugins include a version of the FCM libraries.

To make the two work together, you need to migrate your GCM project from Google console to Firebase console:

1. In Firebase console - [import your existing GCM project](https://firebase.google.com/support/guides/google-android#migrate_your_console_project), don't create a new one.
2. Set your `FCM_VERSION` variable to match the version used in the other plugin. In case of Cordova, your `package.json` contains something like this:

```json
{
  "cordova": {
    "plugins": {
      "cordova-plugin-push": {
        "ANDROID_SUPPORT_V13_VERSION": "27.+",
        "FCM_VERSION": "18.+"
      }
    },
    "platforms": []
  }
}
```

_Note:_ No changes on the back-end side are needed: [even though recommended](https://developers.google.com/cloud-messaging/android/android-migrate-fcm#update_server_endpoints), it isn't yet required and sending messages through GCM gateway should work just fine.

_Note:_ The `FCM_VERSION` must be greater than or equal to 17.1.0 and less than or equal to 18.0.0.

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

Common plugins to suffer from this outdated dependency management are plugins related to _facebook_, _google+_, _notifications_, _crosswalk_ and _google maps_.

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

Chrome 49+
Firefox 46+

## iOS details

### System & Cordova Requirements

**System:**

- `Xcode`: `11.0` or greater.
- `CocoaPods`: `1.8.0` or greater. Preferably `1.9.x`
- `Ruby`: `2.0.0` or greater.

**Cordova:**

- `cordova-cli`: `9.0.0` or greater. Preferably `10.x`
- `cordova-ios`: `5.1.1` or greater. Preferably `6.1.x`

### Bitcode

If you are running into a problem where the linker is complaining about bit code. For instance:

```log
ld: '<file.o>' does not contain bitcode. You must rebuild it with bitcode enabled (Xcode setting ENABLE_BITCODE), obtain an updated library from the vendor, or disable bitcode for this target. for architecture arm64 clang: error: linker command failed with exit code 1 (use -v to see invocation)
```

You have two options. The first is to [disable bitcode as per this StackOverflow answer](http://stackoverflow.com/a/32466484/41679) or [upgrade to cordova-ios 5.1.1 or greater](https://cordova.apache.org/announcements/2019/12/02/cordova-ios-release-5.1.1.html).

```bash
cordova platform rm ios
cordova platform add ios@5.1.1
```

### CocoaPods

To install CocoaPods, please follow the installation instructions [here](https://guides.cocoapods.org/using/getting-started). Since version `1.8.0` and greater, the pod repo no longer needs to be setup or fetched. Pods specs will be fetched directly from the **CocoaPods CDN**.

If you are upgrading from an older version, it might be best to uninstall first the older version and remove the `~/.cocoapods/` directory.

This plugin uses the [Firebase/Messaging](https://cocoapods.org/pods/Firebase) library.

#### Common CocoaPod Installation issues

If you are attempting to install this plugin and you run into this error:

```log
Installing "cordova-plugin-push" for ios
Failed to install 'cordova-plugin-push':Error: pod: Command failed with exit code 1
    at ChildProcess.whenDone (/Users/smacdona/code/push151/platforms/ios/cordova/node_modules/cordova-common/src/superspawn.js:169:23)
    at emitTwo (events.js:87:13)
    at ChildProcess.emit (events.js:172:7)
    at maybeClose (internal/child_process.js:818:16)
    at Process.ChildProcess._handle.onexit (internal/child_process.js:211:5)
Error: pod: Command failed with exit code 1
```

Please try to add the plugin again, with the `--verbose` flag. The above error is generic and can actually be caused by a number of reasons. The `--verbose` flag should help display the exact cause of the install failure.

One of the most common reason is that it is trying to fetch the podspec from the CocoaPods repo and the repo is out-of-date. It recommended to use CocoaPods CDN over the repo. If your using an older version of CocoaPods, it is recommend to upgrade with a fresh installation.

With a fresh installations, you should have one repo source which can be checked with the `pod repo` command.

```log
$ pod repo

trunk
- Type: CDN
- URL:  https://cdn.cocoapods.org/
- Path: /Users/home/.cocoapods/repos/trunk

1 repo
```

##### Library not found for -lPods-Appname

If you open the app in Xcode and you get an error like:

```log
ld: library not found for -lPods-Appname
clang: error: linker command failed with exit code 1
```

Then you are opening the .xcodeproj file when you should be opening the .xcworkspace file.

##### Library not found for -lGoogleToolboxForMac

Trying to build for iOS using the latest cocoapods (1.9.3) but failed with the following error (from terminal running cordova build ios):

```log
ld: library not found for -lGoogleToolboxForMac
```

Workarounds are to add the platform first and install the plugins later, or to manually run pod install on projectName/platforms/ios.

Another workaround is to go to build phases in your project at Link Binary Libraries and add `libPods-PROJECTNAME.a` and `libGoogleToolboxForMac.a`

## Additional Resources

The push plugin enables you to play sounds and display different icons during push (Android only). These additional resources need to be added to your projects `platforms` directory in order for them to be included into your final application binary.

You can now use the `resource-file` tag to deliver the image and sound files to your application. For example if you wanted to include an extra image file for only your Android build you would add the `resource-file` tag to your android `platform` tag:

```xml
<platform name="android">
  <resource-file src="myImage.png" target="res/drawable/myImage.png" />
</platform>
```

or if you wanted to include a sound file for iOS:

```xml
<platform name="ios">
  <resource-file src="mySound.caf" />
</platform>
```
