// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.androidx.navigation.safeargs.kotlin) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.gradle.ktlint) apply false
    alias(libs.plugins.kotlin.ksp) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
