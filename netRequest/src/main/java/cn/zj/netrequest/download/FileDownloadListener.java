package cn.zj.netrequest.download;

import java.io.File;

/**
 * @author: zhengjun
 * created: 2023/7/10
 * desc:
 */
public interface FileDownloadListener {
    void onProgress(int progress,long downloadedLength,long totalLength);
    void onFinish(File file);
    void onFailed(String msg);
}
