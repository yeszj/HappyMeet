package cn.yanhu.baselib.widget

import androidx.recyclerview.widget.RecyclerView




/**
 * @author: zhengjun
 * created: 2023/4/23
 * desc:
 */
class RecyclerViewScrollHelper {
    /**
     * 第一次进入界面时也会回调滚动，所以当手动滚动再监听
     */
    private var isNotFirst = false

    /**
     * 列表控件
     */
    private var scrollingView: RecyclerView? = null

    /**
     * 回调
     */
    private var callback: Callback? = null
    fun attachRecyclerView(scrollingView: RecyclerView?, callback: Callback?) {
        this.scrollingView = scrollingView
        this.callback = callback
        setup()
    }

    private fun setup() {
        scrollingView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                isNotFirst = true
                if (callback != null) {
                    callback!!.onScrollStateChanged(newState)
                    //如果滚动到最后一行，RecyclerView.canScrollVertically(1)的值表示是否能向上滚动，false表示已经滚动到底部
                    if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        !recyclerView.canScrollVertically(1)
                    ) {
                        callback!!.onScrolledToBottom()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (callback != null && isNotFirst) {
                    //RecyclerView.canScrollVertically(-1)的值表示是否能向下滚动，false表示已经滚动到顶部
                    if (!recyclerView.canScrollVertically(-1)) {
                        callback!!.onScrolledToTop()
                    }
                    //下滑
                    if (dy < 0) {
                        callback!!.onScrolledToDown(dy)
                    }
                    //上滑
                    if (dy > 0) {
                        callback!!.onScrolledToUp(dy)
                    }
                }
            }
        })
    }

    interface Callback {
        /**
         * 向下滚动
         */
        fun onScrolledToDown(dy:Int)

        /**
         * 向上滚动
         */
        fun onScrolledToUp(dy:Int)

        /**
         * 滚动到了顶部
         */
        fun onScrolledToTop(){}

        /**
         * 滚动到了底部
         */
        fun onScrolledToBottom(){}

        fun onScrollStateChanged(newState: Int){}
    }


    /**
     * 马上滚动到顶部
     */
    fun moveToTop() {
        if (scrollingView != null) {
            scrollingView!!.scrollToPosition(0)
        }
    }

    /**
     * 缓慢滚动到顶部
     */
    fun smoothMoveToTop() {
        if (scrollingView != null) {
            scrollingView!!.smoothScrollToPosition(0)
        }
    }
}