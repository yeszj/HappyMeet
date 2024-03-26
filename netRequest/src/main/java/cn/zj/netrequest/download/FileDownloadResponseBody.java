package cn.zj.netrequest.download;


import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ThreadUtils;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * @author: zhengjun
 * created: 2023/7/10
 * desc:
 */
public class FileDownloadResponseBody extends ResponseBody {
    private final ResponseBody responseBody;
    private final FileDownloadListener downloadListener;
    private BufferedSource bufferedSource;

    public FileDownloadResponseBody(ResponseBody responseBody, FileDownloadListener downloadListener) {
        this.responseBody = responseBody;
        this.downloadListener = downloadListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(@NonNull Buffer sink, long byteCount) throws IOException {
                final long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                if (null != downloadListener) {
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    final int progress = (int) (totalBytesRead * 100 / responseBody.contentLength());
                    //Log.d("downLoad", "已经下载的：" + totalBytesRead + "共有：" + responseBody.contentLength() + "progress = " + progress);
                    ThreadUtils.getMainHandler().post(() -> downloadListener.onProgress(progress, totalBytesRead, responseBody.contentLength()));
                }
                return bytesRead;
            }
        };
    }
}
