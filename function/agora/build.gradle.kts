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
    namespace = "cn.yanhu.agora"
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
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }
}


dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar","*.aar"))))
    implementation("androidx.core:core-ktx:${cfg["ktxVersion"]}")
    implementation("androidx.appcompat:appcompat:${cfg["appcompatVersion"]}")
    implementation("com.google.android.material:material:${cfg["materialVersion"]}")
    testImplementation("junit:junit:${cfg["junitVersion"]}")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(project(mapOf("path" to ":function:commonRes")))
    kapt ("com.alibaba:arouter-compiler:${rootProject.ext.get("arouter")}")
    //api("io.agora.rtc:full-rtc-basic:4.3.0")
    api("io.agora.rtc:agora-special-full:4.1.1.28")
    implementation(project(mapOf("path" to ":function:sdkLib")))
    implementation(project(mapOf("path" to ":function:imChat")))

}