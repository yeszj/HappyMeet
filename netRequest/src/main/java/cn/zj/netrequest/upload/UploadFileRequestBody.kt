package cn.zj.netrequest.upload

import android.util.Log
import cn.zj.netrequest.FilePartManager
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

/**
 * @author: zhengjun
 * created: 2023/5/16
 * desc:
 */
 class UploadFileRequestBody : RequestBody {
    private val mRequestBody: RequestBody
    private val mProgressListener: UploadFileProgressListener?
    private var bufferedSink: BufferedSink? = null

    constructor(path: String, key: String, progressListener: UploadFileProgressListener?) {
        mRequestBody = FilePartManager.getParam(path,key)
        mProgressListener = progressListener
    }

    constructor(requestBody: RequestBody, progressListener: UploadFileProgressListener?) {
        mRequestBody = requestBody
        mProgressListener = progressListener
    }

    //返回了requestBody的类型，想什么form-data/MP3/MP4/png等等等格式
    override fun contentType(): MediaType? {
        return mRequestBody.contentType()
    }

    //返回了本RequestBody的长度，也就是上传的totalLength
    @Throws(IOException::class)
    override fun contentLength(): Long {
        return mRequestBody.contentLength()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        if (sink is Buffer) {
            // Log Interceptor 解决java.net.ProtocolException: unexpected end of stream异常
            mRequestBody.writeTo(sink)
            return
        }
        if (bufferedSink == null) {
            //包装
            bufferedSink = sink(sink).buffer()
        }
        //写入
        mRequestBody.writeTo(bufferedSink!!)
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink!!.flush()
    }

    private fun sink(sink: Sink): Sink {
        return object : ForwardingSink(sink) {
            //当前写入字节数
            var bytesWritten = 0L

            //总字节长度，避免多次调用contentLength()方法
            var contentLength = 0L
            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                if (contentLength == 0L) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength()
                }
                //增加当前写入的字节数
                bytesWritten += byteCount
                //回调上传接口
               val hasFinish = bytesWritten == contentLength
                mProgressListener?.onProgress(
                    bytesWritten,
                    contentLength
                )
                Log.d(
                    "uploadProgress",
                    "hasUploadSize=$bytesWritten,totalSize = $contentLength,hasFinish= $hasFinish"
                )
            }
        }
    }
}