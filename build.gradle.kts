
// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply(rootProject.file("config.gradle"))
plugins {
    id("com.android.application") version "8.1.3" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.android.library") version "8.1.3" apply false
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
}
