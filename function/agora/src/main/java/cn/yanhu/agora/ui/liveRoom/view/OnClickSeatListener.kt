package cn.yanhu.agora.ui.liveRoom.view

import android.view.View
import cn.yanhu.commonres.bean.RoomSeatInfo

/**
 * @author: zhengjun
 * created: 2025/1/15
 * desc:
 */
interface OnClickSeatListener {
    fun onChildClickListener(view: View, position: Int, item: RoomSeatInfo?)
}