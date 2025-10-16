package cn.yanhu.commonres.view.roseAnim

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import cn.yanhu.commonres.R
import com.blankj.utilcode.util.ScreenUtils

class RoseSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, Runnable {

    private val surfaceHolder: SurfaceHolder = holder
    private var renderThread: Thread? = null
    private var isRunning = false

    private val roses = mutableListOf<Rose>()
    private val rosePool: RosePool
    private val roseBitmap: Bitmap

    // 帧率控制
    private val targetFps = 60
    private val frameTimeMs = 1000L / targetFps

    // 性能监控
    private var frameCount = 0
    private var lastFpsTime = 0L

    init {
        surfaceHolder.addCallback(this)

        // 加载位图并优化
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565 // 减少内存占用
        }
        roseBitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_big_rose, options)
            ?: throw IllegalArgumentException("Rose bitmap not found!")

        rosePool = RosePool(roseBitmap, ScreenUtils.getScreenWidth(), 10)

        setupSurface()
    }

    private fun setupSurface() {
        // 设置背景透明
        setZOrderOnTop(true)
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT)

        // 提高绘制性能
        surfaceHolder.setFixedSize(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight())
    }

    fun addRose() {
        synchronized(roses) {
            val rose = rosePool.obtain()
            roses.add(rose)
        }
    }

    fun addRoses(count: Int) {
        synchronized(roses) {
            repeat(count) {
                val rose = rosePool.obtain()
                roses.add(rose)
            }
        }
    }

    fun clearRoses() {
        synchronized(roses) {
            roses.forEach { rosePool.recycle(it) }
            roses.clear()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        renderThread = Thread(this, "RoseRenderThread")
        renderThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // 处理尺寸变化
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        renderThread?.join(1000) // 等待1秒线程结束
        renderThread = null
        clearRoses()
    }

    override fun run() {
        var lastTime = System.currentTimeMillis()

        while (isRunning) {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - lastTime

            if (!surfaceHolder.surface.isValid) {
                Thread.yield()
                continue
            }

            renderFrame()

            // 精确的帧率控制
            val renderTime = System.currentTimeMillis() - currentTime
            val sleepTime = maxOf(0, frameTimeMs - renderTime)

            try {
                Thread.sleep(sleepTime)
            } catch (e: InterruptedException) {
                // 线程被中断，退出循环
                break
            }

            lastTime = currentTime
            updateFps()
        }
    }

    private fun renderFrame() {
        var canvas: Canvas? = null
        try {
            canvas = surfaceHolder.lockCanvas()
            if (canvas != null) {
                drawFrame(canvas)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (canvas != null) {
                try {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun drawFrame(canvas: Canvas) {
        // 清空画布
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        synchronized(roses) {
            val iterator = roses.iterator()
            while (iterator.hasNext()) {
                val rose = iterator.next()
                rose.update()
                rose.draw(canvas)

                if (rose.isOffScreen(height)) {
                    iterator.remove()
                    rosePool.recycle(rose)
                }
            }
        }
    }

    private fun updateFps() {
        frameCount++
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFpsTime >= 1000) {
            val fps = frameCount
            frameCount = 0
            lastFpsTime = currentTime
            // 可以在这里记录或显示FPS
        }
    }

    fun pause() {
        isRunning = false
    }

    fun resume() {
        if (!isRunning && surfaceHolder.surface.isValid) {
            isRunning = true
            renderThread = Thread(this, "RoseRenderThread")
            renderThread?.start()
        }
    }

    override fun onDetachedFromWindow() {
        isRunning = false
        renderThread?.join(500)
        clearRoses()
        roseBitmap.recycle()
        super.onDetachedFromWindow()
    }

    fun getRoseCount(): Int = synchronized(roses) { roses.size }
}