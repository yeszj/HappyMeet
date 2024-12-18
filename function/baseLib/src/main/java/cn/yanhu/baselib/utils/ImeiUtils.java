package cn.yanhu.baselib.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.SPUtils;


public class ImeiUtils {

    @SuppressLint("HardwareIds")
    public static String getIMEIDeviceId(Context context) {
        String imei = SPUtils.getInstance().getString("imei", "");
        if (!TextUtils.isEmpty(imei)){
            return imei;
        }
        String deviceId;
        //当APK运行在Android10（API>=29）及以上时，获取到的是AndroidID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = ImeiUtils.getAndroidId();
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //当APK运行在Android6.0（API>=23）及以上时，需要check有无READ_PHONE_STATE权限。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "";
                }
            }
            //如果TelephonyManager获取到的DeviceId不为null
            if (mTelephony!=null && !TextUtils.isEmpty(mTelephony.getDeviceId())) {
                //获取GSM手机的国际移动设备识别码（IMEI）或者 CDMA手机的移动设备识别码（MEID).
                deviceId = mTelephony.getDeviceId();
            } else {
                //如果DeviceId为null，我们的DID依然是AndroidID。
                deviceId = ImeiUtils.getAndroidId();
            }
        }
        SPUtils.getInstance().put("imei", deviceId);
        return deviceId;
    }


    public static String getAndroidId() {
        String androidID = SPUtils.getInstance().getString("androidID", "");
        if (TextUtils.isEmpty(androidID)) {
            androidID = DeviceUtils.getAndroidID();
            SPUtils.getInstance().put("androidID", androidID);
        }
        return androidID;
    }
}
