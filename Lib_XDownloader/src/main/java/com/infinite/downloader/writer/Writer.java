package com.infinite.downloader.writer;

import java.io.IOException;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 10/9/2019 - 21:41
 * Description: class description
 */
public interface Writer {

    long saveFile(byte[] buffer, int length) throws IOException;

    void close();
}
