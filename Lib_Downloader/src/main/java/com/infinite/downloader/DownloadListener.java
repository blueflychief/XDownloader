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
    void onDownloadStatus(@DownloadStatus.Status int status, @Nullable FileInfo info);
}
