package cn.yanhu.baselib.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import cn.yanhu.baselib.utils.ext.logcom
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.executor.GlideExecutor
import java.util.concurrent.atomic.AtomicInteger

/**
 * 监控 Glide 全局健康状态 & 自动恢复
 *
 * 原理：
 * - 每次 Glide 加载失败时调用 [onLoadFailed]
 * - 若连续失败次数超过阈值（默认5次），自动重建 Glide 实例
 * - 支持主线程安全重启，避免 OOM、tearDown 异常
 */
object GlideHealthMonitor {

    private const val TAG = "GlideHealthMonitor"
    private const val FAIL_THRESHOLD = 5
    private const val RESET_WINDOW_MS = 10_000L // 10秒内累计失败

    private var lastFailTimestamp = 0L
    private val failCounter = AtomicInteger(0)
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var isRecovering = false

    /**
     * 应在 Application 初始化中调用一次
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private lateinit var appContext: Context

    /**
     * 每次 Glide 加载失败时调用此方法
     * 建议在 RequestListener.onLoadFailed() 或 GlobalErrorInterceptor 中调用
     */
    fun onLoadFailed(e: Throwable?) {
        val now = System.currentTimeMillis()

        if (now - lastFailTimestamp > RESET_WINDOW_MS) {
            failCounter.set(0) // 超过时间窗口，重置
        }

        lastFailTimestamp = now
        val count = failCounter.incrementAndGet()

        if (count >= FAIL_THRESHOLD && !isRecovering) {
            recoverGlide()
        }
    }

    /**
     * 主线程安全地重建 Glide 实例
     */
    @Synchronized
    fun recoverGlide() {
        if (isRecovering) return
        isRecovering = true

        handler.post {
            try {
                logcom("Glide 可能已全局失效，开始恢复...")

                // 安全销毁旧实例
                try {
                    Glide.tearDown()
                } catch (_: Exception) {
                }

                // 重新初始化 Glide
                Glide.init(appContext, GlideBuilder().apply {
                    setMemoryCache(LruResourceCache(10 * 1024 * 1024)) // 10MB
                    setSourceExecutor(GlideExecutor.newSourceExecutor())
                    setDiskCacheExecutor(GlideExecutor.newDiskCacheExecutor())
                })

                failCounter.set(0)
                lastFailTimestamp = 0
                logcom("Glide 恢复成功 ✅")
            } catch (e: Exception) {
                logcom("Glide 恢复失败 ❌: ${e.message}")
            } finally {
                isRecovering = false
            }
        }
    }
}
