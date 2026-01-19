import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * 深度验证 .9.png 文件
 */
object NinePatchValidator {

    /**
     * 完整验证流程
     */
    fun deepValidate(file: File): ValidationResult {
        return try {
            // 1. 基本文件检查
            if (!file.exists()) return ValidationResult.error("File not exists")
            if (file.length() == 0L) return ValidationResult.error("File is empty")

            // 2. PNG 格式验证
            if (!isValidPng(file)) return ValidationResult.error("Not a valid PNG")

            // 3. 检查 NinePatch 标记
            val markResult = checkNinePatchMarks(file)
            if (!markResult.hasMarks) {
                return ValidationResult.error("No NinePatch marks found")
            }

            // 4. 尝试解码获取 chunk
            val decodeResult = tryDecodeForChunk(file)

            ValidationResult(
                isValid = decodeResult.hasChunk,
                hasMarks = markResult.hasMarks,
                hasChunk = decodeResult.hasChunk,
                width = decodeResult.width,
                height = decodeResult.height,
                error = if (decodeResult.hasChunk) null else "Chunk is null",
                marksInfo = markResult
            )

        } catch (e: Exception) {
            ValidationResult.error("Validation failed: ${e.message}")
        }
    }

    /**
     * 详细检查 NinePatch 标记
     */
    private fun checkNinePatchMarks(file: File): MarksInfo {
        return try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            if (bitmap == null) return MarksInfo.error("Failed to decode")

            val width = bitmap.width
            val height = bitmap.height

            val topMarks = mutableListOf<Int>()
            val leftMarks = mutableListOf<Int>()
            val bottomMarks = mutableListOf<Int>()
            val rightMarks = mutableListOf<Int>()

            // 检查顶部边缘
            for (x in 0 until width) {
                if (isBlackMark(bitmap.getPixel(x, 0))) {
                    topMarks.add(x)
                }
            }

            // 检查左侧边缘
            for (y in 0 until height) {
                if (isBlackMark(bitmap.getPixel(0, y))) {
                    leftMarks.add(y)
                }
            }

            // 检查底部边缘
            for (x in 0 until width) {
                if (isBlackMark(bitmap.getPixel(x, height - 1))) {
                    bottomMarks.add(x)
                }
            }

            // 检查右侧边缘
            for (y in 0 until height) {
                if (isBlackMark(bitmap.getPixel(width - 1, y))) {
                    rightMarks.add(y)
                }
            }

            bitmap.recycle()

            MarksInfo(
                hasMarks = topMarks.isNotEmpty() && leftMarks.isNotEmpty(),
                topMarks = topMarks,
                leftMarks = leftMarks,
                bottomMarks = bottomMarks,
                rightMarks = rightMarks,
                width = width,
                height = height
            )

        } catch (e: Exception) {
            MarksInfo.error(e.message ?: "Unknown error")
        }
    }

    /**
     * 尝试解码获取 chunk
     */
    private fun tryDecodeForChunk(file: File): DecodeResult {
        return try {
            // 方法1：标准解码
            val options1 = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inScaled = false
            }

            val bitmap1 = BitmapFactory.decodeFile(file.absolutePath, options1)
            val chunk1 = bitmap1?.ninePatchChunk

            // 方法2：尝试不同的配置
            val options2 = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            val bitmap2 = BitmapFactory.decodeFile(file.absolutePath, options2)
            val chunk2 = bitmap2?.ninePatchChunk

            // 方法3：从流解码
            val inputStream = file.inputStream()
            val options3 = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap3 = BitmapFactory.decodeStream(inputStream, null, options3)
            inputStream.close()
            val chunk3 = bitmap3?.ninePatchChunk

            // 清理
            bitmap1?.recycle()
            bitmap2?.recycle()
            bitmap3?.recycle()

            DecodeResult(
                hasChunk = chunk1 != null || chunk2 != null || chunk3 != null,
                width = bitmap1?.width ?: 0,
                height = bitmap1?.height ?: 0,
                chunkSizes = listOf(
                    chunk1?.size ?: 0,
                    chunk2?.size ?: 0,
                    chunk3?.size ?: 0
                )
            )

        } catch (e: Exception) {
            DecodeResult.error(e.message ?: "Decode failed")
        }
    }

    private fun isValidPng(file: File): Boolean {
        return try {
            val bytes = ByteArray(8)
            file.inputStream().use { it.read(bytes) }
            bytes.contentEquals(byteArrayOf(0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun isBlackMark(pixel: Int): Boolean {
        return (pixel and 0xFF000000.toInt()) == 0xFF000000.toInt() && // Alpha = 255
                (pixel and 0x00FFFFFF) == 0 // RGB = 0
    }

    data class ValidationResult(
        val isValid: Boolean = false,
        val hasMarks: Boolean = false,
        val hasChunk: Boolean = false,
        val width: Int = 0,
        val height: Int = 0,
        val error: String? = null,
        val marksInfo: MarksInfo? = null
    ) {
        companion object {
            fun error(message: String) = ValidationResult(error = message)
        }
    }

    data class MarksInfo(
        val hasMarks: Boolean,
        val topMarks: List<Int>,
        val leftMarks: List<Int>,
        val bottomMarks: List<Int>,
        val rightMarks: List<Int>,
        val width: Int,
        val height: Int,
        val error: String? = null
    ) {
        companion object {
            fun error(message: String) = MarksInfo(
                hasMarks = false,
                topMarks = emptyList(),
                leftMarks = emptyList(),
                bottomMarks = emptyList(),
                rightMarks = emptyList(),
                width = 0,
                height = 0,
                error = message
            )
        }

        fun getMarkPattern(): String {
            return "Top: ${topMarks.joinToString(",")}\n" +
                    "Left: ${leftMarks.joinToString(",")}\n" +
                    "Bottom: ${bottomMarks.joinToString(",")}\n" +
                    "Right: ${rightMarks.joinToString(",")}"
        }
    }

    data class DecodeResult(
        val hasChunk: Boolean,
        val width: Int,
        val height: Int,
        val chunkSizes: List<Int>,
        val error: String? = null
    ) {
        companion object {
            fun error(message: String) = DecodeResult(
                hasChunk = false,
                width = 0,
                height = 0,
                chunkSizes = emptyList(),
                error = message
            )
        }
    }
}