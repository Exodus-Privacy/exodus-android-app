plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs.kotlin")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlin.android")
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.3.0")
    android.set(true)
}

android {
    namespace = "org.eu.exodus_privacy.exodusprivacy"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.eu.exodus_privacy.exodusprivacy"
        minSdk = 23
        targetSdk = 34
        versionCode = 22
        versionName = "3.3.1"
        testInstrumentationRunner = "org.eu.exodus_privacy.exodusprivacy.ExodusTestRunner"
        val API_KEY = System.getenv("EXODUS_API_KEY")
        buildConfigField("String", "EXODUS_API_KEY", "\"$API_KEY\"")

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        signingConfigs {
            create("releaseConfig") {
                // check whether we are in release workflow or on dev system
                if (System.getenv("KEYSTORE_FILE") != null) {
                    storeFile = file(System.getenv("KEYSTORE_FILE"))
                    storePassword = System.getenv("KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("KEYSTORE_ALIAS")
                    keyPassword = System.getenv("KEYSTORE_PASSPHRASE")
                } else {
                    storeFile = file("release.keystore")
                    storePassword = System.getenv("KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("KEYSTORE_ALIAS")
                    keyPassword = System.getenv("KEYSTORE_PASSPHRASE")
                }
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("releaseConfig")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    packaging {
        resources.excludes.add("META-INF/*")
    }
    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    lint {
        lintConfig = file("lint.xml")
    }

    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // r8
    implementation(libs.tools.r8)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.markwon.core)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.android.material)
    implementation(libs.code.gson)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.shimmer)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.test.junit.ktx)

    // Navigation Components
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Retrofit and Moshi
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // okhttp
    implementation(libs.okhttp)

    // Hilt
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.dagger.hilt.android)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.service)

    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    // KTX
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.ktx)

    // unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.dagger.hilt.android.testing)
    testImplementation(libs.okhttp.mockwebserver)
    kspTest(libs.dagger.hilt.compiler)

    // instrumentation tests
    kspAndroidTest(libs.dagger.hilt.compiler)
    androidTestImplementation(libs.dagger.hilt.android.testing)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(kotlin("test"))
}

class RoomSchemaArgProvider(
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDir: File,
) : CommandLineArgumentProvider {
    override fun asArguments() = listOf("room.schemaLocation=${schemaDir.path}")
}
