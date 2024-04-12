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


-keep class cn.yanhu.imchat.bean.** { *; }