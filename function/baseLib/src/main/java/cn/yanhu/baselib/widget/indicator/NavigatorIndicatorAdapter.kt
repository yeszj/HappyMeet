package cn.yanhu.baselib.widget.indicator

import android.content.Context
import android.view.animation.DecelerateInterpolator
import androidx.viewpager.widget.ViewPager
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.TextFontStyleUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView

/**
 * @author: witness
 * created: 2022/4/29
 * desc:
 */
class NavigatorIndicatorAdapter(private var viewpager: ViewPager, private var titles: Array<String>) : CommonNavigatorAdapter() {
    override fun getCount(): Int {
        return titles.size
    }

    override fun getTitleView(context: Context, index: Int): IPagerTitleView {
        val titleView = ColorTransitionPagerTitleView(context)
        titleView.normalColor = CommonUtils.getColor(R.color.fontGrayColor)
        titleView.selectedColor = CommonUtils.getColor(R.color.fontTextColor)
        titleView.textSize = 16f
        titleView.text = titles[index]
        TextFontStyleUtils.setTextFontStyle(titleView,context.getString(R.string.fontMedium))
        titleView.setOnSingleClickListener {
            index.also { viewpager.currentItem = it }
        }
        return titleView
    }

    override fun getIndicator(context: Context?): IPagerIndicator {
        val indicator = LinePagerIndicator(context)
        indicator.mode = LinePagerIndicator.MODE_EXACTLY
        indicator.setColors(CommonUtils.getColor(R.color.fontTextColor))
        indicator.lineHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_2).toFloat()
        indicator.lineWidth = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_30).toFloat()
        indicator.roundRadius = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_1).toFloat()
        indicator.endInterpolator = DecelerateInterpolator(1.6f)
        return indicator
    }
}