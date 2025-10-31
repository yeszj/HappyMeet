package cn.yanhu.imchat.custom.message.chatalertview

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.yanhu.baselib.utils.GlideUtils.loadImage
import cn.yanhu.baselib.utils.HtmlClickProcessor
import cn.yanhu.commonres.config.ImMessageParamsConfig
import cn.yanhu.commonres.manager.AppCacheManager.isMan
import cn.yanhu.commonres.manager.ImageThumbUtils.getThumbUrl
import cn.yanhu.commonres.router.PageIntentUtil.url2Page
import cn.yanhu.commonres.utils.MediaPlayUtils
import cn.yanhu.commonres.utils.MediaPlayUtils.isPause
import cn.yanhu.commonres.utils.MediaPlayUtils.isPlaying
import cn.yanhu.commonres.utils.MediaPlayUtils.pauseMedia
import cn.yanhu.commonres.utils.MediaPlayUtils.release
import cn.yanhu.commonres.utils.MediaPlayUtils.resumeMedia
import cn.yanhu.imchat.R
import cn.yanhu.imchat.bean.CmdMsgInfo
import cn.yanhu.imchat.custom.message.BaseEaseChatRow
import cn.yanhu.imchat.databinding.EaseAlertLayoutBinding
import com.bumptech.glide.Glide
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.easeui.EaseIM
import com.jeremyliao.liveeventbus.LiveEventBus

@SuppressLint("ViewConstructor")
class ChatAlertNew(context: Context?, isSender: Boolean) : BaseEaseChatRow(context, isSender) {
    private var binding: EaseAlertLayoutBinding? = null
    private var icon: ImageView? = null
    private var ivRightIcon: ImageView? = null
    private var hintView: TextView? = null

    private var alert_ll: LinearLayout? = null
    private var call_ll: LinearLayout? = null

    private var tvCallBtn: TextView? = null

    private var tvOperate: TextView? = null


    override fun onInflateView() {
        val inflate = inflater.inflate(R.layout.ease_alert_layout, this)
        inflate.tag = "layout/ease_alert_layout_0"
        binding = EaseAlertLayoutBinding.bind(getRootView())
    }

    override fun onFindViewById() {
        alert_ll = findViewById<LinearLayout?>(R.id.alert_ll)
        icon = findViewById<ImageView>(R.id.alert_icon)
        hintView = findViewById<View?>(R.id.tv_alert) as TextView
        call_ll = findViewById<View?>(R.id.call_ll) as LinearLayout
        ivRightIcon = findViewById<ImageView>(R.id.rightIcon)
        tvOperate = findViewById<TextView>(R.id.tv_operate)
        tvCallBtn = findViewById<TextView?>(R.id.tv_callBtn)
    }

    protected override fun onSetUpView() {
        super.onSetUpView()
        try {
            val messageBody = message.getBody() as EMCustomMessageBody
            val params = messageBody.getParams()
            val iconUrl = params.get("icon")
            if (TextUtils.isEmpty(iconUrl)) {
                icon!!.setVisibility(GONE)
            } else {
                icon!!.setVisibility(VISIBLE)
                Glide.with(context).load(iconUrl).into(icon!!)
            }

            if (params.containsKey("rightIcon")) {
                val rightIcon = params.get("rightIcon")
                if (!TextUtils.isEmpty(rightIcon)) {
                    ivRightIcon!!.setVisibility(VISIBLE)
                    loadImage(context, rightIcon, ivRightIcon)
                } else {
                    ivRightIcon!!.setVisibility(GONE)
                }
            } else {
                ivRightIcon!!.setVisibility(GONE)
            }


            val content = params["content"]
            if (TextUtils.isEmpty(content)) {
                binding!!.alertView.visibility = GONE
            } else {
                binding!!.alertView.visibility = VISIBLE
                if (!TextUtils.isEmpty(content) && content!!.contains("</font>")) {
                    val pageUrl = params.getOrDefault(ImMessageParamsConfig.KEY_PAGE_URL, "")
                    if (!TextUtils.isEmpty(pageUrl)) {
                        if (content.contains("平台防诈骗公告")) {
                            HtmlClickProcessor.setHtmlTextWithClick(
                                hintView!!,
                                content,
                                "《平台防诈骗公告》"
                            ) {
                                url2Page(context, pageUrl)
                            }
                        } else {
                            hintView!!.text = Html.fromHtml(content)
                            hintView!!.setOnClickListener(OnClickListener { v: View? ->
                                url2Page(
                                    context,
                                    pageUrl
                                )
                            })
                        }
                    } else {
                        hintView!!.text = Html.fromHtml(content)
                    }
                } else {
                    hintView!!.text = content
                }
            }

            val msgType = params.get("msgType")
            if (!TextUtils.isEmpty(msgType)) {
                if (CmdMsgInfo.MSG_TYPE_CALL == msgType) {
                    call_ll!!.visibility = VISIBLE
                    call_ll!!.setOnClickListener(OnClickListener { v: View? ->
                        LiveEventBus.get<Any?>("call_phone").post(1)
                    })
                } else {
                    call_ll!!.visibility = GONE
                }
            } else {
                if (!TextUtils.isEmpty(content) && content!!.contains("音视频")) {
                    call_ll!!.visibility = VISIBLE
                    call_ll!!.setOnClickListener(OnClickListener { v: View? ->
                        LiveEventBus.get<Any?>("call_phone").post(1)
                    })
                } else {
                    call_ll!!.visibility = GONE
                }
            }

            if (params.containsKey(ImMessageParamsConfig.KEY_BTN_VALUE)) {
                val btnValue = params[ImMessageParamsConfig.KEY_BTN_VALUE]
                tvOperate!!.text = btnValue
                tvOperate!!.visibility = VISIBLE
                tvOperate!!.setOnClickListener(OnClickListener { v: View? ->
                    val pageUrl = params[ImMessageParamsConfig.KEY_PAGE_URL]
                    url2Page(context, pageUrl)
                })
            } else {
                tvOperate!!.visibility = GONE
            }

            val introduction = params["introduction"]
            if (!TextUtils.isEmpty(introduction) && isMan()) {
                binding!!.setIntroduction(introduction)
                if (introduction!!.contains(".mp4")) {
                    loadImage(context, getThumbUrl(introduction), binding!!.introducePortrait)
                } else {
                    loadImage(
                        context,
                        getThumbUrl(
                            EaseIM.getInstance().userProvider.getUser(message.getUserName())
                                .avatar
                        ),
                        binding!!.introducePortrait
                    )
                }

                binding!!.introduceLl.setOnClickListener(OnClickListener { v: View? ->
                    if (introduction.contains(".mp4")) {
                        LiveEventBus.get<Any?>("slide_introduce").post(0)
                    } else {
                        if (isPlaying()) {
                            binding!!.voiceTxt.text = "播放语音"
                            binding!!.ivStatus.setImageResource(R.mipmap.ic_play_stop)
                            binding!!.voiceLottie.pauseAnimation()
                            binding!!.voiceLottie.frame = 1
                            pauseMedia()
                        } else {
                            binding!!.voiceTxt.text = "暂停播放"
                            binding!!.ivStatus.setImageResource(cn.yanhu.commonres.R.mipmap.ic_play_start)
                            binding!!.voiceLottie.playAnimation()
                            if (isPause) {
                                resumeMedia()
                            } else {
                                MediaPlayUtils.playSound(
                                    introduction,
                                    MediaPlayer.OnCompletionListener { mp: MediaPlayer? ->
                                        binding!!.voiceTxt.text = "播放语音"
                                        binding!!.ivStatus.setImageResource(R.mipmap.ic_play_stop)
                                    },
                                    null
                                )
                            }
                        }
                    }
                })
            } else {
                binding!!.introduceLl.visibility = GONE
                binding!!.setIntroduction("")
            }
            binding!!.executePendingBindings()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }
}