package com.infinite.downloader.lru;

import java.io.File;
import java.io.IOException;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-11-14 - 17:02
 * Description: Class description
 */
public interface DiskUsage {

    void touch(File file) throws IOException;

}
