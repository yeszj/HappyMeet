package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import com.lxj.xpopup.impl.FullScreenPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.PopRoomAngleResultBinding
import cn.yanhu.commonres.bean.BaseUserInfo
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2024/11/21
 * desc:
 */
@SuppressLint("ViewConstructor")
class RoomAngleResultPop(
    context: Context,
    private val angleUser: BaseUserInfo?,
    private val guardUser: BaseUserInfo?
) : FullScreenPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_room_angle_result
    }

    private lateinit var mBinding: PopRoomAngleResultBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopRoomAngleResultBinding.bind(popupImplView)
        mBinding.angleUser = angleUser
        mBinding.guardUser = guardUser
        mBinding.executePendingBindings()
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context, angleUser: BaseUserInfo?, guardUser: BaseUserInfo?
        ): RoomAngleResultPop {
            val matchPop = RoomAngleResultPop(mContext, angleUser, guardUser)
            val builder =
                XPopup.Builder(mContext)
                    .dismissOnBackPressed(false)
                    .dismissOnTouchOutside(false)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }
}