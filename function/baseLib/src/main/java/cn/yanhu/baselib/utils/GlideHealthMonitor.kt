package cn.yanhu.baselib.utils

import android.annotation.SuppressLint
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
    private const val RESET_WINDOW_MS = 10000L // 10秒内累计失败

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
        // 过滤掉可以忽略的异常
        if (shouldIgnoreException(e)) {
            return
        }
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
    private fun isNetworkRelatedException(e: Throwable?): Boolean {
        return e?.message?.contains("ENOENT") == true ||
                e?.message?.contains("EACCES") == true ||
                e?.message?.contains("timeout") == true ||
                e?.message?.contains("Network") == true
    }

    private fun isCancellationException(e: Throwable?): Boolean {
        return e?.message?.contains("Canceled") == true ||
                e?.message?.contains("cleared") == true
    }

    private fun shouldIgnoreException(e: Throwable?): Boolean {
        return when {
            e == null -> true
            e is java.util.concurrent.RejectedExecutionException -> {
                logcom("忽略 Glide 线程池拒绝异常（通常由生命周期引起）")
                true
            }

            isCancellationException(e) -> {
                logcom("忽略取消加载的异常）")
                true
            }

            isNetworkRelatedException(e) == true -> {
                logcom("忽略网络问题加载的异常")
                true
            }

            else -> false
        }
    }

    private fun clearAllGlideRequests() {
        try {
            // 清理内存缓存和活动资源
            Glide.get(appContext).apply {
                clearMemory()
                // 注意：这里不要调用 clearDiskCache()，因为它可能很慢
            }
        } catch (e: Exception) {
            logcom("清理 Glide 请求时异常: ${e.message}")
        }
    }

    /**
     * 主线程安全地重建 Glide 实例
     */
    @SuppressLint("VisibleForTests")
    @Synchronized
    fun recoverGlide() {
        if (isRecovering) return
        isRecovering = true

        handler.post {
            try {
                logcom("Glide 可能已全局失效，开始恢复...")
                // 1. 先暂停所有新的 Glide 请求
                Glide.with(appContext).pauseAllRequests()

                // 2. 清理所有现有请求
                clearAllGlideRequests()

                // 3. 等待一段时间让现有任务完成
                Thread.sleep(100)

                // 4.安全销毁旧实例
                //safeTearDown()

                // 5.重新初始化 Glide
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

    private fun safeTearDown() {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Glide.tearDown()
            } else {
                handler.post { Glide.tearDown() }
            }
        } catch (e: Exception) {
            logcom("Glide.tearDown() 异常（可忽略）: ${e.message}")
        }
    }
}
