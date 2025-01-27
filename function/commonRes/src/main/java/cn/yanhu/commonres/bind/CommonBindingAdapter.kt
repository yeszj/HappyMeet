package cn.yanhu.commonres.bind

import android.graphics.drawable.Drawable
import android.text.Spanned
import android.text.TextUtils
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.toHtml
import cn.yanhu.baselib.view.CustomFontTextView
import com.bumptech.glide.request.target.CustomTarget

/**
 *
 * 类描述：
 * 创建人：woochen
 * 创建时间：2020/12/25 7:29 PM
 * 修改备注：
 **/
@BindingAdapter("android:text")
fun setText(view: CustomFontTextView, text: CharSequence?) {
    val oldText = view.text
    if (text === oldText || text == null && oldText.isEmpty()) {
        return
    }
    if (text is Spanned) {
        if (text == oldText) {
            return; // No change in the spans, so don't set anything.
        }
    } else if (!haveContentsChanged(text, oldText)) {
        return  // No content changes, so don't set anything.
    }
    text?.let {
        if (it.contains("<") && it.contains(">")) {
            view.text = text.toString().toHtml()
        } else view.text = text
    }
}

private fun haveContentsChanged(str1: CharSequence?, str2: CharSequence?): Boolean {
    if (str1 == null != (str2 == null)) {
        return true
    } else if (str1 == null) {
        return false
    }
    val length = str1.length
    if (length != str2!!.length) {
        return true
    }
    for (i in 0 until length) {
        if (str1[i] != str2[i]) {
            return true
        }
    }
    return false
}


@BindingAdapter(
    value = ["imageUrl", "placeholderId"],
    requireAll = false
)
fun loadImage(
    view: ImageView,
    url: Any?,
    placeholderId: Int = cn.yanhu.baselib.R.drawable.icon_portrait,
) {
    if (url is String && url.isEmpty()) {
        return
    }
    val tag = view.tag
    val placeholderIds = if (placeholderId == 0) {
        cn.yanhu.baselib.R.drawable.icon_portrait
    } else {
        placeholderId
    }
    if (tag == null || (url != null && url != tag)) {
        view.tag = url
        GlideUtils.load(
            view.context,
            url,
            view,
            placeholderId = placeholderIds,
            errorId = placeholderIds
        )
    }
}

@BindingAdapter( value = ["maskUrl", "maskRadius","maskSampling"],
    requireAll = false)
fun loadMaskImage(
    view: AppCompatImageView,
    maskUrl: Any?,
    maskRadius:Int=25,
    maskSampling:Int=3
) {
    if (maskUrl is String && maskUrl.isEmpty()) {
        return
    }
    GlideUtils.loadBlurTransPic(view.context, maskUrl.toString(),maskRadius,maskSampling, view)
}


@BindingAdapter(value = ["drawableUrl", "placeholderId"], requireAll = false)
fun loadImage2(
    view: ImageView,
    url: Any?,
    placeholderId: Int = cn.yanhu.commonres.R.drawable.shape_bg_gray,
) {

    if (placeholderId == -1) {
        view.setImageDrawable(null)
    } else {
        view.setImageResource(placeholderId)
    }
    if (url == null || (url is String && TextUtils.isEmpty(url))) {
        view.setImageDrawable(null)
        return
    }

//    val tagUrl = view.getTag(cn.yanhu.commonres.R.id.tag_url)
//    if (tagUrl!=null && tagUrl==url && drawableTag != null && drawableTag is Drawable) {
//        view.setImageDrawable(drawableTag)
//        return
//    }

    view.setTag(cn.yanhu.commonres.R.id.tag_url,url)
    GlideUtils.loadAsDrawable(view.context, url, object : CustomTarget<Drawable>() {
        override fun onResourceReady(resource: Drawable, transition: com.bumptech.glide.request.transition.Transition<in Drawable>?) {
            if (url == view.getTag(cn.yanhu.commonres.R.id.tag_url)) {
                view.setTag(cn.yanhu.commonres.R.id.tag_drawable,resource)
                view.setImageDrawable(resource)
            }
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }

    })
}

