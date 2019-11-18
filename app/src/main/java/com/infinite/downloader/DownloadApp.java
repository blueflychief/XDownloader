package com.infinite.downloader;

import android.app.Application;

import com.infinite.downloader.config.Config;
import com.infinite.downloader.lru.TotalSizeLruDiskUsage;
import com.infinite.downloader.utils.DLogger;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-11-12 - 14:54
 * Description: Class description
 */
public class DownloadApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Config config = Config.defaultConfig(this);
        DLogger.enable();
        config.setDiskUsage(new TotalSizeLruDiskUsage(20 * 1024 * 1024));//限制20M
        XDownload.get().init(this, config);
    }
}
