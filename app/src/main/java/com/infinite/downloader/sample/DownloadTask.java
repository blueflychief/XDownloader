package com.infinite.downloader.sample;

import com.infinite.downloader.download.ComparableTask;
import com.infinite.downloader.record.Recorder;
import com.infinite.downloader.utils.Logger;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-09-10 - 12:00
 * Description: Class description
 */
public class DownloadTask extends ComparableTask {
    private static final String TAG = "DownloadTask";

    private Recorder recorder;

    public DownloadTask(Recorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public void run() {
        while (!isStopped()) {
            Logger.d(TAG, "task running");
        }
        Logger.e(TAG, "task end");
    }

}
