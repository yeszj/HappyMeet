package cn.yanhu.agora.manager.dbCache

import cn.yanhu.agora.bean.InviteSeatRecord
import org.litepal.LitePal

/**
 * @author: zhengjun
 * created: 2023/7/31
 * desc:
 */
object InviteRecordCacheManager {
    fun getInviteRecordByUserId(userId: String): InviteSeatRecord? {
        val find = LitePal.where("userId = ?", userId).limit(1).find(
            InviteSeatRecord::class.java
        )
        if (find.size <= 0) {
            return null
        }
        return find[0]
    }

    fun saveInviteRecord(inviteSeatRecord: InviteSeatRecord) {
        val currentCacheInfo = getInviteRecordByUserId(inviteSeatRecord.userId)
        if (currentCacheInfo == null) {
            inviteSeatRecord.save()
        } else {
            inviteSeatRecord.update(currentCacheInfo.id)
        }
    }
}