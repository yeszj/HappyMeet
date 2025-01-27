package com.hyphenate.easeui.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import com.hyphenate.util.TimeInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EaseDateUtils {

	private static final long INTERVAL_IN_MILLISECONDS = 30 * 1000;

	public static String getTimestampString(Context context, Date messageDate) {
		String format = null;
		String language = Locale.getDefault().getLanguage();
		boolean isZh = language.startsWith("zh");
		long messageTime = messageDate.getTime();
		if (isSameDay(messageTime)) {
			return formatTimeAgo(messageTime);
			//format = "HH:mm";
		} else if (isYesterday(messageTime)) {
			if (isZh) {
				format = "昨天";
			} else {
				return "Yesterday ";
			}
		} else {
			if (isZh) {
				format = "MM-dd";
			} else {
				format = "MMM dd";
			}
		}
		if (isZh) {
			return new SimpleDateFormat(format, Locale.CHINESE).format(messageDate);
		} else {
			return new SimpleDateFormat(format, Locale.ENGLISH).format(messageDate);
		}
	}

	public static String formatTimeAgo(long timestamp) {
		long currentTime = System.currentTimeMillis();
		long timeDelta = currentTime - timestamp;

		if (timeDelta < 1000) {
			return "刚刚";
		}
		long minutes = timeDelta / (60 * 1000);
		long hours = timeDelta / (60 * 60 * 1000);
		long days = timeDelta / (24 * 60 * 60 * 1000);
		if (days > 0) {
			return String.format(Locale.getDefault(), "%d天前", days);
		} else if (hours > 0) {
			return String.format(Locale.getDefault(), "%d小时前", hours);
		} else if (minutes > 0) {
			return String.format(Locale.getDefault(), "%d分钟前", minutes);
		} else {
			return "刚刚";
		}
	}

	public static boolean isCloseEnough(long time1, long time2) {
		// long time1 = date1.getTime();
		// long time2 = date2.getTime();
		long delta = time1 - time2;
		if (delta < 0) {
			delta = -delta;
		}
		return delta < INTERVAL_IN_MILLISECONDS;
	}

	private static boolean isSameDay(long inputTime) {
		
		TimeInfo tStartAndEndTime = getTodayStartAndEndTime();
		if(inputTime>tStartAndEndTime.getStartTime()&&inputTime<tStartAndEndTime.getEndTime())
			return true;
		return false;
	}

	private static boolean isYesterday(long inputTime) {
		TimeInfo yStartAndEndTime = getYesterdayStartAndEndTime();
		if(inputTime>yStartAndEndTime.getStartTime()&&inputTime<yStartAndEndTime.getEndTime())
			return true;
		return false;
	}

    @SuppressLint("SimpleDateFormat")
	public static Date StringToDate(String dateStr, String formatStr) {
		DateFormat format = new SimpleDateFormat(formatStr);
		Date date = null;
		try {
			date = format.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	/**
	 * 
	 * @param timeLength Millisecond
	 * @return
	 */
	@SuppressWarnings("UnusedAssignment")
	@SuppressLint("DefaultLocale")
	public static String toTime(int timeLength) {
		timeLength /= 1000;
		int minute = timeLength / 60;
		int hour = 0;
		if (minute >= 60) {
			hour = minute / 60;
			minute = minute % 60;
		}
		int second = timeLength % 60;
		// return String.format("%02d:%02d:%02d", hour, minute, second);
		return String.format("%02d:%02d", minute, second);
	}
	/**
	 * 
	 * @param timeLength second
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public static String toTimeBySecond(int timeLength) {
//		timeLength /= 1000;
		int minute = timeLength / 60;
		int hour = 0;
		if (minute >= 60) {
			hour = minute / 60;
			minute = minute % 60;
		}
		int second = timeLength % 60;
		// return String.format("%02d:%02d:%02d", hour, minute, second);
		return String.format("%02d:%02d", minute, second);
	}
	 
	

	public static TimeInfo getYesterdayStartAndEndTime() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -1);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);

		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.add(Calendar.DATE, -1);
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}

	public static TimeInfo getTodayStartAndEndTime() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}

	public static TimeInfo getBeforeYesterdayStartAndEndTime() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -2);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.add(Calendar.DATE, -2);
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}
	
	/**
	 * endtime为今天
	 * @return
	 */
	public static TimeInfo getCurrentMonthStartAndEndTime(){
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(Calendar.DATE, 1);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
//		calendar2.set(Calendar.HOUR_OF_DAY, 23);
//		calendar2.set(Calendar.MINUTE, 59);
//		calendar2.set(Calendar.SECOND, 59);
//		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}
	
	public static TimeInfo getLastMonthStartAndEndTime(){
		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.MONTH, -1);
		calendar1.set(Calendar.DATE, 1);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.add(Calendar.MONTH, -1);
		calendar2.set(Calendar.DATE, 1);
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		calendar2.roll(Calendar.DATE,  - 1 );
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}
	
	public static String getTimestampStr() {
        return Long.toString(System.currentTimeMillis());        
    }

	/**
	 * 判断是否是24小时制
	 * @param context
	 * @return
	 */
	public static boolean is24HourFormat(Context context) {
		return android.text.format.DateFormat.is24HourFormat(context);
	}
}
