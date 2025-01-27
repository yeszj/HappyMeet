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
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar","*.aar"))))
    //noinspection GradleDependency
    implementation("androidx.core:core-ktx:${cfg["ktxVersion"]}")
    implementation("androidx.appcompat:appcompat:${cfg["appcompatVersion"]}")
    implementation("com.google.android.material:material:${cfg["materialVersion"]}")
    testImplementation("junit:junit:${cfg["junitVersion"]}")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    api("com.tencent.map.geolocation:TencentLocationSdk-openplatform:7.5.4")
    api("cn.jiguang.sdk:jverification:3.2.5")
    implementation(project(mapOf("path" to ":function:commonRes")))
    api("com.alipay.sdk:alipaysdk-android:+@aar")
    //微信
    api("com.tencent.mm.opensdk:wechat-sdk-android:+")
    api("com.umeng.umsdk:share-wx:7.3.5")
    api("com.umeng.umsdk:share-core:7.3.5")
    // 友盟统计SDK
    api("com.umeng.umsdk:common:9.8.0")// 必选
    api("com.umeng.umsdk:asms:1.8.6")// 必选
    api("com.umeng.umsdk:apm:2.0.1")// 必选
    implementation(project(mapOf("path" to ":function:localRepo:baidu_face_aar")))
    implementation(project(mapOf("path" to ":function:localRepo:baidu_face_aar2")))
    implementation(project(mapOf("path" to ":function:localRepo:baidu_face_aar3")))

}