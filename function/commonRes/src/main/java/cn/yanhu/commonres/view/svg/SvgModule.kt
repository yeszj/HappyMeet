package cn.yanhu.commonres.view.svg

import android.app.ActivityManager
import android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND
import android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE
import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.caverock.androidsvg.SVG
import java.io.InputStream

@GlideModule
class SvgModule : AppGlideModule() {
    override fun registerComponents(
        context: Context, glide: Glide, registry: Registry
    ) {
        registry
            .register<SVG?, PictureDrawable?>(
                SVG::class.java,
                PictureDrawable::class.java,
                SvgDrawableTranscoder()
            )
            .append<InputStream?, SVG?>(InputStream::class.java, SVG::class.java, SvgDecoder())
    }

    // Disable manifest parsing to avoid adding similar modules twice.
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // 设置内存缓存大小（根据设备内存动态调整）
        val memoryCacheSize = calculateMemoryCacheSize(context)
        builder.setMemoryCache(LruResourceCache(memoryCacheSize))

        // 设置 Bitmap 池大小
        val bitmapPoolSize = calculateBitmapPoolSize(context)
        builder.setBitmapPool(LruBitmapPool(bitmapPoolSize))
        //builder.setLogLevel(Log.VERBOSE)
        // 设置默认配置
        builder.setDefaultRequestOptions(
            RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .disallowHardwareConfig() // 避免某些设备的硬件加速问题
        )
    }

    private fun calculateMemoryCacheSize(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return if (activityManager.isLowRamDevice) {
            // 低内存设备使用更小的缓存
            10 * 1024 * 1024L // 10MB
        } else {
            // 正常设备
            20 * 1024 * 1024L // 20MB
        }
    }

    private fun calculateBitmapPoolSize(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return if (activityManager.isLowRamDevice) {
            5 * 1024 * 1024L // 5MB
        } else {
            10 * 1024 * 1024L // 10MB
        }
    }

    // 可选：应用前后台状态监听
    companion object {
        /**
         * 根据应用状态动态调整 Glide 配置
         * 直播应用可以在后台时释放更多资源
         */
        fun adjustForAppState(context: Context, isInBackground: Boolean) {
            try {
                val glide = Glide.get(context)

                if (isInBackground) {
                    // 应用在后台：清理内存
                    glide.clearMemory()

                    // 降低内存缓存大小
                    val trimMemoryLevel = if (isLowRamDevice(context)) {
                        // 低内存设备更激进
                        TRIM_MEMORY_COMPLETE
                    } else {
                        TRIM_MEMORY_BACKGROUND
                    }

                    glide.trimMemory(trimMemoryLevel)
                } else {
                    // 应用回到前台：恢复配置
                    glide.onLowMemory() // 触发内存优化
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun isLowRamDevice(context: Context): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            return activityManager.isLowRamDevice
        }
    }
}


