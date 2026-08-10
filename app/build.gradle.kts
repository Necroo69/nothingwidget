plugins {
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.nothingwidget"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sritam.nothingwidget"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.coil.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi.kotlin)
    kapt(libs.moshi.kotlin.codegen)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.material:material:1.12.0")
    
    // Phase 1 architecture dependencies
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.workmanager.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
