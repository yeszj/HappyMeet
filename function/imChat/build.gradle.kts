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
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar","*.aar"))))
    implementation("androidx.core:core-ktx:${cfg["ktxVersion"]}")
    implementation("androidx.appcompat:appcompat:${cfg["appcompatVersion"]}")
    implementation("com.google.android.material:material:${cfg["materialVersion"]}")
    testImplementation("junit:junit:${cfg["junitVersion"]}")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(project(mapOf("path" to ":function:commonRes")))
    implementation(project(mapOf("path" to ":dimens")))
    kapt("com.github.bumptech.glide:compiler:${rootProject.ext.get("glide")}")

    implementation("com.meizu.flyme.internet:push-internal:4.1.0")
    implementation("com.huawei.agconnect:agconnect-core:1.8.1.300")
    kapt("com.alibaba:arouter-compiler:${rootProject.ext.get("arouter")}")
    api(project(mapOf("path" to ":function:ease-im-kit")))
    api("io.hyphenate:hyphenate-chat:4.5.0")
    //荣耀推送
    api("com.hihonor.mcs:push:7.0.61.303")
    //华为推送
    implementation("com.huawei.hms:push:6.12.0.300")
    api("commons-codec:commons-codec:1.15")
    //七鱼客服
    implementation("com.qiyukf.unicorn:unicorn:9.1.0")
    implementation(project(mapOf("path" to ":function:localRepo:mi_push_aar")))
    implementation(project(mapOf("path" to ":function:localRepo:oppo_push_aar")))


}