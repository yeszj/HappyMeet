package com.contract.commonlib.indicator

import android.content.Context
import android.view.animation.DecelerateInterpolator
import androidx.viewpager.widget.ViewPager
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.indicator.BgPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator

/**
 * @author: witness
 * created: 2022/4/29
 * desc:
 */
class NavigatorBgIndicatorAdapter(
    private var viewpager: ViewPager,
    private var titles: Array<String>
) : CommonNavigatorAdapter() {
    override fun getCount(): Int {
        return titles.size
    }

    override fun getTitleView(context: Context?, index: Int): IPagerTitleView {
        val titleView = BgPagerTitleView(context!!)
        titleView.normalColor = CommonUtils.getColor(R.color.fontTextColor)
        titleView.selectedColor = CommonUtils.getColor(R.color.white)
        titleView.mSelectedBg = R.drawable.tab_bg_black
        titleView.mNormalBg = R.drawable.tab_bg_normal
        titleView.textSize = 12f
        titleView.text = titles[index]
        titleView.setOnSingleClickListener {
            index.also { viewpager.currentItem = it }
        }
        return titleView
    }

    override fun getIndicator(context: Context?): IPagerIndicator {
        val indicator = LinePagerIndicator(context)
        indicator.mode = LinePagerIndicator.MODE_EXACTLY
        indicator.setColors(CommonUtils.getColor(R.color.fontTextColor))
        indicator.lineHeight = 0f
        indicator.lineWidth = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_30).toFloat()
        indicator.roundRadius = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_1).toFloat()
        indicator.endInterpolator = DecelerateInterpolator(1.6f)
        return indicator
    }
}