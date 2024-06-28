plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.just_plant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.just_plant"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


        renderscriptTargetApi=18
        renderscriptSupportModeEnabled=true

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


    }


dependencies {

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation ("com.google.firebase:firebase-firestore:24.11.1")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage")
    // This library is used for crop image feature



    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.glide)
    implementation(libs.recyclerview)
    implementation(libs.picasso)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}