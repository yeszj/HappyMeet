pluginManagement {
    repositories {
        google()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://plugins.gradle.org/m2/")
        maven("https://maven.aliyun.com/nexus/content/repositories/google")
        maven("https://maven.aliyun.com/nexus/content/groups/public")
        maven("https://maven.aliyun.com/nexus/content/repositories/jcenter")
        maven ("https://maven.rongcloud.cn/repository/maven-releases/")
        maven("https://developer.huawei.com/repo/")
        maven ("https://oss.jfrog.org/libs-snapshot")
        maven("https://repo1.maven.org/maven2/")
        maven("https://maven.aliyun.com/repository/public/")
    }
}

rootProject.name = "HappyMeet"
include(":app")
include(":dimens")
include(":netRequest")
include(":function:commonRes")
include(":function:baseLib")
include(":function:imChat")
include(":function:sdkLib")
include(":function:dynamic")
include(":function:agora")
