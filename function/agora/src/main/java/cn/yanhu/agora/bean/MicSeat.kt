package cn.yanhu.agora.bean

/**
 * @author: zhengjun
 * created: 2025/11/17
 * desc:
 */
data class MicSeat(
    val userId: String,
    var isOnline: Boolean = true,
    var offlineStartTime: Long = 0L,
    var status: MicStatus = MicStatus.ONLINE
)

enum class MicStatus {
    ONLINE,          // 在线
    TEMP_OFFLINE,    // 暂时离线（5秒内）
    FORCE_OFFLINE    // 强制下麦（2分钟后）
}