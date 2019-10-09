package com.infinite.downloader;

import android.content.Context;

import java.io.File;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 10/9/2019 - 23:29
 * Description: class description
 */
public class Config {
    private static final int CONNECT_TIMEOUT = 15_000;
    private static final int READ_TIMEOUT = 15_000;
    private String saveDirPath;
    private int connectTimeout = CONNECT_TIMEOUT;
    private int readTimeout = READ_TIMEOUT;

    public static Config defaultConfig(Context context) {
        Config config = new Config();
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            throw new NullPointerException("external cache dir null exist!!!");
        }
        config.setSaveDirPath(cacheDir.getAbsolutePath() + File.separator + "download");
        return config;
    }

    public String getSaveDirPath() {
        return saveDirPath;
    }

    public void setSaveDirPath(String saveDirPath) {
        this.saveDirPath = saveDirPath;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
