package cn.zj.netrequest.download;


import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author: zhengjun
 * created: 2023/7/10
 * desc:
 */
public class FileDownloadInterceptor implements Interceptor {
    private final FileDownloadListener listener;
    public FileDownloadInterceptor(FileDownloadListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public Response intercept(okhttp3.Interceptor.Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        return originalResponse.newBuilder()
                .body(new FileDownloadResponseBody(originalResponse.body(), listener))
                .build();
    }
}
