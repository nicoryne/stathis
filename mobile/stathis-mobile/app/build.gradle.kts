
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
    kotlin("plugin.serialization")
}

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
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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

    // Android UI
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

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
    implementation(libs.androidx.biometric.ktx)

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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)
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

    implementation("androidx.health.connect:connect-client:1.1.0-rc01")

}

kapt {
    correctErrorTypes = true
}
