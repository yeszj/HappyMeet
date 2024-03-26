package cn.yanhu.baselib.utils

import android.content.Context
import android.os.Looper
import android.text.TextUtils
import cn.yanhu.baselib.utils.ext.showToast
import com.blankj.utilcode.util.ThreadUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import java.io.File

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
object CacheSizeManager {

    /**
     * 获取总缓存大小
     *  SDCard/Android/data/应用包名/cache 路径下的缓存大小
     *  +上glide图片的缓存大小
     * @return CacheSize
     */
    fun getCacheSize(context: Context): String? {
        try {
            val glideCacheSize = getGlideCacheSize(context)
            val appCacheDirSize = getAppCacheDirSize(context)
            return CommonUtils.formatSize(context, appCacheDirSize + glideCacheSize)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getAppCacheDirSize(context: Context): Long {
        val externalCacheDir = context.externalCacheDir
        externalCacheDir?.apply {
            return getFolderSize(externalCacheDir)
        }
        return 0
    }

    private fun getGlideCacheSize(context: Context): Long {
        return getFolderSize(File(context.cacheDir.toString() + "/" + InternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR))
    }

    /**
     * 获取指定文件夹内所有文件大小的和
     *
     * @param file file
     * @return size
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList = file.listFiles()
            fileList?.forEach { aFileList ->
                size = if (aFileList.isDirectory) {
                    size + getFolderSize(aFileList)
                } else {
                    size + aFileList.length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }


    /**
     * 清除所有缓存
     */
    fun clearAllCache(context: Context,onClearCacheListener: OnClearCacheListener?=null) {

        DialogUtils.showLoading("正在清除...")
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Boolean>() {
            override fun onSuccess(result: Boolean) {
                showToast("清除成功")
                onClearCacheListener?.onClearSuccess()
                DialogUtils.dismissLoading()
            }
            override fun doInBackground(): Boolean {
                try {
                    clearImageDiskCache(context)
                    clearImageMemoryCache(context)
                    val imageExternalCatchDir =
                        context.externalCacheDir.toString()
                    deleteFolderFile(imageExternalCatchDir)
                }catch (e:Exception){
                    return false
                }
                return true
            }
        })


    }

    /**
     * 删除指定目录下的文件，这里用于缓存的删除
     * @param filePath       filePath
     */
    private fun deleteFolderFile(filePath: String) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                val file = File(filePath)
                if (file.isDirectory) {
                    val files = file.listFiles()
                    files?.forEach { file1 ->
                        deleteFolderFile(file1.absolutePath)
                    }
                }
                if (!file.isDirectory) {
                    file.delete()
                } else {
                    if (file.listFiles()?.isEmpty() == true) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 清除图片磁盘缓存
     */
    private fun clearImageDiskCache(context: Context?) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Thread { Glide.get(context!!).clearDiskCache() }.start()
            } else {
                Glide.get(context!!).clearDiskCache()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 清除图片内存缓存
     */
    private fun clearImageMemoryCache(context: Context?) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(context!!).clearMemory()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnClearCacheListener{
        fun onClearSuccess();
    }
}