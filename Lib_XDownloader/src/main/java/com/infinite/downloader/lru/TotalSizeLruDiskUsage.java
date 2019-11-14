package com.infinite.downloader.lru;

import java.io.File;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-11-14 - 17:07
 * Description: Class description
 */
public class TotalSizeLruDiskUsage extends LruDiskUsage {

    private final long maxSize;

    public TotalSizeLruDiskUsage(long maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be positive number!");
        }
        this.maxSize = maxSize;
    }

    @Override
    protected boolean accept(File file, long totalSize, int totalCount) {
        return totalSize <= maxSize;
    }
}
