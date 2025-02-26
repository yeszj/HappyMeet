package cn.yanhu.commonres.view.roseAnim

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import cn.yanhu.commonres.R
import com.blankj.utilcode.util.ScreenUtils

/**
 * @author: zhengjun
 * created: 2025/2/10
 * desc:
 */
class RoseSurfaceView(context: Context?, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback, Runnable {
    private val holder: SurfaceHolder = getHolder()
    private var renderThread: Thread? = null
    private var isRunning = false
    private val roses: MutableList<Rose>
    private val rosePool: RosePool
    private val roseBitmap: Bitmap

    init {
        holder.addCallback(this)
        roses = ArrayList()
        roseBitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.icon_big_rose
        ) // Replace with your rose image
        rosePool = RosePool(roseBitmap, ScreenUtils.getScreenWidth(), 10)
        //设置背景透明
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    fun addRose() {
        val rose = rosePool.obtain()
        roses.add(rose)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        renderThread = Thread(this)
        renderThread!!.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        try {
            renderThread!!.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun run() {
        while (isRunning) {
            if (!holder.surface.isValid) {
                continue
            }
            val canvas = holder.lockCanvas()
            if (canvas != null) {
                //清空画布
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                synchronized(roses) {
                    for (i in roses.indices.reversed()) {
                        val rose = roses[i]
                        rose.update()
                        rose.draw(canvas)
                        if (rose.isOffScreen(height)) {
                            roses.removeAt(i)
                            rosePool.recycle(rose)
                        }
                    }
                }
                holder.unlockCanvasAndPost(canvas)
            }
            try {
                Thread.sleep(16) // ~60 FPS
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}