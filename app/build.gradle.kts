plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    implementation("androidx.health.connect:connect-client:1.1.0-alpha10")
    implementation("androidx.health:health-services-client:1.1.0-alpha04")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.profileinstaller:profileinstaller:1.3.0")
    implementation ("androidx.compose.ui:ui-text-google-fonts:1.7.5")
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("androidx.room:room-runtime:2.5.2")
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.animation.core.lint)
    implementation(libs.androidx.navigation.compose.android)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.hilt.work)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("com.firebaseui:firebase-ui-auth:9.0.0")
    implementation ("com.itextpdf:itext7-core:7.2.5")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.maxkeppeler.sheets-compose-dialogs:core:1.3.0")
    implementation ("com.maxkeppeler.sheets-compose-dialogs:calendar:1.3.0")


    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation ("com.google.accompanist:accompanist-navigation-animation:0.36.0")
    implementation ("com.google.accompanist:accompanist-pager:0.35.0-alpha")

    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.firebase.firestore.ktx)
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
