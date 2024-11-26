package cn.yanhu.agora.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import cn.yanhu.agora.databinding.UserEnterAnimationItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.commonres.bean.ChatRoomGiftMsg
import cn.yanhu.commonres.config.LevelTagConfig
import cn.yanhu.commonres.utils.SVGAUtils
import cn.yanhu.commonres.view.floatScreenView.RewardAnimUtils
import cn.yanhu.commonres.view.floatScreenView.RewardLayout
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity


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
        val viewGroup = view as ViewGroup
        val get = viewGroup[0]
        val mBinding = DataBindingUtil.bind<UserEnterAnimationItemBinding>(get)
        mBinding?.apply {
            val level = sendUser.level
            if (level >= 100) {
                tvMaxLevel.visibility = View.VISIBLE
            } else {
                tvMaxLevel.visibility = View.GONE
            }
            if (level >= 60) {
                tvTips.text = "大驾光临"
            } else {
                tvTips.text = "进入了房间"
            }
            avatarView.setUserAvatar(sendUser)
            userLevel.setUserLevel(level)
            val enterAnimUrl = sendUser.enterAnimUrl
            if (!TextUtils.isEmpty(enterAnimUrl)) {
                if (enterAnimUrl.endsWith(".svga")) {
                    vgContent.visibility = View.INVISIBLE
                    avatarView.visibility = View.INVISIBLE
                    svgaBg.visibility = View.VISIBLE
                    SVGAUtils.loadCustomSVGAAnim(enterAnimUrl, object : SVGAParser.ParseCompletion {
                        override fun onComplete(videoItem: SVGAVideoEntity) {
                            val dynamicItem = SVGADynamicEntity()
                            val drawable = SVGADrawable(videoItem, dynamicItem)
                            val textPaint = TextPaint()
                            textPaint.textSize = 26f
                            textPaint.color = CommonUtils.getColor(cn.yanhu.baselib.R.color.white)
                            val nickName = sendUser.nickName
                            val nameLayout = staticLayout(" $nickName", textPaint)
                            dynamicItem.setDynamicText(nameLayout, "01")
                            // dynamicItem.setDynamicText(sendUser.nickName,textPaint,"01")
                            val enterDesc = staticLayout(" 进入了房间", textPaint)
                            dynamicItem.setDynamicText(enterDesc, "02")
                            dynamicItem.setDynamicImage(sendUser.portrait, "03")
                            svgaBg.setImageDrawable(drawable)
                            svgaBg.startAnimation()
                        }

                        override fun onError() {
                        }
                    })

                } else {
                    vgContent.visibility = View.VISIBLE
                    avatarView.visibility = View.VISIBLE
                    svgaBg.visibility = View.GONE
                    GlideUtils.loadAsDrawable(
                        context,
                        enterAnimUrl,
                        object : CustomTarget<Drawable>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                ivBg.setImageDrawable(resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                }

            } else if (level > 0) {
                val userEnterBg = LevelTagConfig.getUserEnterBg(level)
                ivBg.setImageResource(userEnterBg)
            }
            // 初始化数据
            tvUserName.text = sendUser.nickName
            vgParent.setOnClickListener {
                clickUserListener.onClickUser(sendUser.userId)
            }
        }

        return view
    }

    private fun staticLayout(
        nickName: String,
        textPaint: TextPaint
    ): StaticLayout {
        val builder = StaticLayout.Builder.obtain(
            nickName,
            0,
            nickName.length,
            textPaint,
            50
        ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.LTR)
            .setLineSpacing(0f, 1f).build()
        return builder
    }

    override fun onUpdate(view: View, o: ChatRoomGiftMsg, t: ChatRoomGiftMsg): View {
        return view
    }

    override fun onKickEnd(bean: ChatRoomGiftMsg?) {
    }

    override fun onComboEnd(bean: ChatRoomGiftMsg?) {
    }

    override fun addAnim(view: View, mBean: ChatRoomGiftMsg) {
        val enterAnimUrl = mBean.sendUser.enterAnimUrl
        if (!enterAnimUrl.endsWith(".svga")) {
            val giftInAnim: Animation =
                RewardAnimUtils.getUserEnterInAnimation(context)
            view.startAnimation(giftInAnim)
        }
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