# Create and push a release

This file explain how create a new release of the app.

1. Increase `versionCode` and `versionName` in [app/build.gradle.kts](/app/build.gradle.kts)
2. Add english and french changelogs in [fastlane/metadata/android/en-US/changelogs](/fastlane/metadata/android/en-US/changelogs) and [fastlane/metadata/android/fr-FR/changelogs](/fastlane/metadata/android/fr-FR/changelogs) with versionCode like name of the file.
3. Push these changes on master
4. Create and push a tag following this format `release-v` + versionName
5. The [release workflow](/.github/workflows/release.yml) is automatically triggered which creates a new release on Github, generates automatically a changelog, and uploads an APK on the Github release.

To follow publication on FDroid, you can check the FDroid robot have update [Build Metadata file](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/org.eu.exodus_privacy.exodusprivacy.yml) and check build is in success [on F-Droid Monitor](https://monitor.f-droid.org/builds/build).
