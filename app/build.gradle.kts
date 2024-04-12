
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}
kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
}
val cfg = rootProject.ext
val keyName = "meetKey"
android {

    namespace = "cn.huanyuan.happymeet"
    compileSdk = cfg["compileSdkVersion"] as Int

    defaultConfig {
        applicationId = "cn.huanyuan.happymeet"
        minSdk = cfg["minSdkVersion"] as Int
        targetSdk = cfg["targetSdkVersion"] as Int
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
//            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
       // resourceConfigurations.add("zh")
        manifestPlaceholders["JPUSH_APPKEY"] = "4035ab3342a72a02d9de14bd"
        manifestPlaceholders["JPUSH_PKGNAME"] = applicationId!!
        manifestPlaceholders["JPUSH_CHANNEL"] = "developer-default"
    }

    signingConfigs {
        create(keyName) {
            keyAlias = "meet"
            keyPassword = "123456"
            storeFile = file("../meet.jks")
            storePassword = "123456"
        }
    }

    buildTypes {
        debug {
            isShrinkResources = false
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName(keyName)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(
                "String",
                "BASE_SERVER_ADDRES",
                "\"https://mock.apifox.com/m1/3995814-0-default/\""
            )
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName(keyName)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_SERVER_ADDRES", "\"https://app.sengos.com/app/v1/\"")
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
        buildConfig = true
        dataBinding = true
    }
    flavorDimensions += listOf("meetDefault")
    productFlavors {
        create("meetDefault") {
            dimension = "meetDefault"
        }
    }
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar","*.aar"))))
    implementation("androidx.core:core-ktx:${cfg["ktxVersion"]}")
    implementation("androidx.appcompat:appcompat:${cfg["appcompatVersion"]}")
    implementation("com.google.android.material:material:${cfg["materialVersion"]}")
    implementation(project(mapOf("path" to ":function:dynamic")))
    testImplementation("junit:junit:${cfg["junitVersion"]}")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(project(mapOf("path" to ":function:commonRes")))
    implementation(project(mapOf("path" to ":function:imChat")))
    implementation(project(mapOf("path" to ":function:sdkLib")))
    implementation(project(mapOf("path" to ":function:agora")))
    api("com.alibaba:arouter-api:${rootProject.ext.get("arouter")}")
    // https://github.com/alibaba/ARouter
    kapt ("com.alibaba:arouter-compiler:${rootProject.ext.get("arouter")}")
    kapt ("androidx.room:room-compiler:2.6.1")
}