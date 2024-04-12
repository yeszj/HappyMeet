plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "cn.yanhu.baselib"
    compileSdk = 33

    defaultConfig {
        minSdk = 24

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

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    api(project(mapOf("path" to ":netRequest")))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    api("com.kingja.loadsir:loadsir:${rootProject.ext.get("loadsir")}")
    api("com.github.yyued:SVGAPlayer-Android:2.6.1")
    api(project(mapOf("path" to ":dimens")))
    val smartRefreshVersion = rootProject.ext.get("smartRefresh")
    //https://github.com/scwang90/SmartRefreshLayout
    api("io.github.scwang90:refresh-layout-kernel:${smartRefreshVersion}")      //核心必须依赖
    // api("io.github.scwang90:refresh-header-classics:${smartRefreshVersion}")    //经典刷新头
    api("io.github.scwang90:refresh-footer-classics:${smartRefreshVersion}")      //经典加载
    api("io.github.scwang90:refresh-header-material:${smartRefreshVersion}")     //谷歌刷新头
    api("me.weishu:free_reflection:3.0.1")
    api("com.github.gzu-liyujiang:Android_CN_OAID:4.2.4")
    //https://github.com/CymChad/BaseRecyclerViewAdapterHelper
    api("io.github.cymchad:BaseRecyclerViewAdapterHelper4:4.1.4")
    //https://github.com/li-xiaojun/XPopup/
    api("com.github.li-xiaojun:XPopup:2.10.0")
    api("com.github.bumptech.glide:glide:${rootProject.ext.get("glide")}")
    annotationProcessor("com.github.bumptech.glide:compiler:${rootProject.ext.get("glide")}")
    //https://github.com/wasabeef/glide-transformations
    api("jp.wasabeef:glide-transformations:4.3.0")
    //https://github.com/lihangleo2/ShadowLayout
    api("com.github.lihangleo2:ShadowLayout:3.4.0")

    //https://gitcode.com/youth5201314/banner/overview
    api("io.github.youth5201314:banner:2.2.2")

    //https://github.com/hackware1993/MagicIndicator/tree/1.7.0
    api("com.github.hackware1993:MagicIndicator:1.7.0")
    //https://github.com/xiaoxiaAndroid/BottomBarLayout?tab=readme-ov-file
    api("com.github.chaychan:BottomBarLayout:2.0.2")

    api("com.guolindev.permissionx:permissionx:1.7.1")

    api("io.github.lucksiege:pictureselector:v3.11.2")
    // 图片压缩 (按需引入)
    implementation("io.github.lucksiege:compress:v3.11.1")
    api("io.github.lucksiege:ucrop:v3.11.1")

    api("androidx.core:core-splashscreen:1.1.0-alpha02")
    api("com.github.getActivity:ToastUtils:10.5")
    api("com.alibaba:arouter-api:${rootProject.ext.get("arouter")}")

    api("com.makeramen:roundedimageview:2.3.0")


    api("pl.droidsonroids.gif:android-gif-drawable:1.2.27")

    api("io.github.jeremyliao:live-event-bus-x:1.8.0")

    //选择器
    api("com.github.gzu-liyujiang.AndroidPicker:WheelPicker:4.1.11")

    api("androidx.room:room-runtime:2.6.1")
    //noinspection KaptUsageInsteadOfKsp
    kapt("androidx.room:room-compiler:2.6.1")
    api("com.cpiz.bubbleview:bubbleview:1.0.2")
    api("com.hyman:flowlayout-lib:1.1.2")
    api("net.yslibrary.keyboardvisibilityevent:keyboardvisibilityevent:3.0.0-RC3")
    //https://github.com/getActivity/Logcat
    debugImplementation("com.github.getActivity:Logcat:11.86")
    api("com.github.getActivity:EasyWindow:10.6")
}