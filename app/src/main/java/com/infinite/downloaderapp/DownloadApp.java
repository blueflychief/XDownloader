package com.infinite.downloaderapp;

import android.app.Application;

import com.infinite.downloader.XDownload;
import com.infinite.downloader.config.Config;
import com.infinite.downloader.lru.TotalSizeLruDiskUsage;
import com.infinite.downloader.utils.DLogger;

import java.io.File;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-11-12 - 14:54
 * Description: Class description
 */
public class DownloadApp extends Application {
    private static XDownload xDownload;

    @Override
    public void onCreate() {
        super.onCreate();
        Config config = Config.defaultConfig(this);
//        File saveDir = new File(Environment.getExternalStorageDirectory() + File.separator + "Android", "11100");
        File saveDir = new File(getExternalCacheDir().getAbsolutePath(), "cache_dir");
        if (!saveDir.isDirectory() || !saveDir.exists()) {
            saveDir.mkdir();
        }
        config.setSaveDirPath(saveDir.getAbsolutePath());
        config.setDiskUsage(new TotalSizeLruDiskUsage(200 * 1024 * 1024));
        DLogger.enable();
        xDownload = new XDownload(this, config);
    }

    public static XDownload getDownload() {
        return xDownload;
    }
}
