plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}
val cfg = rootProject.ext
android {
    namespace = "cn.zj.netrequest"
    compileSdk = cfg["compileSdkVersion"] as Int

    defaultConfig {
        minSdk = cfg["minSdkVersion"] as Int
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures {
        buildConfig  = true
        dataBinding = true
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
}

dependencies {

    implementation("androidx.core:core-ktx:${cfg["ktxVersion"]}")
    implementation("androidx.appcompat:appcompat:${cfg["appcompatVersion"]}")
    implementation("com.google.android.material:material:${cfg["materialVersion"]}")
    testImplementation("junit:junit:${cfg["junitVersion"]}")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    api ("com.squareup.retrofit2:retrofit:${cfg["retrofit2"]}")
    api ("com.squareup.retrofit2:converter-gson:${cfg["converterGson"]}")
    api ("com.jakewharton.rxbinding4:rxbinding:${cfg["rxbinding4"]}")
    api ("com.squareup.okhttp3:logging-interceptor:${cfg["logingInterceptor"]}")
    api ("androidx.lifecycle:lifecycle-viewmodel-ktx:${cfg["viewmodelKtx"]}")
    api ("com.google.code.gson:gson:${cfg["gson"]}")
    //工具库
    api ("com.blankj:utilcodex:1.31.1")
    implementation ("com.github.getActivity:ToastUtils:10.5")

}