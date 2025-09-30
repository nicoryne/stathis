import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
    kotlin("plugin.serialization")
}

val localProps = gradle.rootProject.file("local.properties").let { file ->
    Properties().apply {
        if (file.exists()) file.inputStream().use { load(it) }
    }
}

// String helper
fun propStr(key: String, default: String = ""): String =
    localProps.getProperty(key, default)

// Boolean helper
fun propBool(key: String, default: String = "false"): String =
    localProps.getProperty(key, default)

android {
    namespace = "cit.edu.stathis.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "cit.edu.stathis.mobile"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            // Read from local.properties (defaults shown)
            val appEnv = propStr("APP_ENV", "local")
            val apiBaseUrl = propStr("API_BASE_URL", "https://api-stathis.ryne.dev/")
            buildConfigField("String", "APP_ENV", "\"$appEnv\"")
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
            // Always show onboarding when APP_ENV=local
            val alwaysShowOnboarding = if (appEnv == "local") "true" else "false"
            buildConfigField("boolean", "ALWAYS_SHOW_ONBOARDING", alwaysShowOnboarding)
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            val appEnv = propStr("APP_ENV", "prod")
            val apiBaseUrl = propStr("API_BASE_URL", "https://api-stathis.ryne.dev/")
            buildConfigField("String", "APP_ENV", "\"$appEnv\"")
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
            buildConfigField("boolean", "ALWAYS_SHOW_ONBOARDING", "false")
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = false
        viewBinding = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.biometric.ktx)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt + ViewModel
    implementation(libs.androidx.hilt.navigation.compose)

    // Material 3
    implementation(libs.androidx.material3)
    
    // DataStore for theme preferences (use version catalog alias)

    // Android UI
    implementation(libs.androidx.ui.tooling.preview)

    // Material Icons
    implementation(libs.androidx.material.icons.extended.android)

    // Google Font
    implementation(libs.androidx.ui.text.google.fonts)

    // Data Store
    implementation(libs.androidx.datastore.preferences)

    // Lottie
    implementation(libs.lottie.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Ktor Client Engine
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.android)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Biometric

    // Bluetooth dependencies
    implementation(libs.androidx.bluetooth)

    // ML Kit dependencies for pose detection
    implementation(libs.pose.detection)
    implementation(libs.pose.detection.accurate)

    // CameraX dependencies
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation("com.google.guava:guava:31.0.1-android")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")

    // Accompanist permissions for camera permission handling
    implementation(libs.accompanist.permissions)

    // Testing dependencies
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")
    testImplementation("com.squareup.okhttp3:logging-interceptor")
    // Use unified latest AndroidX test artifacts from version catalog
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    implementation(libs.jwtdecode)

    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    implementation("androidx.health.connect:connect-client:1.2.0-alpha01")

    // Timber for logging
    implementation(libs.timber)

    // Compose Charts
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("com.patrykandpatrick.vico:core:1.13.1")

    // Feature modules
    implementation(project(":core:common"))
    implementation(project(":feature:classroom"))
    implementation(project(":feature:exercise"))
    implementation(project(":feature:vitals"))
    implementation(project(":feature:tasks"))
    implementation(project(":feature:progress"))

}

kapt {
    correctErrorTypes = true
}
