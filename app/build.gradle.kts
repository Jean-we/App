import org.gradle.kotlin.dsl.implementation


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.jeansapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.jeansapplication"
        minSdk = 34
        targetSdk = 35
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
    // AndroidX 基础依赖
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // 测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // 组件依赖（替换占位符为实际版本）
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")
    implementation("androidx.compose.animation:animation:1.5.3")
    implementation("androidx.compose.runtime:runtime:1.5.3")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.compose.material3:material3:1.2.0-alpha03") // 使用稳定版本
    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // CameraX 依赖（统一版本）
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-video:1.3.1")

    // 媒体库依赖
    implementation("androidx.media:media:1.6.0")
    implementation("com.github.gkonovalov.android-vad:webrtc:2.0.9")

}
