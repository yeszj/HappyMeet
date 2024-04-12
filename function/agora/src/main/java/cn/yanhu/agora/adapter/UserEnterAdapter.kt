package cn.yanhu.agora.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.widget.TextView
import cn.yanhu.agora.R
import cn.yanhu.commonres.bean.ChatRoomGiftMsg
import cn.yanhu.commonres.config.LevelTagConfig
import cn.yanhu.commonres.view.UserAvatarView
import cn.yanhu.commonres.view.UserLevelView
import cn.yanhu.commonres.view.floatScreenView.RewardAnimUtils
import cn.yanhu.commonres.view.floatScreenView.RewardLayout

/**
 * @author: witness
 * created: 2022/12/16
 * desc:
 */
class UserEnterAdapter(
    private val context: Context,
    private val clickUserListener: OnClickUserListener,
) : RewardLayout.RewardAnimAdapter<ChatRoomGiftMsg> {
    override fun onInit(view: View, bean: ChatRoomGiftMsg): View {
        val sendUser = bean.sendUser

        val avatarView = view.findViewById<UserAvatarView>(R.id.avatarView)
        val userName = view.findViewById<TextView>(R.id.tv_userName)
        val userLevel = view.findViewById<UserLevelView>(R.id.userLevel)
        val vgParent = view.findViewById<ViewGroup>(R.id.vg_parent)
        val tvTips = view.findViewById<TextView>(R.id.tv_tips)
        val tvMaxLevel = view.findViewById<TextView>(R.id.tv_maxLevel)
        val level = sendUser.level
        if (level>=100){
            tvMaxLevel.visibility = View.VISIBLE
        }else{
            tvMaxLevel.visibility = View.GONE
        }
        if (level>=60){
            tvTips.text = "大驾光临"
        }else{
            tvTips.text = "进入了房间"
        }
        avatarView.setUserAvatar(sendUser)
        userLevel.setUserLevel(level)
        val userEnterBg = LevelTagConfig.getUserEnterBg(level)
        vgParent.setBackgroundResource(userEnterBg)

        // Glide.with(ActivityUtils.getTopActivity()).load(sendUser.portrait).into(ivAvatar)
        // 初始化数据
        userName.text = sendUser.nickName
        vgParent.setOnClickListener {
            clickUserListener.onClickUser(sendUser.userId)
        }
        return view
    }

    override fun onUpdate(view: View, o: ChatRoomGiftMsg, t: ChatRoomGiftMsg): View {
        return view
    }

    override fun onKickEnd(bean: ChatRoomGiftMsg?) {
    }

    override fun onComboEnd(bean: ChatRoomGiftMsg?) {
    }

    override fun addAnim(view: View) {
        val giftInAnim: Animation =
            RewardAnimUtils.getUserEnterInAnimation(context)
        view.startAnimation(giftInAnim)
    }

    override fun outAnim(): AnimationSet {
        return RewardAnimUtils.getUserEnterOutAnimation(context)
    }

    override fun checkUnique(o: ChatRoomGiftMsg, t: ChatRoomGiftMsg): Boolean {
        return o.sendUser.userId
            .equals(t.sendUser.userId)
    }

    override fun generateBean(bean: ChatRoomGiftMsg): ChatRoomGiftMsg? {
        try {
            return bean.clone() as ChatRoomGiftMsg
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onClear() {
    }

    interface OnClickUserListener {
        fun onClickUser(userId: String)
    }
}