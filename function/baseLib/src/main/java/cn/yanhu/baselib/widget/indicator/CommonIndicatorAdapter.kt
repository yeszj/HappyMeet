package cn.yanhu.baselib.widget.indicator

import android.content.Context
import android.view.animation.DecelerateInterpolator
import androidx.viewpager2.widget.ViewPager2
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator

/**
 * @author: witness
 * created: 2022/4/29
 * desc:
 */
class CommonIndicatorAdapter(
    private var viewpager: ViewPager2,
    private var titles: Array<String>,
    private var textSize: Float = CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.dp_14).toFloat(),
    private var selectColor: Int = R.color.fontTextColor,
    private var normalColor: Int = R.color.color6,
    private var lineColor: Int = R.color.colorMain,
    private var lineWidth: Float = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_16).toFloat(),
    private var lineHeight: Float = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_3).toFloat(),
    private var lineRound: Float = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_2).toFloat(),
    private var paddingLeft: Int = 0,
    private var selectTextSize: Float = textSize,
    private var mSmoothScroll:Boolean = true
) : CommonNavigatorAdapter() {
    override fun getCount(): Int {
        return titles.size
    }

    override fun getTitleView(context: Context, index: Int): IPagerTitleView {
        val titleView =
            CommonIndicatorTitleView(
                context,
                paddingLeft
            )
        titleView.normalColor = CommonUtils.getColor(normalColor)
        titleView.selectedColor = CommonUtils.getColor(selectColor)
        titleView.setNormalTextSize(textSize)
        titleView.setSelectTextSize(selectTextSize)
        titleView.text = titles[index]
        titleView.setOnSingleClickListener {
            index.also { viewpager.setCurrentItem(it,mSmoothScroll) }
        }
        return titleView
    }

    override fun getIndicator(context: Context?): IPagerIndicator {
        val indicator = LinePagerIndicator(context)
        indicator.mode = LinePagerIndicator.MODE_EXACTLY
        indicator.setColors(CommonUtils.getColor(lineColor))
        indicator.lineHeight = lineHeight
        indicator.lineWidth = lineWidth
        indicator.roundRadius = lineRound
        indicator.endInterpolator = DecelerateInterpolator(1.6f)
        return indicator
    }
}