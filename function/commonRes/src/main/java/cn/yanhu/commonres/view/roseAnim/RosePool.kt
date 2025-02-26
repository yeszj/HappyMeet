package cn.yanhu.commonres.view.roseAnim

import android.graphics.Bitmap

/**
 * @author: zhengjun
 * created: 2025/2/10
 * desc:
 */
class RosePool(private val roseBitmap: Bitmap, private val screenWidth: Int, initialSize: Int) {
    private val pool: MutableList<Rose>

    init {
        pool = ArrayList()
        for (i in 0 until initialSize) {
            pool.add(Rose(roseBitmap, screenWidth))
        }
    }

    fun obtain(): Rose {
        return if (pool.isEmpty()) {
            Rose(roseBitmap, screenWidth)
        } else pool.removeAt(pool.size - 1)
    }

    fun recycle(rose: Rose) {
        rose.reset()
    }
}