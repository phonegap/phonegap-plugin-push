# Cloud Build Services

- [PhoneGap Build Support](#phonegap-build-support)
  - [Including the plugin](#including-the-plugin)
  - [Adding Resources](#adding-resources)
- [IntelXDK Support](#intelxdk-support)
- [Ionic Cloud Build](#ionic-cloud-build)

## PhoneGap Build Support

> Currently PhoneGap Build does not support version 1.9.0 of the plugin. Updates are being made to PGB to support the latest release.

### Including the plugin

Including this plugin in a project that is built by PhoneGap Build is as easy as adding (replacing `123456789` with your own, that is):

```xml
<preference name="android-build-tool" value="gradle" />
<plugin name="phonegap-plugin-push" source="npm">
    <param name="SENDER_ID" value="123456789" />
</plugin>
```

into your app's `config.xml` file. PhoneGap Build will pick up the latest version of phonegap-plugin-push published on npm. If you want to specify a particular version of the plugin you can add the `spec` attribute to the `plugin` tag.

```xml
<preference name="android-build-tool" value="gradle" />
<plugin name="phonegap-plugin-push" spec="~1.4.5" source="npm" />
```

Note: version 1.3.0 of this plugin begins to use Gradle to install the Android Support Framework. Support for Gradle has recently been added to PhoneGap Build. Please read [this blog post](http://phonegap.com/blog/2015/09/28/android-using-gradle/) for more information.

### Adding resources

Because PhoneGap Build does not support running hooks if you want to include custom image or sounds you will need to use a *beta* feature to include these files.

#### Android

To add custom files, create a directory called `locales/android/` in the root of your PGB application zip / repo, and place your resource files there. The contents will be copied into the Android `res/` directory, and any nested sub-directory structures will persist. Here's an example of how these files will be compiled into your APK:

```
<www.zip>/locales/android/drawables/logo.png    --> <android_apk>/res/drawables/logo.png
<www.zip>/locales/android/raw/beep.mp3          --> <android_apk>/res/raw/beep.mp3
<www.zip>/locales/android/values-fr/strings.xml --> <android_apk>/res/values-fr/strings.xml
```

Existing directories will be merged, but at this time any individual files you include will overwrite their target if it exists.

## IntelXDK Support

1. Do pre-requisite setup on [the iOS Provisioning Portal](https://developer.apple.com/account/ios/identifier/bundle).  Refer to [this guide](https://www.raywenderlich.com/123862/push-notifications-tutorial) or Apple docs for detailed steps.
a. make a new App ID (you'll need to set this in Intel XDK config later)
b. enable push notifications
c. iOS Distribution cert: create (if needed), download and install (if needed), export as a .p12 (set and remember the password as you'll need this to import into Intel XDK later)
**NOTE**: Intel XDK does not support Development certs, so you MUST use your Distribution cert.
d. Make an AdHoc Provisioning Profile using your App ID from (1a) and your cert from (1c).  Make sure your test device is enabled.  Download and save with a name you will recognize. (you'll need to add this to your Intel XDK project later)
e. make a push cert, download it, install it, export it to .p12, convert it to .pem (this is for the push server that will send the notification - you'll need this later to test your Intel XDK app)

2. In Intel XDK, make a new Cordova CLI 5.4.1 project using the HTML5+Cordova Blank Template, then replace the contents of www with [the contents of www from the PhoneGap Push Template](https://github.com/phonegap/phonegap-template-push/tree/master/template_src/www).

3. Delete www/config.xml (optional? Intel XDK does not use config.xml)

4. Intel XDK Project Settings
a. set the iOS App ID to match the App ID from (1a)
b. (if needed) import your .p12 from (1c) - Account Settings->Developer Certificates->iOS, then select it as the Developer Certificate for the project
c. Select "adhoc" for Provisioning Profile
d. copy your provisioning profile from (1d) into www/, then click "Ad hoc Provisioning Profile" and select the profile
e. Add the latest version of phonegap-plugin-push as a "Third-Party Plugin" (at time of testing this was 1.6.4)
f. **After the plugin is added, you will need to edit plugins/phonegap-plugin-push/plugin.xml**.  Intel XDK 3357 does not support plugins with gradle references, so the gradle reference must be commented out (this will prevent this version of the plugin from working for Android but is needed for the iOS build to succeed):
`<!--framework src="push.gradle" custom="true" type="gradleReference" /-->`
A future version of Intel XDK will support gradle references.

5. XDK Build Tab
a. Enable iOS build (click the checkmark)
b. Unlock your iOS certificate (click the lock and enter the password from (1c))
c. click Start Builds
d. once the build completes, download and install the app

6. connect test device by USB and open XCode Devices window (probably could also use Safari Web Inspector + Cordova Console plugin) - start the app and a log message should be written into the console that looks like "Push Plugin register success: \<XXXXXXXX 19b101a3 71590c03 9ea7f446 50eb8409 19ac24bb c1ec1320 XXXXXXXX\>"

7. exit the app (close with home button then swipe it off the multitask view)

8. The angle brackets and everything between (from (5)) is the device token - copy it into a text file

9. Add the device token to your server and send a push notification
a. I used [phonegap-plugin-push/example/server/pushAPNS.rb](https://github.com/phonegap/phonegap-plugin-push/blob/master/example/server/pushAPNS.rb) for this
b. APNS.host = 'gateway.push.apple.com'
c. APNS.pem  = 'PGPush926Prod.pem' #path to your pem file from (1e)
d. device_token = '\<XXXXXXXX 19b101a3 71590c03 9ea7f446 50eb8409 19ac24bb c1ec1320 XXXXXXXX\>' #the device token from (7)
e. edit the alert message and badge number
f. you probably need to install the required gem (`gem install pushmeup`)
g. send the notification (`ruby pushAPNS.rb`)

10. See notification on device!

## Ionic Cloud Build

Users have reported issues with Ionic Cloud Build. Apparently there are some differences in the way variables are handled. If your app has an issue where the `PushNotification` object can't be found try the following.

1. Remove the inclusion of `phonegap-plugin-push` from config.xml. That is delete lines that look like this:

```
<plugin name="phonegap-plugin-push" spec="~1.9.1">
  <variable name="SENDER_ID" value="xxx"/>
</plugin>
```
2. Add the following lines into `package.json` in the `cordovaPlugins` array.

```
{
  "variables": {
    "SENDER_ID": "xxx"
  },
  "locator": "phonegap-plugin-push"
}
```
