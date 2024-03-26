package cn.yanhu.imchat.manager

import com.netease.yunxin.kit.corekit.im.model.UserField
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback
import com.netease.yunxin.kit.corekit.im.repo.CommonRepo

/**
 * @author: zhengjun
 * created: 2024/2/2
 * desc:
 */
object ImUserManager {
    private fun updateUserInfo(field: UserField, value: Any) {
        val map: MutableMap<UserField, Any> = HashMap(1)
        map[field] = value
        CommonRepo.updateUserInfo(
            map,
            object : FetchCallback<Void> {
                override fun onSuccess(param: Void?) {
                }

                override fun onFailed(code: Int) {
                }

                override fun onException(exception: Throwable?) {
                }
            })
    }

    private fun updateUserInfo(map: MutableMap<UserField, Any>) {
        CommonRepo.updateUserInfo(
            map,
            object : FetchCallback<Void> {
                override fun onSuccess(param: Void?) {
                }

                override fun onFailed(code: Int) {
                }

                override fun onException(exception: Throwable?) {
                }
            })
    }

}