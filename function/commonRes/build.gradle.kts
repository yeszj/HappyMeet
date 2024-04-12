plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "cn.yanhu.commonres"
    compileSdk = 33

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
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

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    api(project(mapOf("path" to ":function:baseLib")))
    //https://github.com/Doikki/DKVideoPlayer
    api("xyz.doikki.android.dkplayer:dkplayer-java:3.3.7")
    api("xyz.doikki.android.dkplayer:dkplayer-ui:3.3.7")
    api("xyz.doikki.android.dkplayer:player-ijk:3.3.7")
    api("xyz.doikki.android.dkplayer:videocache:3.3.7")
    //api("xyz.doikki.android.dkplayer:player-exo:3.3.7")


    api ("com.github.jd-alexander:LikeButton:0.2.3")
    kapt ("androidx.room:room-compiler:2.6.1")

    api ("com.google.zxing:core:3.3.3")

    //https://github.com/airbnb/lottie-android
    api ("com.airbnb.android:lottie:6.4.0")

    //https://gitcode.com/lihangleo2/ViewPager2Demo/overview
    api("com.github.lihangleo2:SmartViewPager2Adapter:2.1.3")

}