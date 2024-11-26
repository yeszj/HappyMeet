package cn.yanhu.baselib.utils

import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.text.format.Formatter
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import cn.yanhu.baselib.R
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.lxj.xpopup.core.BasePopupView
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.Date
import java.util.regex.Pattern

/**
 * @author: zhengjun
 * created: 2024/1/31
 * desc:
 */
object CommonUtils {
    @JvmStatic
    fun isScreenOff(): Boolean {
        val topActivity = ActivityUtils.getTopActivity() ?: return false
        val flag = topActivity
            .getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return flag.isKeyguardLocked
    }

    fun formatRoseNum(roseNum:String):String{
        return if (compareString(roseNum,"1000")){
            val toString = divideString(roseNum, "1000").toString()
            val format = BigDecimal(toString).setScale(1,RoundingMode.DOWN).toPlainString()
            subZeroAndDot(format) +"k"
        }else{
            roseNum
        }
    }

    fun subZeroAndDot(s: String): String {
//        KLog.i("进入" + s);
        var s = s
        if (s.indexOf(".") > 0) {
            s = s.replace("0+?$".toRegex(), "") //去掉多余的0
            s = s.replace("[.]$".toRegex(), "") //如最后一位是.则去掉
        }
        //        KLog.i("离开" + s);
        return s
    }

    @JvmStatic
    fun <T>reverseList(list: MutableList<T>){
        list.reverse()
    }
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        // 取 drawable 的长宽
        val w = drawable.intrinsicWidth
        val h = drawable.intrinsicHeight

        // 取 drawable 的颜色格式
        val config =
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        // 建立对应 bitmap
        val bitmap = Bitmap.createBitmap(w, h, config)
        // 建立对应 bitmap 的画布
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        // 把 drawable 内容画到画布中
        drawable.draw(canvas)
        return bitmap
    }
    @JvmStatic
    fun getDimension(dimenId: Int): Int {
        val topActivity = ActivityUtils.getTopActivity()
        return topActivity?.resources?.getDimensionPixelSize(dimenId)
            ?: Utils.getApp().resources.getDimensionPixelSize(dimenId)
    }

    @JvmStatic
    fun getSpByDimen(dimenId: Int): Int {
        val dimension: Int = getDimension(dimenId)
        return DisplayUtils.px2sp(dimension.toFloat())
    }
    @JvmStatic
    fun getColor(colorId: Int): Int {
        return ContextCompat.getColor(ActivityUtils.getTopActivity(), colorId)
    }


    /**
     * 返回app运行状态
     *
     * @param context     一个context
     * @param packageName 要判断应用的包名
     * @return int 1:前台 2:后台 0:不存在
     */
    fun isAppAlive(context: Context, packageName: String): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val listInfos = activityManager.getRunningTasks(20)
        // 判断程序是否在栈顶
        return if (listInfos[0].topActivity!!.packageName == packageName) {
            1
        } else {
            // 判断程序是否在栈里
            for (info in listInfos) {
                if (info.topActivity!!.packageName == packageName) {
                    return 2
                }
            }
            0 // 栈里找不到，返回0
        }
    }


    /**
     * 根据文件大小自动转化为KB, MB, GB
     */
    fun formatSize(context: Context?, target_size: Long): String? {
        return Formatter.formatFileSize(context, target_size)
    }

    fun compareString(startValue: String?, endValue: String?): Boolean {
        return BigDecimal(startValue) >= BigDecimal(endValue)
    }

    fun compareString(startValue: String?, endValue: BigDecimal?): Boolean {
        return BigDecimal(startValue) > endValue
    }

    fun compareZero(startValue: String?): Boolean {
        var finalValue = startValue
        if (TextUtils.isEmpty(startValue)) {
            finalValue = "0"
        }
        return BigDecimal(finalValue) > BigDecimal(0)
    }

    fun multiplyString(startValue: String?, endValue: String?): String? {
        return BigDecimal(startValue).multiply(BigDecimal(endValue)).stripTrailingZeros()
            .toPlainString()
    }

    fun divideString(startValue: String?, endValue: String?): String? {
        return BigDecimal(startValue).divide(BigDecimal(endValue)).stripTrailingZeros()
            .toPlainString()
    }

    fun addString(startValue: String?, endValue: String?): String? {
        return BigDecimal(startValue).add(BigDecimal(endValue)).toPlainString()
    }

    fun subString(startValue: String?, endValue: String?): String? {
        return BigDecimal(startValue).subtract(BigDecimal(endValue)).toPlainString()
    }

    private var touchTime: Long = 0
    private const val WAIT_TIME: Long = 2000

    fun exitApp(keyCode: Int): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - touchTime >= WAIT_TIME) {
                ToastUtils.showShort(getString(R.string.tips_exit))
                touchTime = currentTime
                true
            } else {
                false
            }
        } else {
            true
        }
    }

    fun getUrlLastValue(url: String): String {
        val index = url.lastIndexOf("/")
        return url.substring(index + 1)
    }

    @JvmStatic
    fun getString(stringId: Int): String? {
        val topActivity = ActivityUtils.getTopActivity()
        return topActivity?.getString(stringId) ?: StringUtils.getString(stringId)
    }

    //由出生日期获得年龄
    fun getAge(birthDay: Date, context: Context): Int {
        val cal = Calendar.getInstance()
        if (cal.before(birthDay)) {
            Toast.makeText(context, "出生日期不能在当今日期之后！", Toast.LENGTH_SHORT).show()
            return -1
        }
        val yearNow = cal[Calendar.YEAR]
        val monthNow = cal[Calendar.MONTH]
        val dayOfMonthNow = cal[Calendar.DAY_OF_MONTH]
        cal.time = birthDay
        val yearBirth = cal[Calendar.YEAR]
        val monthBirth = cal[Calendar.MONTH]
        val dayOfMonthBirth = cal[Calendar.DAY_OF_MONTH]
        var age = yearNow - yearBirth
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) age--
            } else {
                age--
            }
        }
        return age
    }


    fun isInteger(str: String): Boolean {
        val pattern = Pattern.compile("^[-+]?\\d*$")
        return pattern.matcher(str).matches()
    }

    fun isPopShow(pop: BasePopupView?): Boolean {
        if (pop!=null && pop.isShow){
            return true
        }
        return false
    }

    @JvmOverloads
    fun EditText.disableCopy(disablePaste: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val callback = object : ActionMode.Callback, android.view.ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    //创建菜单项ActionMode，返回false表示不创建，事件结束
                    return false
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    //菜单项更新，返回false表示没有更新，事件结束
                    return false
                }

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    //选择菜单项的某一项，返回true表示拦截点击事件，事件结束
                    return true
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                    //菜单项ActionMode销毁，什么也不做
                }

                override fun onCreateActionMode(
                    mode: android.view.ActionMode?,
                    menu: Menu?
                ): Boolean {
                    return false
                }

                override fun onPrepareActionMode(
                    mode: android.view.ActionMode?,
                    menu: Menu?
                ): Boolean {
                    return false
                }

                override fun onActionItemClicked(
                    mode: android.view.ActionMode?,
                    item: MenuItem?
                ): Boolean {
                    return true
                }
                override fun onDestroyActionMode(mode: android.view.ActionMode?) {
                }
            }
            //禁用复制
            this.customSelectionActionModeCallback = callback
            //禁用粘贴
            if (disablePaste)
                this.customInsertionActionModeCallback = callback
        } else {
            //通过反射来禁用复制和粘贴
            disableCopyAndPasteByReflect(disablePaste)
        }
    }

    private fun EditText.disableCopyAndPasteByReflect(disablePaste: Boolean) {
        try {
            //因为Editor包含两个boolean字段： mInsertionControllerEnabled、mSelectionControllerEnabled，
            // 其中mInsertionControllerEnabled表示粘贴，为false就不起作用
            // 其中mSelectionControllerEnabled表示选中文本，为false就不起作用
            //所以，获取Editor之后，将这两个boolean字段设为false就可以禁用复制和粘贴

            //1、获取EditText的Editor实例
            val editorField = TextView::class.java.getDeclaredField("mEditor")
            editorField.isAccessible = true
            val editorInstance = editorField.get(this)

            val editorClass = Class.forName("android.widget.Editor")
            //2、获取Editor的mSelectionControllerEnabled字段，设为false禁用复制
            val selectField = editorClass.getDeclaredField("mSelectionControllerEnabled")
            selectField.isAccessible = true
            selectField.set(editorInstance, false)
            //3、获取Editor的mInsertionControllerEnabled字段，设为false禁用粘贴
            if (disablePaste) {
                val insertField = editorClass.getDeclaredField("mInsertionControllerEnabled")
                insertField.isAccessible = true
                insertField.set(editorInstance, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}