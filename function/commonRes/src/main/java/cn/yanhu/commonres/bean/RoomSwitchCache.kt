package cn.yanhu.commonres.bean

import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

/**
 * @author: zhengjun
 * created: 2025/9/3
 * desc:
 */
class RoomSwitchCache : LitePalSupport() {
    var id: Long = 0
    var roomId: String = ""
    @Column(defaultValue = "true")
    var giftVoiceOpen: Boolean = true
    @Column(defaultValue = "true")
    var enterAnimOpen: Boolean = true
}