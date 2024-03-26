-keep class xyz.doikki.videoplayer.** { *; }
-dontwarn xyz.doikki.videoplayer.**

# IjkPlayer
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**

# ExoPlayer
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

## NIM SDK 防混淆
-dontwarn com.netease.nim.**
-keep class com.netease.nim.** {*;}

-dontwarn com.netease.nimlib.**
-keep class com.netease.nimlib.** {*;}

-dontwarn com.netease.share.**
-keep class com.netease.share.** {*;}

-dontwarn com.netease.mobsec.**
-keep class com.netease.mobsec.** {*;}

## 如果你使用全文检索插件，需要加入
-dontwarn org.apache.lucene.**
-keep class org.apache.lucene.** {*;}

## IM UIKit 防混淆
-dontwarn com.netease.yunxin.kit.**
-keep class com.netease.yunxin.kit.** {*;}
-keep public class * extends com.netease.yunxin.kit.corekit.XKitInitOptions
-keep class * implements com.netease.yunxin.kit.corekit.XKitService {*;}

## 呼叫组件防混淆
-keep class com.netease.lava.** {*;}
-keep class com.netease.yunxin.** {*;}

-dontwarn com.netease.yunxin.kit.**
-keep class com.netease.yunxin.kit.** {*;}
-keep public class * extends com.netease.yunxin.kit.corekit.XKitInitOptions
-keep class * implements com.netease.yunxin.kit.corekit.XKitService {*;}

## glide 4
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

## okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

## 如果你使用全文检索插件，需要加入
-dontwarn org.apache.lucene.**
-keep class org.apache.lucene.** {*;}

## 如果你开启数据库功能，需要加入
-keep class net.sqlcipher.** {*;}

-keep class cn.yanhu.commonres.bean.** { *; }

