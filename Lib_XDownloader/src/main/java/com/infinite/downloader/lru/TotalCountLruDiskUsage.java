package com.infinite.downloader.lru;

import java.io.File;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-11-14 - 17:07
 * Description: Class description
 */
public class TotalCountLruDiskUsage extends LruDiskUsage {

    private final int maxCount;

    public TotalCountLruDiskUsage(int maxCount) {
        if (maxCount <= 0) {
            throw new IllegalArgumentException("Max count must be positive number!");
        }
        this.maxCount = maxCount;
    }

    @Override
    protected boolean accept(File file, long totalSize, int totalCount) {
        return totalCount <= maxCount;
    }
}