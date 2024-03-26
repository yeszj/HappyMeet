plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}
kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
}
val cfg = rootProject.ext
android {
    namespace = "cn.yanhu.imchat"
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
    implementation(project(mapOf("path" to ":function:commonRes")))
    implementation("com.huawei.hms:push:6.12.0.300")
    implementation("com.meizu.flyme.internet:push-internal:4.1.0")
    implementation("com.huawei.agconnect:agconnect-core:1.8.1.300")
    kapt ("com.alibaba:arouter-compiler:${rootProject.ext.get("arouter")}")
}