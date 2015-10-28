# Google APIs for iOS

Simplify your iOS development, grow your user base, and monetize more
effectively with Google services.

Much more information can be found at https://developers.google.com/ios/.

## Install a Google SDK using CocoaPods

Google distributes several iOS specific APIs and SDKs via CocoaPods.
You can install the CocoaPods tool on OS X by running the following command from
the terminal. Detailed information is available in the [Getting Started
guide](https://guides.cocoapods.org/using/getting-started.html#getting-started).

```
$ sudo gem install cocoapods
```

## Try out an SDK

You can try any of the SDKs with `pod try`. Run the following command and select
the SDK you are interested in when prompted:

```
$ pod try Google
```

Note that some SDKs may require credentials. More information is available in
the SDK-specific documentation at https://developers.google.com/ios/.

### Add a Google SDK to your iOS app

CocoaPods is used to install and manage dependencies in existing Xcode projects.

1. Create an Xcode project, and save it to your local machine.
2. Create a file named `Podfile` in your project directory. This file defines
   your project's dependencies, and is commonly referred to as a Podspec.
3. Open `Podfile`, and add your dependencies. A simple Podspec is shown here:

    ```
    platform :ios, '8.1'
    pod 'Google'
    ```

4. Save the file.
5. Open a terminal and `cd` to the directory containing the Podfile.

    ```
    $ cd <path-to-project>/project/
    ```

6. Run the `pod install` command. This will install the SDKs specified in the
   Podspec, along with any dependencies they may have.

    ```
    $ pod install
    ```

7. Open your app's `.xcworkspace` file to launch Xcode.
   Use this file for all development on your app.

### CocoaPods published by Google

Below is a complete list of the Podspecs published by Google.

| CocoaPods published by Google |                                                                                 |
|-------------------------------|---------------------------------------------------------------------------------|
| Google APIs for iOS           | [Google](https://cocoapods.org/pods/Google)                                     |
| App Invites                   | [AppInvites](https://cocoapods.org/pods/AppInvites)                             |
| Google Analytics              | [GoogleAnalytics](https://cocoapods.org/pods/GoogleAnalytics)                   |
| Google App Indexing           | [GoogleAppIndexing](https://cocoapods.org/pods/GoogleAppIndexing)               |
| Google Cloud Messaging        | [GoogleCloudMessaging](https://cocoapods.org/pods/GoogleCloudMessaging)         |
| Google Conversion Tracking    | [GoogleConversionTracking](https://cocoapods.org/pods/GoogleConversionTracking) |
| Google Maps                   | [GoogleMaps](https://cocoapods.org/pods/GoogleMaps)                             |
| Google Mobile Ads             | [GoogleMobileAds](https://cocoapods.org/pods/GoogleMobileAds)                   |
| Google IDFA Support           | [GoogleIDFASupport](https://cocoapods.org/pods/GoogleIDFASupport)               |
| Google Places                 | [GoogleMaps](https://cocoapods.org/pods/GoogleMaps)                             |
| Google Sign In                | [GoogleSignIn](https://cocoapods.org/pods/GoogleSignIn)                         |
| Google Tag Manager            | [GoogleTagManager](https://cocoapods.org/pods/GoogleTagManager)                 |
| Instance ID                   | [GGLInstanceID](https://cocoapods.org/pods/GGLInstanceID)                       |
| Google Cast                   | [google-cast-sdk](https://cocoapods.org/pods/google-cast-sdk)                   |
| Firebase                      | [Firebase](https://cocoapods.org/pods/Firebase)                                 |
| Google Play Games Services    | [GooglePlayGames](https://cocoapods.org/pods/GooglePlayGames)                   |
| GeoFire                       | [GeoFire](https://cocoapods.org/pods/GeoFire)                                   |
| Google Interactive Media Ads  | [GoogleAds-IMA-iOS-SDK](https://cocoapods.org/pods/GoogleAds-IMA-iOS-SDK)       |

