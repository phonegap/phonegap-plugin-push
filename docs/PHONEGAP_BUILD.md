# PhoneGap Build Support

- [PhoneGap Build Support](#phonegap-build-support)
  - [Including the plugin](#including-the-plugin)
  - [Adding Resources](#adding-resources)

## Including the plugin

Including this plugin in a project that is built by PhoneGap Build is as easy as adding:

```xml
<preference name="android-build-tool" value="gradle" />
<plugin name="phonegap-plugin-push" source="npm">
    <param name="SENDER_ID" value="<Your Sender ID>" />
</plugin>
```

into your app's `config.xml` file. PhoneGap Build will pick up the latest version of phonegap-plugin-push published on npm. If you want to specify a particular version of the plugin you can add the `spec` attribute to the `plugin` tag.

```xml
<preference name="android-build-tool" value="gradle" />
<plugin name="phonegap-plugin-push" spec="~1.4.5" source="npm" />
```

Note: version 1.3.0 of this plugin begins to use Gradle to install the Android Support Framework. Support for Gradle has recently been added to PhoneGap Build. Please read [this blog post](http://phonegap.com/blog/2015/09/28/android-using-gradle/) for more information.

## Adding resources

Because PhoneGap Build does not support running hooks if you want to include custom image or sounds you will need to use a *beta* feature to include these files.

### Android

To add custom files, create a directory called `locales/android/` in the root of your PGB application zip / repo, and place your resource files there. The contents will be copied into the Android `res/` directory, and any nested sub-directory structures will persist. Here's an example of how these files will be compiled into your APK:

```
<www.zip>/locales/android/drawables/logo.png    --> <android_apk>/res/drawables/logo.png
<www.zip>/locales/android/raw/beep.mp3          --> <android_apk>/res/raw/beep.mp3
<www.zip>/locales/android/values-fr/strings.xml --> <android_apk>/res/values-fr/strings.xml
```

Existing directories will be merged, but at this time any individual files you include will overwrite their target if it exists.
