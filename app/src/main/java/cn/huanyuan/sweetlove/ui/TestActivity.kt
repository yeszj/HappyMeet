package cn.huanyuan.sweetlove.ui

import android.annotation.SuppressLint
import cn.huanyuan.sweetlove.databinding.ActivityTestBinding
import cn.huanyuan.sweetlove.ui.main.MainViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.spannedgridlayoutmanager.SpanSize
import cn.yanhu.baselib.widget.spannedgridlayoutmanager.SpannedGridLayoutManager

/**
 * @author: zhengjun
 * created: 2025/1/13
 * desc:
 */
class TestActivity : BaseActivity<ActivityTestBinding, MainViewModel>(
    R.layout.activity_test,
    MainViewModel::class.java
) {
    private var isExpand = false
    private var selectPosition= 0
    @SuppressLint("NotifyDataSetChanged")
    override fun initData() {
        val layoutManager =
            SpannedGridLayoutManager(SpannedGridLayoutManager.Orientation.VERTICAL, 3)
        layoutManager.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { position ->
            if (!isExpand) {
                if (position == 0) {
                    SpanSize(3, 1)
                } else {
                    SpanSize(1, 1)
                }
            } else {
                when (position) {
                    0 -> {
                        SpanSize(3, 1)
                    }
                    selectPosition -> {
                        SpanSize(2, 2)
                    }
                    else -> {
                        SpanSize(1, 1)
                    }
                }
            }
        }
        mBinding.recyclerView.layoutManager = layoutManager
        val testAdapter = TestAdapter()
        mBinding.recyclerView.adapter = testAdapter
        val list = mutableListOf<String>()
        for (i in 0..6) {
            list.add(i.toString())
        }
        testAdapter.submitList(list)
        val layoutManager2 =
            SpannedGridLayoutManager(SpannedGridLayoutManager.Orientation.VERTICAL, 4)
        layoutManager2.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { position ->
            if (position == 1) {
                SpanSize(3, 4)
            } else {
                SpanSize(1, 1)
            }
        }
        mBinding.btnSwitch.setOnSingleClickListener {
            isExpand = true
            if (selectPosition==1){
                testAdapter.swap(1,6)
            }else{
                selectPosition = 1
                testAdapter.notifyItemChanged(selectPosition)
            }

           // testAdapter.notifyDataSetChanged()
            // layoutManager2.also { mBinding.recyclerView.layoutManager = it }
        }
    }
}