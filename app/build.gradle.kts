import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    val mode = "dev"

    val propertiesFile = rootProject.file("app/properties/${mode}.properties")
    val properties = Properties()
    properties.load(FileInputStream(propertiesFile))

    signingConfigs {
        if (mode == "test" || mode == "dev") {
            getByName("debug") {
                storeFile = rootProject.file("app/keystore")
                storePassword = "ara-password"
                keyPassword = "ara-password"
                keyAlias = "ara-key"
            }
            create("release") {
                storeFile = rootProject.file("app/keystore")
                storePassword = "ara-password"
                keyPassword = "ara-password"
                keyAlias = "ara-key"
            }
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas/$mode")
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas/$mode")
    }

    namespace = "com.dox.ara"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dox.ara"
        minSdk = 28
        targetSdk = 34
        versionCode = properties["VERSION_CODE"].toString().toInt()
        versionName = properties["VERSION_NAME"].toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField (
            "String",
            "ENCRYPTION_KEY",
            properties["ENCRYPTION_KEY"].toString()
        )

        buildConfigField (
            "String",
            "BASE_URL",
            properties["BASE_URL"].toString()
        )

        buildConfigField (
            "String",
            "MODE",
            properties["MODE"].toString()
        )
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.activity:activity-compose:1.9.0")

    val lifecycleVersion = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")

    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("androidx.wear.compose:compose-material:1.3.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    val hiltVersion = "2.49"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")

    val hiltWorkerVersion = "1.2.0"
    implementation("androidx.hilt:hilt-work:$hiltWorkerVersion")
    implementation("androidx.hilt:hilt-navigation-compose:$hiltWorkerVersion")
    ksp("androidx.hilt:hilt-compiler:$hiltWorkerVersion")

    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")

    val okHttpVersion = "4.11.0"
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation ("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    testImplementation("androidx.room:room-testing:$roomVersion")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")

    val pagingVersion = "3.3.0"
    implementation("androidx.paging:paging-runtime-ktx:${pagingVersion}")
    implementation("androidx.paging:paging-compose:${pagingVersion}")

    val coroutineVersion = "1.7.3"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion")

    val dexterVersion = "6.2.3"
    implementation("com.karumi:dexter:$dexterVersion")

    val systemUIControllerVersion = "0.33.1-alpha"
    implementation("com.google.accompanist:accompanist-systemuicontroller:$systemUIControllerVersion")

    val firebaseVersion = "32.5.0"
    implementation(platform("com.google.firebase:firebase-bom:$firebaseVersion"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    val workVersion = "2.9.0"
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    val coilImageVersion = "2.6.0"
    implementation("io.coil-kt:coil-compose:$coilImageVersion")

    val cameraxVersion= "1.3.3"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    val media3Version = "1.3.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")

    implementation("com.airbnb.android:lottie-compose:6.1.0")

    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation("com.github.skydoves:colorpicker-compose:1.0.8")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
}