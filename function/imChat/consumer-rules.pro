
##环信
-keep class com.hyphenate.** {*;}
-dontwarn  com.hyphenate.**


-keep class cn.yanhu.imchat.bean.** { *; }

#华为推送
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
#荣耀推送
  -ignorewarnings
  -keepattributes *Annotation*
  -keepattributes Exceptions
  -keepattributes InnerClasses
  -keepattributes Signature
  -keepattributes SourceFile,LineNumberTable

#oppo推送
-keep public class * extends android.app.Service
-keep class com.heytap.msp.** { *;}
#vivo推送
-dontwarn com.vivo.push.**
-keep class com.vivo.push.**{*; }
-keep class com.vivo.vms.**{*; }
#小米推送
#下面可以不需要，环信SDK混淆逻辑中已包含相关
#-keep class com.hyphenate.push.platform.mi.EMMiMsgReceiver {*;}
#可以防止一个误报的 warning 导致无法成功编译，如果编译使用的 Android 版本是 23。
-dontwarn com.xiaomi.push.**
