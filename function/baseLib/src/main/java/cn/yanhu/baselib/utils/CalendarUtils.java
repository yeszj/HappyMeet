package cn.yanhu.baselib.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.TimeZone;



/**
 * 作者: Xiao Danchen.
 * 工具类：
 * 通过日历添加事件提醒的方式实现秒杀、抢购等提醒功能。
 * 要求内部实现：
 * 1，新增提醒是否是重复提醒，是则添加到相关事件下；否则添加到新事件
 * 2，过期事件、提醒的清理能力
 *
 * 日历相关的资料：https://developer.android.com/guide/topics/providers/calendar-provider.html?hl=zh-cn#calendar
 */
public class CalendarUtils {
    private static String calanderURL;
    private static String calanderEventURL;
    private static String calanderRemiderURL;

    private static String CALENDARS_NAME = "暖遇";
    private static String CALENDARS_ACCOUNT_NAME = "happyMeet";
    private static String CALENDARS_ACCOUNT_TYPE = "cn.huanyuan.sweetlove";
    private static String CALENDARS_DISPLAY_NAME = "福气红包";

    /**
     * 初始化uri
     */
    static {
        if (Build.VERSION.SDK_INT >= 8) {
            calanderURL = "content://com.android.calendar/calendars";
            calanderEventURL = "content://com.android.calendar/events";
            calanderRemiderURL = "content://com.android.calendar/reminders";
        } else {
            calanderURL = "content://calendar/calendars";
            calanderEventURL = "content://calendar/events";
            calanderRemiderURL = "content://calendar/reminders";
        }
    }

    /**
     * 获取日历ID
     * @param context
     * @return 日历ID
     */
    private static int checkAndAddCalendarAccounts(Context context){
        int oldId = checkCalendarAccounts(context);
        if( oldId >= 0 ){
            return oldId;
        }else{
            long addId = addCalendarAccount(context);
            if (addId >= 0) {
                return checkCalendarAccounts(context);
            } else {
                return -1;
            }
        }
    }

    /**
     * 检查是否存在日历账户
     * @param context
     * @return
     */
    @SuppressLint("Range")
    private static int checkCalendarAccounts(Context context) {

        Cursor userCursor = context.getContentResolver().query(Uri.parse(calanderURL), null, null, null, CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " ASC ");
        try {
            if (userCursor == null)//查询返回空值
                return -1;
            int count = userCursor.getCount();
            if (count > 0) {//存在现有账户，取第一个账户的id返回
                userCursor.moveToLast();
                return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
            } else {
                return -1;
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    /**
     * 添加一个日历账户
     * @param context
     * @return
     */
    private static long addCalendarAccount(Context context) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);

        value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = Uri.parse(calanderURL);
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
                .build();

        Uri result = context.getContentResolver().insert(calendarUri, value);
        long id = result == null ? -1 : ContentUris.parseId(result);
        return id;
    }

    /**
     * 向日历中添加一个事件
     * @param context
     * @param calendar_id （必须参数）
     * @param title
     * @param description
     * @param begintime 事件开始时间，以从公元纪年开始计算的协调世界时毫秒数表示。 （必须参数）
     * @param endtime 事件结束时间，以从公元纪年开始计算的协调世界时毫秒数表示。（非重复事件：必须参数）
     * @param  rule "FREQ=DAILY;COUNT=8" //每天提醒 持续8天
     * @return
     */
    private static Uri insertCalendarEvent(Context context, long calendar_id, String title, String description , long begintime, long endtime,String rule){
        ContentValues event = new ContentValues();
        event.put("title", title);
        event.put("description", description);
        // 插入账户的id
        event.put("calendar_id", calendar_id);
        event.put(CalendarContract.Events.RRULE, rule);//必须有

        event.put(CalendarContract.Events.DTSTART, begintime);//必须有
        if (endtime!=0){
            event.put(CalendarContract.Events.DTEND, endtime);//非重复事件：必须有
        }
        event.put(CalendarContract.Events.HAS_ALARM, 1);//设置有闹钟提醒
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());//这个是时区，必须有，
        //添加事件
        Uri newEvent = context.getContentResolver().insert(Uri.parse(calanderEventURL), event);
        return newEvent;
    }

    /**
     * 查询日历事件
     * @param context
     * @param title 事件标题
     * @return 事件id,查询不到则返回""
     */
    @SuppressLint("Range")
    private static String queryCalendarEvent(Context context, long calendar_id, String title, String description, long start_time, long end_time){
        // 根据日期范围构造查询
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, start_time);
        ContentUris.appendId(builder, end_time);
        Cursor cursor = context.getContentResolver().query(builder.build(), null, null, null, null);
        String tmp_title;
        String tmp_desc;
        long temp_calendar_id;
        if(cursor.moveToFirst()){
            do{
                tmp_title = cursor.getString(cursor.getColumnIndex("title"));
                tmp_desc = cursor.getString(cursor.getColumnIndex("description"));
                temp_calendar_id = cursor.getLong(cursor.getColumnIndex("calendar_id"));
                long dtstart = cursor.getLong(cursor.getColumnIndex("dtstart"));
                if(TextUtils.equals(title,tmp_title) && TextUtils.equals(description,tmp_desc) && calendar_id == temp_calendar_id && dtstart==start_time){
                    String eventId = cursor.getString(cursor.getColumnIndex("event_id"));
                    return eventId;
                }
            }while(cursor.moveToNext());
        }
        return  "";
    }

    /**
     * 添加日历提醒：标题、描述、开始时间共同标定一个单独的提醒事件
     * @param context
     * @param title 日历提醒的标题,不允许为空
     * @param description 日历的描述（备注）信息
     * @param begintime 事件开始时间，以从公元纪年开始计算的协调世界时毫秒数表示。
     * @param endtime 事件结束时间，以从公元纪年开始计算的协调世界时毫秒数表示。
     * @param remind_minutes 提前remind_minutes分钟发出提醒
     * @param callback 添加提醒是否成功结果监听
     */
    public static void addCalendarEventRemind(Context context, @NonNull String title, String description, long begintime, long endtime, int remind_minutes,String rule, onCalendarRemindListener callback){
        long calendar_id = checkAndAddCalendarAccounts(context);
        if(calendar_id < 0){
            // 获取日历失败直接返回
            if(null != callback){
                callback.onFailed(onCalendarRemindListener.Status.CALENDAR_ERROR);
            }
            return;
        }
        //根据标题、描述、开始时间查看提醒事件是否已经存在
        String event_id = queryCalendarEvent(context,calendar_id,title,description,begintime,endtime);
        //如果提醒事件不存在，则新建事件
        if(TextUtils.isEmpty(event_id)){
            Uri newEvent = insertCalendarEvent(context,calendar_id,title,description,begintime,endtime,rule);
            if (newEvent == null) {
                // 添加日历事件失败直接返回
                if(null != callback){
                    callback.onFailed(onCalendarRemindListener.Status.EVENT_ERROR);
                }
                return;
            }
            event_id = ContentUris.parseId(newEvent)+"";
        }
        //为事件设定提醒
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, event_id);
        // 提前remind_minutes分钟有提醒
        values.put(CalendarContract.Reminders.MINUTES, remind_minutes);
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        Uri uri = context.getContentResolver().insert(Uri.parse(calanderRemiderURL), values);
        if(uri == null) {
            // 添加提醒失败直接返回
            if(null != callback){
                callback.onFailed(onCalendarRemindListener.Status.REMIND_ERROR);
            }
            return;
        }

        //添加提醒成功
        if(null != callback){
            callback.onSuccess();
        }
    }

    /**
     * 删除日历提醒事件：根据标题、描述和开始时间来定位日历事件
     * @param context
     * @param title 提醒的标题
     * @param description 提醒的描述：deeplink URI
     * @param startTime 事件的开始时间
     * @param callback 删除成功与否的监听回调
     */
    @SuppressLint("Range")
    public static void deleteCalendarEventRemind(Context context, String title, String description, long startTime,onCalendarRemindListener callback){
        Cursor eventCursor = context.getContentResolver().query(Uri.parse(calanderEventURL), null, null, null, null);
        try {
            if (eventCursor == null)//查询返回空值
                return;
            if (eventCursor.getCount() > 0) {
                //遍历所有事件，找到title、description、startTime跟需要查询的title、descriptio、dtstart一样的项
                for (eventCursor.moveToFirst(); !eventCursor.isAfterLast(); eventCursor.moveToNext()) {
                    String eventTitle = eventCursor.getString(eventCursor.getColumnIndex("title"));
                    @SuppressLint("Range") String eventDescription = eventCursor.getString(eventCursor.getColumnIndex("description"));
                    long dtstart = eventCursor.getLong(eventCursor.getColumnIndex("dtstart"));
                    if (!TextUtils.isEmpty(title) && title.equals(eventTitle) && !TextUtils.isEmpty(description) && description.equals(eventDescription) && dtstart==startTime ) {
                        int id = eventCursor.getInt(eventCursor.getColumnIndex(CalendarContract.Calendars._ID));//取得id
                        Uri deleteUri = ContentUris.withAppendedId(Uri.parse(calanderEventURL), id);
                        int rows = context.getContentResolver().delete(deleteUri, null, null);
                        if (rows == -1) {
                            // 删除提醒失败直接返回
                            if(null != callback){
                                callback.onFailed(onCalendarRemindListener.Status.REMIND_ERROR);
                            }
                            return;
                        }
                        //删除提醒成功
                        if(null != callback){
                            callback.onSuccess();
                        }
                    }
                }
            }
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }
    }

    /**
     * 日历提醒添加成功与否监控器
     */
    public static interface onCalendarRemindListener{
        enum Status {
            CALENDAR_ERROR,
            EVENT_ERROR,
            REMIND_ERROR
        }
        void onFailed(Status error_code);
        void onSuccess();
    }

    /**
     * 辅助方法：获取设置时间起止时间的对应毫秒数
     * @param year
     * @param month 1-12
     * @param day 1-31
     * @param hour 0-23
     * @param minute 0-59
     * @return
     */
    public static long remindTimeCalculator(int year,int month,int day,int hour,int minute){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month-1,day,hour,minute);
        return calendar.getTimeInMillis();
    }
}
