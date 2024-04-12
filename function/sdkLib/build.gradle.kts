plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}
val cfg = rootProject.ext
android {
    namespace = "com.pcl.sdklib"
    compileSdk = cfg["compileSdkVersion"] as Int

    defaultConfig {
        minSdk = cfg["minSdkVersion"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:${cfg["ktxVersion"]}")
    implementation("androidx.appcompat:appcompat:${cfg["appcompatVersion"]}")
    implementation("com.google.android.material:material:${cfg["materialVersion"]}")
    testImplementation("junit:junit:${cfg["junitVersion"]}")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    api("com.tencent.map.geolocation:TencentLocationSdk-openplatform:7.5.4")
    api("cn.jiguang.sdk:jverification:3.1.9")
    implementation(project(mapOf("path" to ":function:commonRes")))
    api("com.alipay.sdk:alipaysdk-android:+@aar")
    //微信
    api("com.tencent.mm.opensdk:wechat-sdk-android:+")
    api("com.umeng.umsdk:share-wx:7.3.3")
    api("com.umeng.umsdk:share-core:7.3.3")
    // 友盟统计SDK
    api("com.umeng.umsdk:common:9.6.7")// 必选
    api("com.umeng.umsdk:asms:1.8.0")// 必选

    //七鱼客服
    api("com.qiyukf.unicorn:unicorn:9.1.0")
}