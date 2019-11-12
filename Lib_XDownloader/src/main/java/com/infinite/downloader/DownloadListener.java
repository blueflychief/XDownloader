package com.infinite.downloader;


import androidx.annotation.Nullable;

import com.infinite.downloader.config.DownloadStatus;
import com.infinite.downloader.config.FileInfo;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-09-24 - 18:41
 * Description: Class description
 */
public interface DownloadListener {
    /**
     * This method will not run in ui thread,so if you need change view,handle with these:
     * runOnUiThread(new Runnable() {
     *
     * @param status download status,see{@link DownloadStatus}
     * @param info   information of the file what downloading
     * @Override public void run() {
     * <p>
     * }
     * });
     */
    void onDownloadStatus(@DownloadStatus.Status int status, @Nullable FileInfo info);
}
