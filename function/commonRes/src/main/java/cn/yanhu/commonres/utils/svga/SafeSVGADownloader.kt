//package cn.yanhu.commonres.utils.svga
//
///**
// * @author: zhengjun
// * created: 2025/5/19
// * desc:
// */
//import com.opensource.svgaplayer.SVGAParser
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import java.io.ByteArrayOutputStream
//import java.io.IOException
//import java.io.InputStream
//import java.net.URL
//import java.util.concurrent.TimeUnit
//
//class SafeSVGADownloader : SVGAParser.FileDownloader() {
//
//    // 使用 OkHttp 代替原生 HttpURLConnection
//    private val okHttpClient by lazy {
//        OkHttpClient.Builder()
//            .connectTimeout(15, TimeUnit.SECONDS)
//            .readTimeout(15, TimeUnit.SECONDS)
//            .writeTimeout(15, TimeUnit.SECONDS)
//            .build()
//    }
//
//    // 最大允许的文件大小 (8MB)
//    private val MAX_FILE_SIZE = 8 * 1024 * 1024L
//
//    // 内存缓冲区大小 (128KB)
//    private val BUFFER_SIZE = 128 * 1024
//
//    override fun resume(
//        url: URL,
//        complete: (inputStream: InputStream) -> Unit,
//        failure: (error: Exception) -> Unit
//    ): () -> Unit {
//        try {
//            // 1. 检查内存状态
//            checkMemoryAvailability()
//
//            // 2. 创建带进度监控的请求
//            val request = Request.Builder()
//                .url(url)
//                .header("Accept-Encoding", "identity") // 禁用压缩，避免自动解压占用内存
//                .build()
//
//            okHttpClient.newCall(request).execute().use { response ->
//                if (!response.isSuccessful) {
//                    throw IOException("Unexpected code $response")
//                }
//
//                // 3. 检查文件大小
//                val contentLength = response.body?.contentLength() ?: 0L
//                if (contentLength > MAX_FILE_SIZE) {
//                    throw IOException("SVGA file too large: ${contentLength / 1024}KB")
//                }
//
//                // 4. 使用分块读取方式
//                response.body?.byteStream()?.use { inputStream ->
//                    val outputStream = ByteArrayOutputStream().apply {
//                        if (contentLength > 0) {
//                            // 预分配空间 (避免多次扩容)
//                            this.ensureCapacity(contentLength.toInt())
//                        }
//                    }
//
//                    val buffer = ByteArray(BUFFER_SIZE)
//                    var bytesRead: Int
//                    var totalRead = 0L
//
//                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
//                        // 5. 检查内存是否足够
//                        if (totalRead + bytesRead > MAX_FILE_SIZE) {
//                            throw IOException("File exceeds size limit during download")
//                        }
//
//                        outputStream.write(buffer, 0, bytesRead)
//                        totalRead += bytesRead
//
//                        // 6. 定期检查内存压力
//                        if (totalRead % (512 * 1024) == 0L) {
//                            checkMemoryAvailability()
//                        }
//                    }
//
//                    // 7. 转换为ByteArrayInputStream
//                    complete(outputStream.toByteArray().inputStream())
//                }
//            }
//        } catch (e: Exception) {
//            // 8. 错误处理
//            handleDownloadError(e, failure)
//        }
//    }
//
//    private fun checkMemoryAvailability() {
//        val runtime = Runtime.getRuntime()
//        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
//        val maxMemory = runtime.maxMemory()
//
//        if (usedMemory > maxMemory * 0.7) {
//            // 内存使用超过70%时主动GC
//            System.gc()
//            Thread.sleep(100) // 给GC一点时间
//
//            // 再次检查
//            val newUsedMemory = runtime.totalMemory() - runtime.freeMemory()
//            if (newUsedMemory > maxMemory * 0.8) {
//                throw OutOfMemoryError("Memory usage too high: ${newUsedMemory / 1024 / 1024}MB/${maxMemory / 1024 / 1024}MB")
//            }
//        }
//    }
//
//    private fun handleDownloadError(e: Exception, failure: (Exception) -> Unit) {
//        when (e) {
//            is OutOfMemoryError -> {
//                // 内存不足的特殊处理
//                System.gc()
//                failure(IOException("Insufficient memory to load SVGA", e))
//            }
//            is IOException -> {
//                // 网络或IO错误
//                failure(e)
//            }
//            else -> {
//                // 其他未知错误
//                failure(RuntimeException("Failed to download SVGA", e))
//            }
//        }
//    }
//}