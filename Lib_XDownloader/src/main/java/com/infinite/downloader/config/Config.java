package com.infinite.downloader.config;

import android.content.Context;
import android.text.TextUtils;

import com.infinite.downloader.lru.DiskUsage;
import com.infinite.downloader.lru.TotalSizeLruDiskUsage;

import java.io.File;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 10/9/2019 - 23:29
 * Description: class description
 */
public class Config {
    private static final long ONE_M = 1024 * 1024;
    private static final int CONNECT_TIMEOUT = 15_000;
    private static final int READ_TIMEOUT = 15_000;
    private String saveDirPath;
    private int connectTimeout = CONNECT_TIMEOUT;
    private int readTimeout = READ_TIMEOUT;
    private boolean checkRemote = false;
    private DiskUsage diskUsage;

    public static Config defaultConfig(Context context) {
        Config config = new Config();
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            throw new NullPointerException("external cache dir null exist!!!");
        }
        String dirPath = cacheDir.getAbsolutePath() + File.separator + "xdownload";
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        config.setDiskUsage(new TotalSizeLruDiskUsage(1024 * ONE_M));
        config.setSaveDirPath(dirPath);
        return config;
    }

    public boolean existSaveDir() {
        if (TextUtils.isEmpty(saveDirPath)) {
            return false;
        }
        File dir = new File(saveDirPath);
        return dir.exists() && dir.isDirectory();
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

    public boolean isCheckRemote() {
        return checkRemote;
    }

    public void setCheckRemote(boolean checkRemote) {
        this.checkRemote = checkRemote;
    }

    public DiskUsage getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(DiskUsage diskUsage) {
        this.diskUsage = diskUsage;
    }
}
