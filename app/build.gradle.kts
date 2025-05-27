plugins {
    // Apply the Android Application plugin via version catalog alias
    alias(libs.plugins.android.application)
    // Apply Kotlin Android plugin via version catalog alias
    alias(libs.plugins.kotlin.android)
}

android {
    // Unique namespace for your app (used for R class, etc.)
    namespace = "com.tomo.memento"

    // Compile against Android API 35
    compileSdk = 35

    defaultConfig {
        // Application ID (also package name on Play Store)
        applicationId = "com.tomo.memento"
        // Minimum Android version supported
        minSdk = 24
        // Target Android version to optimize for
        targetSdk = 35
        // Internal app versioning
        versionCode = 1
        versionName = "1.0"

        // Runner for instrumented tests
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Minification disabled for release; can enable later for size/security
            isMinifyEnabled = false
            // Use default and custom ProGuard rules
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Java 11 compatibility
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        // Kotlin JVM target set to 11
        jvmTarget = "11"
    }

    buildFeatures {
        // Enables ViewBinding for easier view access
        viewBinding = true
    }
}

dependencies {
    // Android core libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Material Components UI library
    implementation(libs.material.v1110) // Explicitly declared

    // Layout and lifecycle components
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Jetpack Navigation components
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Mapbox Maps SDK for displaying maps
    implementation(libs.android)

    // Unit testing
    testImplementation(libs.junit)
    // Android instrumented testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
