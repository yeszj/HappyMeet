
// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply(rootProject.file("config.gradle"))
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0") // ✅ 必须添加
    }
}
configurations.configureEach {
    resolutionStrategy{
        force("androidx.viewpager2:viewpager2:1.1.0")
    }
}
plugins {
    id("com.android.application") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.android.library") version "8.10.1" apply false
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
}
