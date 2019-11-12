package com.infinite.downloader;

import android.app.Application;

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
        XDownload.get().init(this);
    }
}
