package cn.yanhu.baselib.func.photo;

import android.content.Context;
import android.net.Uri;

import com.luck.picture.lib.engine.CompressFileEngine;
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener;

import java.io.File;
import java.util.ArrayList;

import top.zibin.luban.Luban;
import top.zibin.luban.OnNewCompressListener;


/**
 * @author: witness
 * created: 2022/7/7
 * desc:
 */
class ImageCompressEngine implements CompressFileEngine {
    @Override
    public void onStartCompress(Context context, ArrayList<Uri> source, OnKeyValueResultCallbackListener call) {
        Luban.with(context).load(source).ignoreBy(100)
                .setCompressListener(new OnNewCompressListener() {
                    @Override
                    public void onStart() {
                    }
                    @Override
                    public void onSuccess(String source, File compressFile) {
                        if (call != null) {
                            call.onCallback(source, compressFile.getAbsolutePath());
                        }
                    }
                    @Override
                    public void onError(String source, Throwable e) {
                        if (call != null) {
                            call.onCallback(source, null);
                        }
                    }
                }).launch();
    }

}
