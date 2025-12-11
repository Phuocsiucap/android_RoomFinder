plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
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
        manifestPlaceholders["MAPBOX_ACCESS_TOKEN"] =
            project.properties["MAPBOX_DOWNLOADS_TOKEN"]?.toString() ?: ""
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
//    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase BOM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    
    // Firebase Services
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    
    // Google Play Services Auth (for Google Sign-In)
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.android.material:material:1.9.0")
    
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    
    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
    // OkHttp for Cloudinary upload
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // PhotoView for zoom image
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.mapbox.maps:android:11.16.6")

}