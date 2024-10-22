package cn.huanyuan.sweetlove.ui.userinfo.edit

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
enum class UserParamType(var type: Int) {

    /**
     * 基本信息
     */
    TYPE_NICKNAME(4),   //昵称
    TYPE_AGE(5),  //年龄
    TYPE_EDUCATION(6),//学历
    TYPE_MARRIAGE(7),//婚姻状况
    TYPE_ADDRESS(8),//现居地
    TYPE_WORK(9),//职业
    TYPE_MONTY_INCOME(10),//月收入
    TYPE_HOMETOWN(11),//家乡

    /**
     * 交友信息
     */
    TYPE_FRIEND_ADDRESS(21),   //所在地
    TYPE_FRIEND_AGE(22),  //年龄
    TYPE_FRIEND_HEIGHT(23),//身高
    TYPE_FRIEND_MIN_EDUCATION(24),//最低学历
    TYPE_FRIEND_MIN_INCOME(25),//最低收入
}