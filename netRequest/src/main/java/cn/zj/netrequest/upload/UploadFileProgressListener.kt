package cn.zj.netrequest.upload

/**
 * @author: zhengjun
 * created: 2023/5/16
 * desc:
 */
interface UploadFileProgressListener {
    fun onProgress(hasWrittenLen: Long, totalLen: Long)
    fun onUploadSuccess(url:String){}

    fun onUploadFail(msg:String){}
}