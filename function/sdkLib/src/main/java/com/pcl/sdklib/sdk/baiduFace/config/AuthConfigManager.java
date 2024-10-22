package com.pcl.sdklib.sdk.baiduFace.config;

import android.content.Context;
import android.util.Log;


import com.baidu.idl.face.api.utils.FileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AuthConfigManager {

    private static final String FILE_NAME = "console_config.json";
    private static volatile AuthConfigManager instance;

    private ConsoleConfig config;

    public static AuthConfigManager getInstance(Context ctx) {
        if (instance == null) {
            synchronized (AuthConfigManager.class) {
                if (instance == null) {
                    instance = new AuthConfigManager(ctx.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private AuthConfigManager(Context ctx) {
        try {
            config = new ConsoleConfig();
            String json = FileUtil.readAssetFileUtf8String(ctx.getAssets(), FILE_NAME);
            JSONObject jsonObject = new JSONObject(json);
            config.parseFromJSONObject(jsonObject);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "初始配置读取失败", e);
            config = null;
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "初始配置读取失败, JSON格式不正确", e);
            config = null;
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "初始配置读取失败, JSON格式不正确", e);
            config = null;
        }
    }

    public ConsoleConfig getConfig() {
        return config;
    }
}
