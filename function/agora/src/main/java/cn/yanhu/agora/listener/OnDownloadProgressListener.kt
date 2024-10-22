package cn.yanhu.agora.listener

/**
 * @author: zhengjun
 * created: 2023/8/3
 * desc:
 */
interface OnDownloadProgressListener {
    fun onProgress(progress: Int)
    fun onDownLoadFail()
}