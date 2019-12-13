package com.infinite.downloader.lru;

import com.infinite.downloader.utils.DLogger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-11-14 - 17:03
 * Description: Class description
 */
public abstract class LruDiskUsage implements DiskUsage {
    private static int MIN_NOT_ALLOWED_DELETE_COUNT = 4;

    private final ExecutorService workerThread = Executors.newSingleThreadExecutor();

    @Override
    public void touch(File file) throws IOException {
        workerThread.submit(new TouchCallable(file));
    }

    private void touchInBackground(File file) throws IOException {
        Files.setLastModifiedNow(file);
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            List<File> files = Files.getLruListFiles(parentFile);
            int fileCount = files != null ? files.size() : 0;
            DLogger.d("touch in background,dir file count is:" + fileCount);
            //if the dir count is less than 5,not do trim,because maybe the latest file maybe is using!so do not delete!
            if (fileCount > MIN_NOT_ALLOWED_DELETE_COUNT) {
                trim(files);
            }
        }
    }

    protected abstract boolean accept(File file, long totalSize, int totalCount);

    private void trim(List<File> files) {
        long totalSize = countTotalSize(files);
        int totalCount = files.size();
        DLogger.d("file total count is " + totalCount + ",total size is " + totalSize);
        for (File file : files) {
            boolean accepted = accept(file, totalSize, totalCount);
            if (!accepted) {
                if (totalCount <= MIN_NOT_ALLOWED_DELETE_COUNT) {
                    return;
                }
                long fileSize = file.length();
                boolean deleted = file.delete();
                if (deleted) {
                    totalCount--;
                    totalSize -= fileSize;
                    DLogger.i("Cache file " + file + " is deleted because it exceeds cache limit,remain count is " + totalCount);
                } else {
                    DLogger.e("Error deleting file " + file + " for trimming cache");
                }
            }
        }
    }

    private long countTotalSize(List<File> files) {
        long totalSize = 0;
        for (File file : files) {
            totalSize += file.length();
        }
        return totalSize;
    }

    private class TouchCallable implements Callable<Void> {

        private final File file;

        public TouchCallable(File file) {
            this.file = file;
        }

        @Override
        public Void call() throws Exception {
            touchInBackground(file);
            return null;
        }
    }
}
