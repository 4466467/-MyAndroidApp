plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

}

android {
    namespace = "com.example.ourbookapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.ourbookapplication"
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
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    //implementation("androidx.appcompat:appcompat:1.6.1")
    // 其他依赖...
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")

    // 高德地图SDK - 使用兼容版本组合
    //implementation("com.amap.api:3dmap:10.0.600")
    //implementation("com.amap.api:search:9.7.1")
    //implementation("com.amap.api:location:6.5.1")    // 定位SDK
    //implementation("com.amap.api:cluster:latest.integration")

        // 使用相同版本号，避免兼容性问题
    implementation("com.amap.api:3dmap:9.8.2")
    //implementation("com.amap.api:location:6.2.0")
    implementation("com.amap.api:search:9.7.0")
    //implementation("com.amap.api:cluster:1.0.1") // 检查最新版本号


    // 确保使用Kotlin DSL语法
    implementation("androidx.core:core-ktx:1.12.0")
    // 材料设计组件
    //implementation("com.google.android.material:material:1.10.0")
// 约束布局
    //implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}




