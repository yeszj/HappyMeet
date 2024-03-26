package cn.zj.netrequest.download;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @author: zhengjun
 * created: 2023/7/10
 * desc:
 */
public interface DownloadService {
    @Streaming
    @GET
    Call<ResponseBody> downloadWithDynamicUrl(@Url String fileUrl);
}
