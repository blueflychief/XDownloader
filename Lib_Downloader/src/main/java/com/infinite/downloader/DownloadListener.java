package com.infinite.downloader;


import androidx.annotation.Nullable;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-09-24 - 18:41
 * Description: Class description
 */
public interface DownloadListener {
    void onDownloadStatus(@DownloadStatus.Status int status, @Nullable FileInfo info);
}
