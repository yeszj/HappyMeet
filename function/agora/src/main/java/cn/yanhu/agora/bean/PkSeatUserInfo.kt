package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.BaseUserInfo

/**
 * @author: zhengjun
 * created: 2025/9/4
 * desc:
 */
data class PkSeatUserInfo(
    val seatIndex: Int, var selectStatus: Int//0（未选择）1（红方选中）2（蓝方选中）
) : BaseUserInfo()