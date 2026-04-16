package cn.yanhu.commonres.bean

import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/9/19
 * desc:
 */
open  class PkRoomResultInfo(var roomPkValue: Int, val otherRoomPkValue: Int, var countDownTime:Int):Serializable