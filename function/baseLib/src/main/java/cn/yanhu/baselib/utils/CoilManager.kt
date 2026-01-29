package cn.yanhu.baselib.utils

import android.content.Context
import coil.ComponentRegistry
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * @author: zhengjun
 * created: 2026/1/20
 * desc:
 */
object CoilManager {
    private var imageLoader: ImageLoader? = null

    fun getImageLoader(context: Context): ImageLoader {
        return imageLoader ?: createImageLoader(context).also {
            imageLoader = it
        }
    }

    private fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components(fun ComponentRegistry.Builder.() {
                add(SvgDecoder.Factory())
            })
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coil_cache"))
                    .maxSizeBytes(512L * 1024 * 1024) // 512MB
                    .build()
            }

            .crossfade(500)
            .respectCacheHeaders(true)
            .allowHardware(false) // 允许硬件加速
            .allowRgb565(true) // 使用RGB_565节省内存
            .build()
    }

    @OptIn(ExperimentalCoilApi::class)
    fun clearCache() {
        imageLoader?.apply {
            memoryCache?.clear()
            diskCache?.clear()
        }
    }
}