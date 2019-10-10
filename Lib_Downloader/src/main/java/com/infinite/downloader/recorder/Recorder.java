package com.infinite.downloader.recorder;


import com.infinite.downloader.config.FileInfo;

import java.util.List;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-08-28 - 18:07
 * Description: Class description
 */
public interface Recorder {
    FileInfo get(String url);

    long put(String url, FileInfo fileInfo);

    int delete(String url);

    List<FileInfo> queryAll();

    void release();
}
