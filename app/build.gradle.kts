plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.nhom15_roomfinder"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nhom15_roomfinder"
        minSdk = 24
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
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Material Design
//    implementation 'com.google.android.material:material:1.9.0'
//
//    // RecyclerView
//    implementation 'androidx.recyclerview:recyclerview:1.3.1'
//
//    // CardView
//    implementation 'androidx.cardview:cardview:1.0.0'
//
//    // Glide for image loading
//    implementation 'com.github.bumptech.glide:glide:4.15.1'
//    annotationProcessor 'com.github.bumptech.glide:compiler:4.15.1'
//
//    // Coordinator Layout
//    implementation 'androidx.coordinatorlayout:coordinatorlayout:1.2.0'
}