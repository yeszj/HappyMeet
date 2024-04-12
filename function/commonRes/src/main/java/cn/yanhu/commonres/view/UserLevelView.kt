package cn.yanhu.commonres.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import cn.yanhu.baselib.view.StrokeGradientTextView
import cn.yanhu.commonres.R
import cn.yanhu.commonres.config.LevelTagConfig

/**
 * @author: witness
 * created: 2022/12/5
 * desc:
 */
class UserLevelView : LinearLayout {
    private lateinit var tvLevel: StrokeGradientTextView

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView(context)
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_user_level, this, true)
        tvLevel = findViewById(R.id.tv_level)
    }


    @SuppressLint("SetTextI18n")
    fun setUserLevel(userLevel: Int) {
        if (userLevel <= 0) {
            tvLevel.visibility = View.GONE
        } else {
            val levelBg = LevelTagConfig.getLevelTagBg(userLevel)
            tvLevel.setBackgroundResource(levelBg)
            tvLevel.text = "Lv.$userLevel"
            tvLevel.visibility = View.VISIBLE
        }
    }

}