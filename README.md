<h1 align="center">Exodus - Android Application</h1>

Exodus is an Android application that lets you know what trackers are embedded in apps installed on your smartphone using [the εxodus platform](https://github.com/Exodus-Privacy/exodus). It also lets you know the permissions required by any apps on your smartphone.

It helps you to take your privacy back!

<p align="center">
  <a href="https://github.com/Exodus-Privacy/exodus-android-app/actions/workflows/main.yml">
    <img src="https://github.com/Exodus-Privacy/exodus-android-app/actions/workflows/main.yml/badge.svg?branch=master" alt="Build Status"/>
  </a>
  <a href="https://github.com/Exodus-Privacy/exodus-android-app/releases">
    <img src="https://img.shields.io/github/v/release/Exodus-Privacy/exodus-android-app" alt="GitHub release (latest by date)"/>
  </a>
  <a href="https://crowdin.com/project/exodus-android-app">
    <img src="https://badges.crowdin.net/exodus-android-app/localized.svg" alt="Crowdin"/>
  </a>
</p>

<p align="center">
  <a href="https://f-droid.org/packages/org.eu.exodus_privacy.exodusprivacy/">
    <img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80"/>
  </a>
  <a href="https://play.google.com/store/apps/details?id=org.eu.exodus_privacy.exodusprivacy">
    <img src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' alt='Get it on Google Play' height="80"/>
  </a>
  <a href="https://github.com/Exodus-Privacy/exodus-android-app/releases/latest/">
    <img src="https://user-images.githubusercontent.com/663460/26973090-f8fdc986-4d14-11e7-995a-e7c5e79ed925.png" alt="Download APK from GitHub" height="80">
  </a>
</p>

## Screenshots

<p align="center">
  <a href="./fastlane/metadata/android/en-US/images/phoneScreenshots/app_list.png">
    <img src="https://github.com/Exodus-Privacy/exodus-android-app/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/app_list.png" height="600">
  </a>
  <a href="./fastlane/metadata/android/en-US/images/phoneScreenshots/tracker1.png">
    <img src="https://github.com/Exodus-Privacy/exodus-android-app/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/tracker1.png" height="600">
  </a>
  <a href="./fastlane/metadata/android/en-US/images/phoneScreenshots/report2.png">
    <img src="https://github.com/Exodus-Privacy/exodus-android-app/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/report2.png" height="600">
  </a>
</p>

## Contributing

If you want to help us improve this application, you can:
- [Translate app](https://github.com/Exodus-Privacy/exodus-android-app#translation)
- Use [the issues](https://github.com/Exodus-Privacy/exodus-android-app/issues) to report bugs and propose ideas or feature requests
- Join us on our [IRC channel #exodus-android-app on Libera.chat](https://web.libera.chat/?nick=webguest?#exodus-android-app) (also bridged on [Matrix channel #exodus-android-app:matrix.org](https://matrix.to/#/#exodus-android-app:matrix.org))


### Translation

Do you want to help translate the application? Contribute here:

https://crowdin.com/project/exodus-android-app

- Exodus is fully translated into 2 languages and 36 languages can be translated in Crowdin.
- You can translate app on Github but remember to insert a backslash `\` before any apostrophe `'`.

### Development

**If you would like to improve app code and have skills about development, you are most welcome.**
- You can find work in [issues](https://github.com/Exodus-Privacy/exodus-android-app/issues) or in [Exodus project](https://github.com/Exodus-Privacy/exodus-android-app/projects/1).
- Before submitting pull requests please, execute Kotlin Liner.
- Do not create pull requests to update dependencies, we have [dependabot](https://github.com/Exodus-Privacy/exodus-android-app/blob/master/.github/dependabot.yml).

**Build APK Debug**

```
./gradlew assembleDebug
```

**Execute Kotlin Liner**

```
./gradlew app:ktlintCheck --info
```

**Execute local unit tests**

```
./gradlew test
```

**Execute instrumented tests**

```
./gradlew connectedAndroidTest
```

- To execute test move [network_security_config.xml](/doc/network_security_config.xml) in [/app/src/main/res/xml](/app/src/main/res/xml)
- Add ```android:networkSecurityConfig="@xml/network_security_config"``` in [AndroidManifest.xml](/app/src/main/AndroidManifest.xml)

### Links

- [Privacy-Policy](https://exodus-privacy.eu.org/en/page/privacy-policy/)
- [REST API Documentation](https://github.com/Exodus-Privacy/exodus/blob/v1/doc/api.md)
