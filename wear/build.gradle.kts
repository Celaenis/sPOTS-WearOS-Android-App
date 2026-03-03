plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.tutorial"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tutorial"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    implementation("androidx.wear:wear:1.2.0")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.core:core")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.0")
    implementation("androidx.health.connect:connect-client:1.1.0-alpha10")
    implementation("androidx.health:health-services-client:1.1.0-alpha04")
    implementation("com.google.guava:guava:31.1-android")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("androidx.compose.material3:material3:1.3.1")
    implementation ("androidx.compose.material3:material3-window-size-class:1.3.1")
    implementation ("androidx.compose.material3:material3-adaptive-navigation-suite:1.4.0-alpha04")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.6.0")
    implementation("androidx.wear.compose:compose-foundation:1.2.0-alpha05")
    implementation("androidx.wear.compose:compose-material:1.2.0-alpha05")
    implementation("androidx.wear.compose:compose-navigation:1.4.0")
    implementation ("androidx.room:room-runtime:2.5.2")
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
