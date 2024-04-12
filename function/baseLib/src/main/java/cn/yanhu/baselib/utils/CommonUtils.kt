package cn.yanhu.baselib.utils

import android.app.ActivityManager
import android.content.Context
import android.text.TextUtils
import android.text.format.Formatter
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import cn.yanhu.baselib.R
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.lxj.xpopup.core.BasePopupView
import java.math.BigDecimal
import java.util.Calendar
import java.util.Date
import java.util.regex.Pattern

/**
 * @author: zhengjun
 * created: 2024/1/31
 * desc:
 */
object CommonUtils {

    fun getDimension(dimenId: Int): Int {
        val topActivity = ActivityUtils.getTopActivity()
        return topActivity?.resources?.getDimensionPixelSize(dimenId)
            ?: Utils.getApp().resources.getDimensionPixelSize(dimenId)
    }


    fun getSpByDimen(dimenId: Int): Int {
        val dimension: Int = getDimension(dimenId)
        return DisplayUtils.px2sp(dimension.toFloat())
    }

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
}