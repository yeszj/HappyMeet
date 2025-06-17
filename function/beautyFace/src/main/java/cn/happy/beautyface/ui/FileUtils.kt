/*
 * MIT License
 *
 * Copyright (c) 2023 Agora Community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package cn.happy.beautyface.ui

import android.content.Context
import android.text.TextUtils
import android.util.Log
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.commonres.manager.AppCacheManager
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.also
import kotlin.collections.sumOf
import kotlin.text.endsWith
import kotlin.text.lastIndexOf
import kotlin.text.replace
import kotlin.text.substring
import kotlin.toString

object FileUtils {
    val SEPARATOR: String = File.separator

    const val BUFFER: Int = 4096

    fun copyFileFromAssets(
        context: Context,
        assetsFilePath: String,
        storagePath: String
    ): String? {
        var storagePath = storagePath
        if (TextUtils.isEmpty(storagePath)) {
            return null
        } else if (storagePath.endsWith(SEPARATOR)) {
            storagePath = storagePath.substring(0, storagePath.length - 1)
        }

        if (TextUtils.isEmpty(assetsFilePath) || assetsFilePath.endsWith(SEPARATOR)) {
            return null
        }

        val storageFilePath =
            storagePath + SEPARATOR + assetsFilePath

        val assetManager = context.assets
        try {
            val file = File(storageFilePath)
            if (file.exists()) {
                return storageFilePath
            }
            file.parentFile.mkdirs()
            val inputStream = assetManager.open(assetsFilePath)
            readInputStream(storageFilePath, inputStream)
        } catch (e: IOException) {
            logcom("FileUtils", e.toString())
            return null
        }
        return storageFilePath
    }

    fun copyFileAndUnzipFromAssets(
        ctx: Context,
        assetsFilePath: String,
        storagePath: String?
    ): String? {
        var storagePath = storagePath
        if (TextUtils.isEmpty(storagePath)) {
            return null
        } else if (storagePath!!.endsWith(SEPARATOR)) {
            storagePath = storagePath.substring(0, storagePath.length - 1)
        }

        if (TextUtils.isEmpty(assetsFilePath) || assetsFilePath.endsWith(SEPARATOR)) {
            return null
        }

        val lastDotIndex = assetsFilePath.lastIndexOf('.')
        val assetDir = assetsFilePath.substring(0, lastDotIndex)
        val storageDirPath = storagePath + SEPARATOR
        var storageFilePath = storageDirPath + assetsFilePath
        val cacheFilePath = storageFilePath.replace(".zip", "")
        val file = File(cacheFilePath)
        if (file.exists() && AppCacheManager.beautyBundleSize != 0L && getDirSize(file) == AppCacheManager.beautyBundleSize) {
            return cacheFilePath
        }
        val assetManager = ctx.assets
        try {
            val file = File(storageFilePath)
            if (!file.exists()) {
                file.getParentFile().mkdirs()
                val inputStream = assetManager.open(assetsFilePath)
                readInputStream(storageFilePath, inputStream)
            }
            if (assetsFilePath.endsWith(".zip")) {
                unzipFile(storageFilePath, storageDirPath + assetDir)
                storageFilePath = storageDirPath + assetDir
                AppCacheManager.beautyBundleSize = getDirSize(File(storageFilePath))
            }
        } catch (e: IOException) {
            Log.e("FileUtils", "copyFileAndUnzipFromAssets: $e")
            return null
        }
        return storageFilePath
    }

    fun getDirSize(file: File): Long {
        if (file.isFile) return file.length()
        if (file.isDirectory) {
            return file.listFiles()?.sumOf { getDirSize(it) } ?: 0
        }
        return 0
    }


    private fun readInputStream(storagePath: String, inputStream: InputStream) {
        val file = File(storagePath)
        try {
            if (!file.exists()) {
                // 1.建立通道对象
                val fos = FileOutputStream(file)
                // 2.定义存储空间
                val buffer = ByteArray(inputStream.available())
                // 3.开始读文件
                var lenght = 0
                while ((inputStream.read(buffer).also { lenght = it }) != -1) { // 循环从输入流读取buffer字节
                    // 将Buffer中的数据写到outputStream对象中
                    fos.write(buffer, 0, lenght)
                }
                fos.flush() // 刷新缓冲区
                // 4.关闭流
                fos.close()
                inputStream.close()
            }
        } catch (e: IOException) {
            Log.e("FileUtils", "readInputStream: $e")
        }
    }

    fun unzipFile(zipFile: String, destPath: String) {
        var destPath = destPath
        if (!destPath.endsWith(File.separator)) {
            destPath += File.separator
        }
        val destFile = File(destPath)
        val items = destFile.listFiles()
        if (items != null && items.size > 0) {
            for (item in items) {
                item.delete()
            }
        }
        var fos: FileOutputStream?
        var zipIn: ZipInputStream? = null
        var zipEntry: ZipEntry?
        var file: File?
        var buffer: Int
        val buf: ByteArray? = ByteArray(BUFFER)
        try {
            zipIn = ZipInputStream(BufferedInputStream(FileInputStream(zipFile)))
            while ((zipIn.getNextEntry().also { zipEntry = it }) != null) {
                file = File(destPath + zipEntry!!.name)
                if (zipEntry.isDirectory) {
                    file.mkdirs()
                } else {
                    val parent = file.getParentFile()
                    if (!parent.exists()) {
                        parent.mkdirs()
                    }
                    fos = FileOutputStream(file)
                    while ((zipIn.read(buf).also { buffer = it }) > 0) {
                        fos.write(buf, 0, buffer)
                    }
                    fos.close()
                }
                zipIn.closeEntry()
                //delete zip
                val zipItem = File(zipFile)
                zipItem.delete()
            }
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        } finally {
            try {
                if (zipIn != null) {
                    zipIn.close()
                }
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            }
        }
    }
}