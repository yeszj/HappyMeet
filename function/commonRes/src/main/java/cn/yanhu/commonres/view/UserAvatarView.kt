package cn.yanhu.commonres.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.view.CircleBorderTransformation
import cn.yanhu.commonres.R
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.utils.SVGAUtils
import coil.load
import coil.transform.CircleCropTransformation
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser

/**
 * @author: witness
 * created: 2022/12/5
 * desc:
 */
class UserAvatarView : LinearLayout {
    private var avatarMarginLeft: Float = 0F
    private lateinit var ivAvatar: AppCompatImageView
    private lateinit var svgAvatar: SVGAImageView
    private var isShowSvga: Boolean = false
    private var isCanClick: Boolean = true
    private var avatarBorderColor: Int = -1
    var avatarBorderSize: Float = 0f

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView(context, attrs)
    }

    @SuppressLint("Recycle")
    private fun initView(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val attrArray = context.resources.obtainAttributes(attrs, R.styleable.UserAvatarView)
        isShowSvga = attrArray.getBoolean(R.styleable.UserAvatarView_isShowSvgAvatar, false)
        val avatarSize = attrArray.getDimension(R.styleable.UserAvatarView_avatarSize, 40f)
        val svgaAvatarSize = attrArray.getDimension(R.styleable.UserAvatarView_svgAvatarSize, 64f)
        avatarBorderSize =
            attrArray.getDimension(R.styleable.UserAvatarView_avatarBorderSize, 0f)
        avatarMarginLeft = attrArray.getDimension(R.styleable.UserAvatarView_avatarMarginLeft, 0f)
        avatarBorderColor = attrArray.getColor(R.styleable.UserAvatarView_avatarBorderColor, -1)
        isCanClick = attrArray.getBoolean(R.styleable.UserAvatarView_isCanClick, true)
        LayoutInflater.from(context).inflate(R.layout.view_user_avatar, this, true)
        ivAvatar = findViewById(R.id.iv_user_portrait)
        svgAvatar = findViewById(R.id.svg_portraitAnim)
        val layoutParams = ivAvatar.layoutParams
        layoutParams.width = avatarSize.toInt()
        layoutParams.height = avatarSize.toInt()
        ivAvatar.layoutParams = layoutParams

        if (isShowSvga) {
            val layoutParams1 = svgAvatar.layoutParams
            layoutParams1.width = svgaAvatarSize.toInt()
            layoutParams1.height = svgaAvatarSize.toInt()
            svgAvatar.layoutParams = layoutParams1
            svgAvatar.visibility = View.VISIBLE
        } else {
            svgAvatar.visibility = View.INVISIBLE
        }

        val vgParent = findViewById<ViewGroup>(R.id.vg_avatarParent)
        val layoutParams2 = vgParent.layoutParams
        layoutParams2.width = svgaAvatarSize.toInt()
        layoutParams2.height = svgaAvatarSize.toInt()
        vgParent.layoutParams = layoutParams2

        attrArray.recycle()
    }

    fun getAvatarView(): AppCompatImageView {
        return ivAvatar
    }

    fun setUserAvatar(item: BaseUserInfo, parseCompletion: SVGAParser.ParseCompletion) {
        setAvatar(item, parseCompletion)
    }

    fun setUserAvatar(item: BaseUserInfo) {
        setAvatar(item, null)
    }

    fun setAvatarBorder(avatarBorderSize: Int) {
        this.avatarBorderSize = avatarBorderSize.toFloat()
        ivAvatar.load(ivAvatar.getTag(R.id.tag_url)) {
            if (avatarBorderColor != -1) {
                transformations(CircleBorderTransformation(this@UserAvatarView.avatarBorderSize, avatarBorderColor))
            } else {
                transformations(CircleCropTransformation())
            }
        }
    }

    fun setAvatarSize(avatarSize: Int) {
        val layoutParams = ivAvatar.layoutParams
        layoutParams.width = avatarSize
        layoutParams.height = avatarSize
        ivAvatar.layoutParams = layoutParams
    }

    @SuppressLint("CheckResult")
    private fun setAvatar(item: BaseUserInfo, parseCompletion: SVGAParser.ParseCompletion?) {
        ivAvatar.setImageResource(R.drawable.ease_default_avatar)
        val portrait = item.portrait
        if (TextUtils.isEmpty(portrait)) {
            return
        }
        ivAvatar.setTag(R.id.tag_url,portrait)
        ivAvatar.load(portrait) {
            if (avatarBorderColor != -1) {
                transformations(CircleBorderTransformation(avatarBorderSize, avatarBorderColor))
            } else {
                transformations(CircleCropTransformation())
            }
        }

        if (isShowSvga) {
            if (!TextUtils.isEmpty(item.avatarFrame)) {
                ViewUtils.setMarginLeft(this, 0)
                svgAvatar.visibility = View.VISIBLE
                svgAvatar.visibility = View.VISIBLE
                if (!item.avatarFrame.endsWith(".svga")) {
                    GlideUtils.load(
                        context,
                        item.avatarFrame,
                        svgAvatar,
                        placeholderId = -1
                    )
                } else {
                    val svgAvatarTag = svgAvatar.tag
                    if (svgAvatarTag == null || !item.avatarFrame.equals(svgAvatarTag)) {
                        svgAvatar.tag = item.avatarFrame
                        if (parseCompletion != null) {
                            SVGAUtils.loadSVGAAnim(svgAvatar, item.avatarFrame, parseCompletion)
                        } else {
                            SVGAUtils.loadSVGAAnim(svgAvatar, item.avatarFrame)
                        }
                        svgAvatar.addOnAttachStateChangeListener(object :
                            OnAttachStateChangeListener {
                            override fun onViewAttachedToWindow(v: View) {
                                svgAvatar.startAnimation()
                            }

                            override fun onViewDetachedFromWindow(v: View) {
                                svgAvatar.pauseAnimation()
                            }
                        })
                    } else {
                        if (!svgAvatar.isAnimating) {
                            svgAvatar.startAnimation()
                        }
                    }

                }
            } else {
                ViewUtils.setMarginLeft(this, avatarMarginLeft.toInt())
                clearAnim()
            }
        }
        if (isCanClick && !TextUtils.isEmpty(item.userId)) {
            setOnSingleClickListener {
                RouteIntent.lunchPersonHomePage(item.userId)
            }
        }
    }


    private fun clearAnim() {
        svgAvatar.setImageBitmap(null)
        svgAvatar.tag = ""
        svgAvatar.stopAnimation(true)
    }
}