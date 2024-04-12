package cn.yanhu.commonres.adapter

import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.widget.ImageView
import android.widget.TextView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.view.CustomFontTextView
import cn.yanhu.commonres.bean.ChatRoomGiftMsg
import cn.yanhu.commonres.view.floatScreenView.RewardLayout
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.bumptech.glide.Glide
import cn.yanhu.commonres.R
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.SexManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.view.floatScreenView.RewardAnimUtils
import cn.yanhu.commonres.view.floatScreenView.NumAnim

/**
 * @author: witness
 * created: 2022/12/15
 * desc:
 */
class GiftAnimAdapter(private var clearListener: OnClearListener) :
    RewardLayout.RewardAnimAdapter<ChatRoomGiftMsg> {
    override fun onInit(view: View, bean: ChatRoomGiftMsg): View {
        val giftInfo = bean.giftInfo
        val sendUser = bean.sendUser
        val giftNum = view.findViewById<CustomFontTextView>(R.id.tv_gift_amount)
        val ivAvatar = view.findViewById<ImageView>(R.id.iv_avatar)
        val giftImage = view.findViewById<ImageView>(R.id.iv_gift_img)

        val userName = view.findViewById<TextView>(R.id.tv_user_name)
        val tvTargetName = view.findViewById<TextView>(R.id.tv_targetName)
        Glide.with(ActivityUtils.getTopActivity()).load(sendUser.portrait).into(ivAvatar)
        ivAvatar.setOnClickListener {
            if (!TextUtils.isEmpty(sendUser.userId)) {
                RouteIntent.lunchPersonHomePage(sendUser.userId)
            }
        }
        // 初始化数据
        val giftCount = java.lang.String.format(" x %s", giftInfo.sendNumber)
        giftNum.text = giftCount
        bean.theGiftCount = giftInfo.sendNumber
        Glide.with(ActivityUtils.getTopActivity()).load(giftInfo.giftIcon).into(giftImage)
        if (SexManager.isMan(sendUser.gender)) {
            userName.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.manColor))
        } else {
            userName.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.femaleColor))
        }
        userName.text = sendUser.nickName
        if (!TextUtils.isEmpty(bean.receiverUser.nickName)) {
            if (SexManager.isMan(bean.receiverUser.gender)) {
                tvTargetName.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.manColor))
            } else {
                tvTargetName.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.femaleColor))
            }
        }
        tvTargetName.text =
            if (TextUtils.isEmpty(bean.receiverUser.nickName)) "everyone" else bean.receiverUser.nickName
        return view
    }

    override fun onUpdate(view: View?, o: ChatRoomGiftMsg, t: ChatRoomGiftMsg): View {
        val giftImage = view!!.findViewById<ImageView>(R.id.iv_gift_img)
        val giftNum = view.findViewById<CustomFontTextView>(R.id.tv_gift_amount)
        val showNum: Int = o.theGiftCount + o.giftInfo.sendNumber
        // 刷新已存在的giftview界面数据
        val giftCount = String.format(" x %s", showNum)
        giftNum.text = giftCount
        Glide.with(ActivityUtils.getTopActivity()).load(o.giftInfo.giftIcon).into(giftImage)
        // 数字刷新动画
        NumAnim().start(giftNum)
        // 更新累计礼物数量
        o.theGiftCount = showNum
        return view
    }

    override fun onKickEnd(bean: ChatRoomGiftMsg) {
        Log.d("onKickEnd", GsonUtils.toJson(bean))
        sendGift(bean)
    }

    override fun onComboEnd(bean: ChatRoomGiftMsg) {
        //连击结束
        Log.d("onComboEnd", GsonUtils.toJson(bean))
        sendGift(bean)
    }


    private fun sendGift(bean: ChatRoomGiftMsg) {
        if (bean.sendUser.userId == AppCacheManager.userId) {
            //调用送礼接口
            clearListener.onSendGiftSuccess(bean)
        }
    }

    override fun addAnim(view: View?) {
        val textView = view!!.findViewById<TextView>(R.id.tv_gift_amount)
        val img = view.findViewById<ImageView>(R.id.iv_gift_img)
        // 整个giftview动画
        val giftInAnim: Animation = RewardAnimUtils.getInAnimation(ActivityUtils.getTopActivity())
        // 礼物图像动画
        val imgInAnim: Animation = RewardAnimUtils.getInAnimation(ActivityUtils.getTopActivity())
        // 首次连击动画
        val comboAnim = NumAnim()
        imgInAnim.startTime = 500
        imgInAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                textView.visibility = View.GONE
            }

            override fun onAnimationEnd(animation: Animation) {
                textView.visibility = View.VISIBLE
                comboAnim.start(textView)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        view.startAnimation(giftInAnim)
        img.startAnimation(imgInAnim)
    }

    override fun outAnim(): AnimationSet {
        return RewardAnimUtils.getOutAnimation(ActivityUtils.getTopActivity())
    }

    override fun checkUnique(o: ChatRoomGiftMsg, t: ChatRoomGiftMsg): Boolean {
        return o.giftInfo.id == t.giftInfo.id && (o.sendUser.userId
            .equals(t.sendUser.userId) && o.receiverUser.userId.equals(t.receiverUser.userId))
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
        clearListener.onClear()
    }

    interface OnClearListener {
        fun onClear()
        fun onSendGiftSuccess(chatRoomGiftMsg: ChatRoomGiftMsg) {}
    }
}