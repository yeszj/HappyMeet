package cn.zj.netrequest.download;

import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * @author: zhengjun
 * created: 2023/7/10
 * desc:
 */
public class DownloadUtil {
    private static final String TAG = "DownloadUtil";
    private static final int DEFAULT_TIMEOUT = 15;
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private OkHttpClient.Builder mBuilder;


    public void initConfig(OkHttpClient.Builder builder) {
        this.mBuilder = builder;
    }

    /**
     * download file and show the progress
     *
     * @param listener
     */
    public void downloadFile(InputParameter inputParam, final FileDownloadListener listener) {

        FileDownloadInterceptor interceptor = new FileDownloadInterceptor(listener);
        if (mBuilder != null) {
            mBuilder.addInterceptor(interceptor);
        } else {
            mBuilder = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .retryOnConnectionFailure(true)
                    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        }
        final DownloadService api = new Retrofit.Builder()
                .client(mBuilder.build())
                .baseUrl(inputParam.getBaseUrl())
                .build()
                .create(DownloadService.class);
        mExecutorService.execute(() -> {
            try {
                Response<ResponseBody> result = api.downloadWithDynamicUrl(inputParam.getRelativeUrl()).execute();
                File file = new FileUtil(ActivityUtils.getTopActivity()).write2SDFromInput(inputParam.getLoadedFilePath(), inputParam.getDir(),result.body().byteStream());
                if (listener != null) {
                    if (inputParam.isCallbackOnUiThread()) {
                        ThreadUtils.getMainHandler().post(() -> listener.onFinish(file));
                    } else {
                        listener.onFinish(file);
                    }
                }
            } catch (Exception e) {
                if (listener != null) {
                    if (inputParam.isCallbackOnUiThread()) {
                        ThreadUtils.getMainHandler().post(() -> listener.onFailed(e.getMessage()));
                    } else {
                        listener.onFailed(e.getMessage());
                    }
                }
                Log.e(TAG, e.getMessage(), e);
            }
        });
    }
}
