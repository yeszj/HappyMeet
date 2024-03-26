package cn.yanhu.baselib.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

/**
 * @author: zhengjun
 * created: 2024/1/31
 * desc:
 */
object DateUtils {

    fun clearCalendarHour(calendar: Calendar) {
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MILLISECOND] = 0
    }

    fun secondToHMSTime(time: Long): String {
        return secondToHMSTime(time, "%02d:%02d:%02d")
    }

    @SuppressLint("DefaultLocale")
    fun secondConvertMinSecond(time: Long): String {
        val day = (time / 24 / 3600).toInt()
        val hour = (time / 3600).toInt()
        val minute = (time / 60 % 60).toInt()
        val second = (time % 60).toInt()
        return if (hour > 0) {
            String.format("%02d:%02d:%02d", hour + day * 24, minute, second)
        } else {
            String.format("%02d:%02d", minute, second)
        }
    }

    fun secondToHMSTime(time: Long, format: String?): String {
        var finalTime = time
        val day = (finalTime / 24 / 3600).toInt()
        finalTime %= (24 * 3600)
        val hour = (finalTime / 3600).toInt()
        val minute = (finalTime / 60 % 60).toInt()
        val second = (finalTime % 60).toInt()
        return String.format(format!!, hour + day * 24, minute, second)
    }

    fun isOutRideMinutes(time: Long, minutes: Int): Boolean {
        val second = (time / 1000).toInt()
        return second > minutes * 60
    }


    fun formatTimeStr(n: Long): String {
        if (n in 0..59) { // 一分钟以下
            return if (n < 10) {
                "00:00:0$n"
            } else {
                "00:00:$n"
            }
        } else if (n in 60..3599) {
            //一小时以下
            val minute = n / 60
            val sec = n % 60
            return if (minute < 10) {
                if (sec < 10) {
                    "00:0$minute:0$sec"
                } else {
                    "00:0$minute:$sec"
                }
            } else {
                if (sec < 10) {
                    "00:$minute:0$sec"
                } else {
                    "00:$minute:$sec"
                }
            }
        } else if (n >= 3600) {            // 一小时及以上
            val hour = n / 3600
            val minute = (n - 3600 * hour) / 60
            val sec = (n - 3600 * hour - 60 * minute) % 60
            return if (hour < 10) {
                if (minute < 10) {
                    if (sec < 10) {
                        "0$hour:0$minute:0$sec"
                    } else {
                        "0$hour:0$minute:$sec"
                    }
                } else {
                    if (sec < 10) {
                        "0$hour:$minute:0$sec"
                    } else {
                        "0$hour:$minute:$sec"
                    }
                }
            } else {
                if (minute < 10) {
                    if (sec < 10) {
                        "$hour:0$minute:0$sec"
                    } else {
                        "$hour:0$minute:$sec"
                    }
                } else {
                    if (sec < 10) {
                        "$hour:$minute:0$sec"
                    } else {
                        "$hour:$minute:$sec"
                    }
                }
            }
        }
        return "00:00:00"
    }

    fun isToday(time: Long): Boolean {
        return isSameOfDay(time, System.currentTimeMillis())
    }

    fun isSameOfDay(time1: Long, time2: Long): Boolean {
        val date = Date(time1)
        val date2 = Date(time2)
        val calendar = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        calendar.time = date //给calendar赋值
        val y1 = calendar[Calendar.YEAR] //获取年份
        val d1 = calendar[Calendar.DAY_OF_YEAR] //获取年中第几天
        calendar2.time = date2
        val y2 = calendar2[Calendar.YEAR]
        val d2 = calendar2[Calendar.DAY_OF_YEAR]
        return y1 == y2 && d1 == d2
    }

    fun getTodayStr(): String {
        val date = Date(System.currentTimeMillis())
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(date)
    }

    fun getTodayStr(format: String?): String {
        val date = Date(System.currentTimeMillis())
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat(format)
        return sdf.format(date)
    }

    fun getTodayMonthStr(): String? {
        val date = Date(System.currentTimeMillis())
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("yyyy-MM")
        return sdf.format(date)
    }

    /**
     * @param i         负数往前推几天 正数代表往后推几天
     * @param dayFormat
     * @return
     */
    fun getYestodyStr(i: Int, dayFormat: String?): String{
        var date = Date()
        @SuppressLint("SimpleDateFormat") val formatter = SimpleDateFormat(dayFormat)
        val calendar: Calendar = GregorianCalendar()
        calendar.time = date
        var dateString = ""
        try {
            calendar.add(Calendar.DATE, i) //把日期往后增加一天.整数往后推,负数往前移动
            date = calendar.time //这个时间就是日期往后推一天的结果
            dateString = formatter.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dateString
    }
}