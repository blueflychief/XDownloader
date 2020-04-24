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
    FileInfo get(String urlMd5);

    long put(String urlMd5, FileInfo fileInfo);

    int delete(String urlMd5);

    int deleteList(List<FileInfo> fileInfoList);

    List<FileInfo> queryByFinishTime(long minTimestamp, long maxTimestamp);

    List<FileInfo> query(int count);

    int shrink();

    void release();
}
