package com.infinite.downloader;

import java.io.IOException;


/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 10/9/2019 - 22:23
 * Description: class description
 */
public interface StreamReader {
    FileInfo getFileInfo(String url, long offset);

    int readInputStream(byte[] buffer) throws IOException;
}
