package cn.yanhu.baselib.utils

import android.graphics.Typeface
import android.widget.TextView
import cn.yanhu.baselib.R
import com.blankj.utilcode.util.ActivityUtils

/**
 * @author: witness
 * created: 2022/6/7
 * desc:
 */
object TextFontStyleUtils {
    fun setTextFontStyle(textView: TextView, fontType: String?,fontStyle:Int = Typeface.NORMAL) {
        val customTypeFace = getCustomTypeFace(fontType)
        if (!textView.isInEditMode && customTypeFace != null) {

            textView.setTypeface(customTypeFace,fontStyle)
        }
    }

    @JvmStatic
    fun setTextFontStyle(textView: TextView, fontType: String?) {
        val customTypeFace = getCustomTypeFace(fontType)
        if (!textView.isInEditMode && customTypeFace != null) {
            textView.typeface = customTypeFace
        }
    }

    fun getCustomTypeFace(fontType: String?): Typeface? {
        try {
            val tf: Typeface
            val topActivity = ActivityUtils.getTopActivity()
            if (topActivity != null) {
                //val mgr: AssetManager = topActivity.assets
                when (fontType) {
                    CommonUtils.getString(R.string.fontBold) -> {
                        tf = Typeface.create("sans-serif-medium", Typeface.BOLD)
                        //tf = Typeface.createFromAsset(mgr, "fonts/Poppins-Bold.ttf")
                    }
                    CommonUtils.getString(R.string.fontMedium) -> {
                        val familyName = "sans-serif-medium"
                        tf = Typeface.create(familyName, Typeface.NORMAL)
                       // tf = Typeface.createFromAsset(mgr, "fonts/Poppins-Medium.ttf")
                    }
                    CommonUtils.getString(R.string.fontSemiBold) -> {
                        val familyName = "sans-serif-black"
                        tf = Typeface.create(familyName, Typeface.BOLD)
                       // tf = Typeface.createFromAsset(mgr, "fonts/Poppins-SemiBold.ttf")
                    }
                    CommonUtils.getString(R.string.fontLight) -> {
                        val familyName = "sans-serif-light"
                        tf = Typeface.create(familyName, Typeface.NORMAL)
                       // tf = Typeface.createFromAsset(mgr, "fonts/Poppins-Light.ttf")
                    }
                    else -> {
                        tf = Typeface.create("sans-serif", Typeface.NORMAL)
                        //tf = Typeface.createFromAsset(mgr, "fonts/Poppins-Regular.ttf")
                    }
                }
                return tf
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return null
    }
}