package cn.huanyuan.happymeet.ui.userinfo.edit

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
enum class UserParamType(var type: Int) {

    /**
     * 基本信息
     */
    TYPE_NICKNAME(1),   //昵称
    TYPE_AGE(2),  //年龄
    TYPE_HEIGHT(3),//身高
    TYPE_WEIGHT(4),//体重
    TYPE_EDUCATION(5),//学历
    TYPE_MARRIAGE(6),//婚姻状况
    TYPE_ADDRESS(7),//现居地

    /**
     * 详细信息
     */
    TYPE_WORK(8),//工作
    TYPE_MONTY_INCOME(9),//月收入
    TYPE_HOMETOWN(10),//家乡
    TYPE_LIVE_ROOM(11),//住房情况
    TYPE_CAR(12),//买车情况
    TYPE_PERSONALITY(13),//个性特征
    TYPE_INTEREST(14),//兴趣爱好
    TYPE_LOVE_TARGET(15),//恋爱目标

    /**
     * 交友信息
     */
    TYPE_FRIEND_ADDRESS(16),   //所在地
    TYPE_FRIEND_AGE(17),  //年龄
    TYPE_FRIEND_HEIGHT(18),//身高
    TYPE_FRIEND_MIN_EDUCATION(19),//最低学历
    TYPE_FRIEND_MIN_INCOME(20),//最低收入
}