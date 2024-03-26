plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}
val cfg = rootProject.ext
android {
    namespace = "com.zj.dimens"
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
}

dependencies {

    implementation("androidx.core:core-ktx:${cfg["ktxVersion"]}")
    implementation("androidx.appcompat:appcompat:${cfg["appcompatVersion"]}")
    implementation("com.google.android.material:material:${cfg["materialVersion"]}")
}