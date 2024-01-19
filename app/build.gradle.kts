plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.wonderapps.translator"
    compileSdk = 34
    bundle {
        language {
            enableSplit = false
        }
    }
    defaultConfig {
        applicationId = "com.wonderapps.translator"
        minSdk = 26
        targetSdk = 33
        versionCode = 18
        versionName = "1.0.18"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            resValue("string", "app_id", "ca-app-pub-3940256099942544~3347511713")
            resValue(
                "string",
                "splash_interstitial_ad_id",
                "ca-app-pub-3940256099942544/1033173712"
            )
            resValue("string", "interstitial_ad_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "banner_ad_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "native_ad_id", "ca-app-pub-3940256099942544/2247696110")

        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "app_id", "ca-app-pub-3612093051527159~4908912444")
            resValue(
                "string",
                "splash_interstitial_ad_id",
                "ca-app-pub-3612093051527159/4273120646"
            )
            resValue("string", "interstitial_ad_id", "ca-app-pub-3612093051527159/1180053447")
            resValue("string", "banner_ad_id", "ca-app-pub-3612093051527159/8212365656")
            resValue("string", "native_ad_id", "ca-app-pub-3612093051527159/2960038979")

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true

    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-ads:22.4.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")
    implementation("com.google.mlkit:language-id-common:16.1.0")
    implementation("com.google.android.play:review-ktx:2.0.1")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.airbnb.android:lottie:5.0.2")
    implementation("com.infideap.drawerbehavior:drawer-behavior:1.0.1")
    val nav_version = "2.7.4"

    // Java language implementation
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")
    implementation("com.github.iammannan:TranslateAPI:1.1")
    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Feature module Support
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    // Jetpack Compose Integration
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.github.MindorksOpenSource:EditDrawableText:1.1.0")
    implementation("com.google.mlkit:translate:17.0.1")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:18.0.2")
    implementation("com.google.mlkit:translate:17.0.1")
    val room_version = "2.5.0"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    val camerax_version = "1.0.0-rc01"
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:1.0.0-alpha20")
    implementation("com.edmodo:cropper:2.0.0")
    implementation("com.theartofdev.edmodo:android-image-cropper:2.8+")
    implementation("io.ktor:ktor-client-core:2.2.2")
    implementation("io.ktor:ktor-client-cio:2.2.2")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.karumi:dexter:6.2.3")
    implementation("com.google.mlkit:translate:17.0.1")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:18.0.2")
    implementation("com.google.mlkit:translate:17.0.1")
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("com.google.firebase:firebase-ml-vision:24.0.3")
    implementation("com.google.mlkit:language-id:17.0.4")
    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("com.google.android.gms:play-services-vision-common:19.1.3")
    implementation("com.rmtheis:tess-two:6.0.4")
    implementation("com.jakewharton:butterknife:8.8.1")
    implementation("com.rmtheis:tess-two:9.0.0")
    implementation("com.google.firebase:firebase-ml-vision:24.0.3")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-chinese:16.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-devanagari:16.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-japanese:16.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-korean:16.0.0")
    implementation("com.github.Amankhan-mobipixels:Admob-Ads:1.1.2")

    implementation("com.getkeepsafe.taptargetview:taptargetview:1.13.3")
    implementation("io.github.torrydo:floating-bubble-view:0.6.3")
    implementation("com.github.skydoves:balloon:1.6.3")
    implementation("com.gusakov:pulse-countdown:1.1.0-rc1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")

}