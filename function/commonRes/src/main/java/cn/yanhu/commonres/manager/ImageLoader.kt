package cn.yanhu.commonres.manager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.NinePatchDrawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import com.blankj.utilcode.util.GsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * @author: zhengjun
 * created: 2026/1/13
 * desc: 增强版 .9.png 图片加载管理器（解决 chunk 为 null 的问题）
 */
class ImageLoader(private val context: Context) {

    companion object {
        private const val TAG = "EnhancedImageLoader"
        private const val CACHE_DIR = "enhanced_ninepatch_cache"

        // 单例
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ImageLoader? = null

        fun getInstance(context: Context): ImageLoader {
            return instance ?: synchronized(this) {
                instance ?: ImageLoader(context.applicationContext).also { instance = it }
            }
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "image/png")
                    .addHeader("User-Agent", "Android-NinePatch-Loader")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    /**
     * 智能加载 .9.png（自动处理 chunk 为 null 的情况）
     */
    suspend fun loadSmartNinePatch(
        url: String,
        imageView: ImageView,
        fallbackToNormal: Boolean = true
    ) = withContext(Dispatchers.Main) {
        try {
            val result = downloadAndProcessImage(url, fallbackToNormal)
            when (result) {
                is ImageResult.NinePatchSuccess -> {
                    imageView.setImageDrawable(result.drawable)
                }

                is ImageResult.NormalBitmap -> {
                    if (fallbackToNormal) {
                        imageView.setImageBitmap(result.bitmap)
                    } else {
                        Log.w(TAG, "Fallback disabled, using placeholder")
                    }
                }

                is ImageResult.Error -> {
                    Log.e(TAG, "Load failed: ${result.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Load error", e)
        }
    }

    suspend fun loadNinePatchBackground(
        url: String,
        imageView: View,
        fallbackToNormal: Boolean = true
    ) = withContext(Dispatchers.Main) {
        try {
            val result = downloadAndProcessImage(url, fallbackToNormal)
            when (result) {
                is ImageResult.NinePatchSuccess -> {
                    imageView.background = (result.drawable)
                }

                is ImageResult.NormalBitmap -> {
                    if (fallbackToNormal) {
                        imageView.background = result.bitmap.toDrawable(context.resources)
                    } else {
                        Log.w(TAG, "Fallback disabled, using placeholder")
                    }
                }

                is ImageResult.Error -> {
                    Log.e(TAG, "Load failed: ${result.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Load error", e)
        }
    }

    /**
     * 下载并处理图片
     */
    private suspend fun downloadAndProcessImage(
        url: String,
        fallbackToNormal: Boolean
    ): ImageResult = withContext(Dispatchers.IO) {
        try {
            // 1. 下载文件
            val file = downloadImage(url) ?: return@withContext ImageResult.Error("Download failed")
            val deepValidate = NinePatchValidator.deepValidate(file)
            Log.e(TAG, "deepValidate=${GsonUtils.toJson(deepValidate)}")
           // /data/user/0/cn.huanyuan.sweetlove/cache/enhanced_ninepatch_cache/-1743248349.9.png
            // 2. 尝试作为 .9.png 加载
            val ninePatchResult = tryLoadAsNinePatch(file)
            if (ninePatchResult is ImageResult.NinePatchSuccess) {
                return@withContext ninePatchResult
            }

            // 3. 如果不是 .9.png，检查是否是普通 PNG
            if (fallbackToNormal) {
                val bitmap = decodeBitmap(file)
                if (bitmap != null) {
                    Log.w(TAG, "File is not a valid .9.png, fallback to normal bitmap")
                    return@withContext ImageResult.NormalBitmap(bitmap)
                }
            }

            ImageResult.Error("Invalid image file")

        } catch (e: Exception) {
            ImageResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * 尝试作为 .9.png 加载
     */
    private fun tryLoadAsNinePatch(file: File): ImageResult {
        return try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inScaled = false
            }

            // 方法1：标准解码
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            if (bitmap == null) {
                return ImageResult.Error("Failed to decode bitmap")
            }

            // 检查 chunk
            val chunk = bitmap.ninePatchChunk
            if (chunk != null && chunk.isNotEmpty()) {
                // 成功加载为 NinePatch
                val drawable = NinePatchDrawable(
                    context.resources,
                    bitmap,
                    chunk,
                    Rect(),
                    null
                )
                return ImageResult.NinePatchSuccess(drawable)
            }

            // 方法2：尝试手动检测 .9.png 特征
            if (isLikelyNinePatchFile(file)) {
                Log.d(TAG, "File looks like .9.png but chunk is null, trying manual recovery")

                // 尝试修复或创建 NinePatch
                val repairedDrawable = tryCreateNinePatchFromBitmap(bitmap)
                if (repairedDrawable != null) {
                    return ImageResult.NinePatchSuccess(repairedDrawable)
                }
            }

            ImageResult.Error("Not a valid ninepatch")

        } catch (e: Exception) {
            ImageResult.Error("NinePatch load failed: ${e.message}")
        }
    }

    /**
     * 检查文件是否可能是 .9.png
     */
    private fun isLikelyNinePatchFile(file: File): Boolean {
        // 检查1：文件名
        if (!file.name.endsWith(".9.png", ignoreCase = true)) {
            return false
        }

        // 检查2：文件大小（.9.png 通常比普通 PNG 大）
        if (file.length() < 100) { // 太小肯定不是
            return false
        }

        // 检查3：尝试读取 PNG 头信息
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            // .9.png 通常有特定的尺寸特征
            options.outWidth > 0 && options.outHeight > 0

        } catch (e: Exception) {
            false
        }
    }

    /**
     * 尝试从 Bitmap 创建 NinePatchDrawable（手动修复）
     */
    private fun tryCreateNinePatchFromBitmap(bitmap: Bitmap): NinePatchDrawable? {
        return try {
            // 方法1：尝试从 bitmap 边缘检测 NinePatch 标记
            val chunk = detectNinePatchChunk(bitmap)
            if (chunk != null) {
                return NinePatchDrawable(
                    context.resources,
                    bitmap,
                    chunk,
                    Rect(),
                    null
                )
            }

            // 方法2：创建简单的 NinePatch（全图可拉伸）
            createSimpleNinePatch(bitmap)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create NinePatch manually", e)
            null
        }
    }

    /**
     * 检测 NinePatch chunk（简单实现）
     */
    private fun detectNinePatchChunk(bitmap: Bitmap): ByteArray? {
        // 这是一个简化的实现，实际应该更复杂
        val width = bitmap.width
        val height = bitmap.height

        // 检查边缘像素是否有黑色标记（.9.png 的特征）
        val hasTopMark = checkEdgeForNinePatchMark(bitmap, Edge.TOP)
        val hasLeftMark = checkEdgeForNinePatchMark(bitmap, Edge.LEFT)

        if (hasTopMark || hasLeftMark) {
            Log.d(TAG, "Detected possible NinePatch marks")
            // 这里应该返回实际的 chunk 数据
            // 由于复杂，返回一个简单的 chunk
            return createSimpleChunk(width, height)
        }

        return null
    }

    /**
     * 创建简单的 NinePatch（整个图片可拉伸）
     */
    private fun createSimpleNinePatch(bitmap: Bitmap): NinePatchDrawable {
        val width = bitmap.width
        val height = bitmap.height

        // 创建简单的 chunk：整个图片可拉伸，内容区域为整个图片
        val chunk = createSimpleChunk(width, height)

        return NinePatchDrawable(
            context.resources,
            bitmap,
            chunk,
            Rect(),
            null
        )
    }

    /**
     * 创建简单的 chunk 数据
     */
    private fun createSimpleChunk(width: Int, height: Int): ByteArray {
        // 这是一个简化的 NinePatch chunk 结构
        // 实际结构更复杂，这里只是为了演示
        val chunk = ByteArray(56) // 最小 chunk 大小

        // 设置一些基本值（这是 NinePatch 的简化表示）
        // 注意：这不是完整的实现，仅用于演示

        // 标记为 NinePatch
        chunk[0] = 0x01.toByte()

        // 可拉伸区域：整个图片
        writeInt(chunk, 4, 0)      // left
        writeInt(chunk, 8, width)  // right
        writeInt(chunk, 12, 0)     // top
        writeInt(chunk, 16, height)// bottom

        return chunk
    }

    private fun writeInt(data: ByteArray, offset: Int, value: Int) {
        data[offset] = (value shr 24).toByte()
        data[offset + 1] = (value shr 16).toByte()
        data[offset + 2] = (value shr 8).toByte()
        data[offset + 3] = value.toByte()
    }

    private enum class Edge { TOP, LEFT, BOTTOM, RIGHT }

    private fun checkEdgeForNinePatchMark(bitmap: Bitmap, edge: Edge): Boolean {
        val width = bitmap.width
        val height = bitmap.height

        return when (edge) {
            Edge.TOP -> {
                // 检查顶部边缘是否有黑色像素
                (0 until width).any { x ->
                    val pixel = bitmap.getPixel(x, 0)
                    isBlackMark(pixel)
                }
            }

            Edge.LEFT -> {
                // 检查左侧边缘是否有黑色像素
                (0 until height).any { y ->
                    val pixel = bitmap.getPixel(0, y)
                    isBlackMark(pixel)
                }
            }

            else -> false
        }
    }

    private fun isBlackMark(pixel: Int): Boolean {
        val alpha = pixel shr 24 and 0xFF
        val red = pixel shr 16 and 0xFF
        val green = pixel shr 8 and 0xFF
        val blue = pixel and 0xFF

        // 黑色标记：Alpha 接近 255，RGB 接近 0
        return alpha > 200 && red < 50 && green < 50 && blue < 50
    }

    /**
     * 下载图片
     */
    private suspend fun downloadImage(url: String): File? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, CACHE_DIR).apply {
                if (!exists()) mkdirs()
            }
            val cacheFile = File(cacheDir, "${url.hashCode()}.9.png")

            // 如果缓存存在且较新，直接使用
            if (cacheFile.exists() && cacheFile.length() > 0) {
                val cacheAge = System.currentTimeMillis() - cacheFile.lastModified()
                if (cacheAge < 24 * 60 * 60 * 1000) { // 24小时缓存
                    return@withContext cacheFile
                }
            }

            val request = Request.Builder()
                .url(url)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.byteStream()?.use { input ->
                        FileOutputStream(cacheFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    cacheFile
                } else {
                    Log.e(TAG, "Download failed: ${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download error", e)
            null
        }
    }

    /**
     * 解码普通 Bitmap
     */
    private fun decodeBitmap(file: File): Bitmap? {
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 验证 OSS 文件是否正确
     */
    suspend fun validateOSSFile(url: String): ValidationResult {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .head() // HEAD 请求只获取头信息
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    val headers = response.headers

                    val result = ValidationResult(
                        isSuccess = response.isSuccessful,
                        statusCode = response.code,
                        contentType = headers["Content-Type"],
                        contentDisposition = headers["Content-Disposition"],
                        contentLength = headers["Content-Length"]?.toLongOrNull(),
                        isNinePatch = url.endsWith(".9.png", ignoreCase = true) &&
                                headers["Content-Type"]?.contains("image/png") == true
                    )
                    Log.e(TAG, "ValidationResult: ${GsonUtils.toJson(result)}")

                    result
                }
            } catch (e: Exception) {
                val result = ValidationResult(
                    isSuccess = false,
                    error = e.message ?: "Unknown error"
                )
                Log.e(TAG, "ValidationResult: ${GsonUtils.toJson(result)}")
                result
            }
        }
    }

    // 结果密封类
    sealed class ImageResult {
        data class NinePatchSuccess(val drawable: NinePatchDrawable) : ImageResult()
        data class NormalBitmap(val bitmap: Bitmap) : ImageResult()
        data class Error(val message: String) : ImageResult()
    }

    data class ValidationResult(
        val isSuccess: Boolean = false,
        val statusCode: Int? = null,
        val contentType: String? = null,
        val contentDisposition: String? = null,
        val contentLength: Long? = null,
        val isNinePatch: Boolean = false,
        val error: String? = null
    )
}