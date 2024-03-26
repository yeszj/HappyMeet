package cn.yanhu.baselib.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


/**
 * 改变状态栏的颜色
 * @author woochen123
 * @time 2017/9/14 9:28
 * @desc
 */
object StatusBarUtil {

    private val isOverMiUi7  by lazy {
        getSystemProperty()
    }

    /**
     * 全透明状态栏
     */
    fun setStatusBarFullTransparent(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 21) {//21表示5.0
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= 19) {//19表示4.4
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            //虚拟键盘也透明
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

     fun hideBottomUIMenu(activity: Activity) {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            val v: View = activity.window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            val decorView: View = activity.window.decorView
            val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
            decorView.systemUiVisibility = uiOptions
        }
    }


    /**
     * 半透明状态栏
     */
    fun setHalfTransparent(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//21表示5.0
            val decorView = activity.window.decorView
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            decorView.systemUiVisibility = option
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//19表示4.4
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            //虚拟键盘也透明
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * 如果需要内容紧贴着StatusBar
     * 应该在对应的xml布局文件中，设置根布局 fitsSystemWindows=true。
     */
    fun setFitSystemWindow(activity: Activity, fitSystemWindow: Boolean) {
        val contentViewGroup = (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
        contentViewGroup.fitsSystemWindows = fitSystemWindow
    }


    /**
     * 设置状态栏颜色
     * @param activity
     * @param color
     */
    @TargetApi(19)
    fun statusBarTintColor(activity: Activity, color: Int) {
        //代表5.0及以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = activity.applicationContext.resources.getColor(color)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //versionCode > 4.4 and versionCode < 5.0
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            val androidContainer = activity.findViewById<View>(android.R.id.content) as ViewGroup
            //留出高度 setFitsSystemWindows true代表会调整布局，会把状态栏的高度留出来
            val contentView = androidContainer.getChildAt(0)
            if (contentView != null) {
                contentView.fitsSystemWindows = true
            }
            //在原来的位置上添加一个状态栏
            val statusBarView = createStatusBarView(activity)
            androidContainer.addView(statusBarView, 0)
            statusBarView.setBackgroundColor(color)
        }
    }
    fun setWindowStatusBarColor(activity: Activity, colorResId: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = activity.resources.getColor(colorResId)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    /**
     * 创建一个需要填充的statusBarView
     * @param activity
     * @return
     */
    private fun createStatusBarView(activity: Activity): View {
        val statusBarView = View(activity)
        val statusBarParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity)
        )
        statusBarView.layoutParams = statusBarParams
        return statusBarView
    }

    /**
     * 获取状态栏的高度
     * @param context
     * @return
     */
     fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }


    /**
     * 状态栏亮色模式，设置状态栏黑色文字、图标，
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     * @return 1:MIUUI 2:Flyme 3:android6.0
     */
    fun StatusBarLightMode(activity: Activity): Int {
        var result = 0
        if (MIUISetStatusBarLightMode(activity, true)) {
            result = 1
        } else if (FlymeSetStatusBarLightMode(activity, true)) {
            result = 2
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            result = 3
        }
        return result
    }


    /**
     * 状态栏暗色模式，清除MIUI、flyme或6.0以上版本状态栏黑色文字、图标
     */
    fun StatusBarDarkMode(activity: Activity, type: Int) {
        if (type == 1) {
            MIUISetStatusBarLightMode(activity, false)
        } else if (type == 2) {
            FlymeSetStatusBarLightMode(activity, false)
        } else if (type == 3) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_VISIBLE
            }

        }
    }




    /**
     * 设置状态栏字体图标为深色，需要MIUIV6以上
     * @param dark 是否把状态栏字体及图标颜色设置为深色
     * @return  boolean 成功执行返回true
     */
    fun MIUISetStatusBarLightMode(activity: Activity, dark: Boolean): Boolean {
        val window = activity.window
        var result = false
        if (window != null) {
            val clazz = window.javaClass
            try {
                var darkModeFlag = 0
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag)//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag)//清除黑色字体
                }
                result = true
                if (isOverMiUi7 &&Build.VERSION.SDK_INT >= Build.VERSION_CODES.M  ) {
                    //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                    if (dark) {
                        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    }
                }
            } catch (e: Exception) {

            }

        }
        return result
    }



    fun getSystemProperty(): Boolean {
        val line: String
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop " + "ro.miui.ui.version.code")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
            if (line != null) {
                val code = Integer.parseInt(line)
                return code >= 7
            } else {
                return false
            }
        } catch (ex: IOException) {
            return false
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                }

            }
        }
        return false
    }

    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     * @param dark 是否把状态栏字体及图标颜色设置为深色
     * @return  boolean 成功执行返回true
     */
    fun FlymeSetStatusBarLightMode(activity: Activity, dark: Boolean): Boolean {
        val window = activity.window
        var result = false
        if (window != null) {
            try {
                val lp = window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java
                        .getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                if (dark) {
                    value = value or bit
                } else {
                    value = value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                window.attributes = lp
                result = true
            } catch (e: Exception) {

            }

        }
        return result
    }


}
