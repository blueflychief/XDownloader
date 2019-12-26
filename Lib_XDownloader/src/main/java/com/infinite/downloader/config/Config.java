package com.infinite.downloader.config;

import android.content.Context;
import android.text.TextUtils;

import com.infinite.downloader.lru.DiskUsage;
import com.infinite.downloader.lru.TotalSizeLruDiskUsage;
import com.infinite.downloader.utils.DLogger;

import java.io.File;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 10/9/2019 - 23:29
 * Description: class description
 */
public class Config {
    private static final long ONE_M = 1_048_576L;
    private static final int CONNECT_TIMEOUT = 15_000;
    private static final int READ_TIMEOUT = 15_000;
    private static final String DEFAULT_DOWNLOAD_DIR = "xdownload";
    private String saveDirPath;
    private int connectTimeout = CONNECT_TIMEOUT;
    private int readTimeout = READ_TIMEOUT;
    private boolean checkRemote = false;
    private DiskUsage diskUsage;

    public static Config defaultConfig(Context context) {
        Config config = new Config();
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        if (cacheDir == null) {
            throw new NullPointerException("external cache dir null exist!!!");
        }

        String dirPath = cacheDir.getAbsolutePath() + File.separator + DEFAULT_DOWNLOAD_DIR;
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            boolean isOk = dir.mkdirs();
            if (!isOk) {
                throw new IllegalStateException("create dir fail, path:" + dirPath);
            }
        }
        config.setSaveDirPath(dirPath);
        config.setDiskUsage(new TotalSizeLruDiskUsage(512 * ONE_M));
        return config;
    }

    public boolean existSaveDir() {
        if (TextUtils.isEmpty(saveDirPath)) {
            return false;
        }
        File dir = new File(saveDirPath);
        return dir.exists() && dir.isDirectory();
    }

    public synchronized boolean tryCreateSaveDir() {
        if (TextUtils.isEmpty(saveDirPath)) {
            return false;
        }
        boolean isOk = false;
        File dir = new File(saveDirPath);
        if (dir.exists() && dir.isDirectory()) {
            return true;
        }
        try {
            isOk = dir.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
            DLogger.e("create dir exception:" + e.getMessage());
        }
        return isOk;
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

    @Override
    public String toString() {
        return "Config{" +
                "saveDirPath='" + saveDirPath + '\'' +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", checkRemote=" + checkRemote +
                ", diskUsage=" + diskUsage +
                '}';
    }
}
