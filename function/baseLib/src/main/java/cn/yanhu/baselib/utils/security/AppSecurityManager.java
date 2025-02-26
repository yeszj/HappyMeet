package cn.yanhu.baselib.utils.security;

import android.app.Activity;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;


/**
 * @author: witness
 * created: 2022/3/14
 * desc: app安全风险校验
 */
public class AppSecurityManager {

    /**
     * 1.解决正式环境动态调试安全
     * 2.解决so注入检测安全
     */
    private static boolean isLoop = true;

    public static void checkDynamicDebug() {
       // if (!BuildConfig.DEBUG) {
            Thread t = new Thread(() -> {
                checkSecurity();
                while (isLoop) {
                    try {
                        Thread.sleep(300);
                        checkSecurity();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, "SafeGuardThread");
            t.start();
       // }
    }

    private static void checkSecurity() {
        if (ActivityUtils.getActivityList().size() > 0) {
            Activity activity =ActivityUtils.getTopActivity();
            if (activity != null && !activity.isDestroyed()) {
                activity.runOnUiThread(() -> {
                    if (Debug.isDebuggerConnected()) {
                        ToastUtils.showShort("禁止动态调试，应用将在1s后自动关闭");
                        exitApp();
                    }
                    if (isUnderTraced()) {
                        ToastUtils.showShort("已被其它恶意进程跟踪，应用将在1s后自动关闭");
                        exitApp();
                    }
                    if (isEmulator()){
                        ToastUtils.showShort("禁止在模拟器中运行，应用将在1s后自动关闭");
                        exitApp();
                    }
                });
            }

        }

    }


    private static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    /**
     * 当我们使用Ptrace方式跟踪一个进程时目标进程会记录自己被谁跟踪，可以查看/proc/pid/status看到这个信息
     * 而没有被调试的时候TracerPid为0
     */
    private static boolean isUnderTraced() {
        String processStatusFilePath = String.format(Locale.US, "/proc/%d/status", android.os.Process.myPid());
        File procInfoFile = new File(processStatusFilePath);
        try {
            BufferedReader b = new BufferedReader(new FileReader(procInfoFile));
            String readLine;
            while ((readLine = b.readLine()) != null) {
                if (readLine.contains("TracerPid")) {
                    String[] arrays = readLine.split(":");
                    if (arrays.length == 2) {
                        int tracerPid = Integer.parseInt(arrays[1].trim());
                        if (tracerPid != 0) {
                            return true;
                        }
                    }
                }
            }
            b.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void exitApp() {
        isLoop = false;
        new Handler().postDelayed(() -> {
            int myPid = android.os.Process.myPid();
            android.os.Process.killProcess(myPid);
            //异常退出虚拟机
            System.exit(0);
        }, 1000);
    }
}
