package cn.yanhu.baselib.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils

/**
 * @author zhengjun
 * @desc 上拉加载更多  当无更多数据，没有数据时显示的footview
 * @create at 2018/8/24 16:17
 */
class NoMoreDataFootView : LinearLayout {
    private var tvFoot: TextView? = null
    private var ivEmpty: ImageView? = null
    private var rlParent: ViewGroup? = null

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.footview_nomore_data, this, true)
        tvFoot = findViewById(R.id.tv_foot)
        ivEmpty = findViewById(R.id.iv_empty)
        rlParent = findViewById(R.id.rlParent)
    }

    var state = 0
    fun getFootViewState(): Int {
        return state;
    }

    /**
     * @desc 设置没有内容时显示的文案
     * @author zhengjun
     * @created at 2018/9/6 10:11
     */
    fun setFootText(text: String?) {
        tvFoot!!.text = text
    }

    fun setEmptyImgVisible(visibility: Int) {
        ivEmpty!!.visibility = visibility
    }

    //设置背景颜色
    fun setCustomBackGroundColor(colorId: Int) {
        rlParent!!.setBackgroundColor(ContextCompat.getColor(context,colorId))
    }

    fun footViewState(state: Int) {
        this.state = state;
        when (state) {
            FOOT_NO_MORE -> {
                tvFoot!!.visibility = VISIBLE
                tvFoot!!.text = context.getString(R.string.load_no_more)
                ivEmpty!!.visibility = GONE
            }
            FOOT_LOAD_ERROR -> {
                tvFoot!!.visibility = VISIBLE
                ivEmpty!!.visibility = VISIBLE
                ivEmpty!!.setImageResource(R.mipmap.wifi_404)
                tvFoot!!.text = context.getString(R.string.load_loading_fail)
            }
            FOOT_HIDE -> {
                tvFoot!!.visibility = GONE
                ivEmpty!!.visibility = GONE
            }
            FOOT_NO_DATA -> {
                tvFoot!!.visibility = VISIBLE
                ivEmpty!!.visibility = VISIBLE
                ViewUtils.setViewSize(ivEmpty,
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_120),
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_120))
                ivEmpty!!.setImageResource(R.drawable.svg_common_empty)
                tvFoot!!.text = context.getString(R.string.load_no_data)
            }
        }
    }

    companion object {
        const val FOOT_NO_MORE = 1 //显示底部footview 显示文案为 已经到底了
        const val FOOT_NO_DATA = 2 //无数据
        const val FOOT_HIDE = 3 //隐藏
        const val FOOT_LOAD_ERROR = 4 //加载出错
    }
}