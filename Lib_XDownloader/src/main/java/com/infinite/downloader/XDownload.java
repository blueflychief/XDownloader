package com.infinite.downloader;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.infinite.downloader.config.Config;
import com.infinite.downloader.config.FileInfo;
import com.infinite.downloader.recorder.Recorder;
import com.infinite.downloader.recorder.SqliteRecorder;
import com.infinite.downloader.task.DownloadTask;
import com.infinite.downloader.utils.CommonUtils;
import com.infinite.downloader.utils.DLogger;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-11-12 - 11:06
 * Description:
 * 注意：务必保证各个XDownload中的Config.saveDirPath不一致！！！
 */
public class XDownload {
    private Context appContext;
    private ThreadPoolExecutor threadPoolExecutor;
    private Config downloadConfig;
    private static Recorder recorder;//use static,SQLite must be a singleton
    private boolean initialized;
    private final Map<String, DownloadTask> TASK_MAP = new HashMap<>(32);

    private XDownload() {
        throw new IllegalStateException("XDownload not allowed invoke this constructor !!!");
    }

    public XDownload(Context appContext) {
        init(appContext, null);
    }

    public XDownload(Context appContext, Config config) {
        init(appContext, config);
    }

    private void init(Context context, Config config) {
        if (!initialized) {
            appContext = context.getApplicationContext();
            downloadConfig = config != null ? config : Config.defaultConfig(appContext);
            if (recorder == null) {
                recorder = new SqliteRecorder(appContext);
            }
            int cpuCount = Runtime.getRuntime().availableProcessors();
            threadPoolExecutor = new ThreadPoolExecutor(
                    cpuCount + 3,
                    cpuCount << 5,
                    60, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new ThreadFactory() {
                        private int index;

                        @Override
                        public Thread newThread(Runnable r) {
                            Thread thread = new Thread(r, "[XDownload-" + index + "]");
                            index++;
                            return thread;
                        }
                    });
//            recorder.shrink();
            initialized = true;
            DLogger.d("XDownload is initialized，config:\n" + downloadConfig);
        } else {
            DLogger.d("XDownload has initialized already!");
        }
    }

    /**
     * add a download task with url.
     *
     * @param url download url
     * @return DownloadTask
     */
    public DownloadTask addTask(String url) {
        return addTask(url, null);
    }

    /**
     * add a download task with url and listener
     *
     * @param url      url
     * @param listener listener
     * @return DownloadTask, is url is invalid,return null.
     */
    @Nullable
    public DownloadTask addTask(String url, DownloadListener listener) {
        shrink();
        DownloadTask task = null;
        String md5 = CommonUtils.computeTaskMd5(url, downloadConfig.getSaveDirPath());
        if (!TextUtils.isEmpty(md5)) {
            synchronized (TASK_MAP) {
                DownloadTask t = TASK_MAP.get(md5);
                if (t == null || t.dead()) {
                    task = new DownloadTask(appContext, url, recorder, downloadConfig);
                    TASK_MAP.put(md5, task);
                    if (listener != null) {
                        task.addDownloadListener(listener);
                    }
                    threadPoolExecutor.submit(task);
                    DLogger.d("add a new task:" + task.getUrlMd5());
                } else {
                    task = t;
                    if (listener != null) {
                        task.addDownloadListener(listener);
                    }
                    DLogger.d("task:" + task.getUrlMd5() + " has exist already");
                }
            }
        } else {
            DLogger.e("add task fail,md5 is " + md5);
        }
        return task;
    }

    /**
     * get a download task with url,maybe the task is dead.
     *
     * @param url url
     * @return DownloadTask
     */
    @Nullable
    public DownloadTask getTask(String url) {
        if (!TextUtils.isEmpty(url)) {
            String md5 = CommonUtils.computeTaskMd5(url, downloadConfig.getSaveDirPath());
            synchronized (TASK_MAP) {
                return TASK_MAP.get(md5);
            }
        }
        return null;
    }

    /**
     * Delete download record by finish time
     *
     * @param minTimestamp
     * @param maxTimestamp
     * @param deleteFile
     * @return
     */
    public int deleteByFinishTime(long minTimestamp, long maxTimestamp, boolean deleteFile) {
        return deleteByTime(true, minTimestamp, maxTimestamp, deleteFile);
    }

    /**
     * Delete download record by start time
     *
     * @param minTimestamp
     * @param maxTimestamp
     * @param deleteFile
     * @return
     */
    public int deleteByStartTime(long minTimestamp, long maxTimestamp, boolean deleteFile) {
        return deleteByTime(false, minTimestamp, maxTimestamp, deleteFile);
    }

    private int deleteByTime(boolean isFinishTime, long minTimestamp, long maxTimestamp, boolean deleteFile) {
        boolean tempRecorder = recorder == null;
        Recorder r = recorder != null ? recorder : new SqliteRecorder(appContext);
        List<FileInfo> fileInfoList = isFinishTime ?
                r.queryByFinishTime(minTimestamp, maxTimestamp) :
                r.queryByStartTime(minTimestamp, maxTimestamp);
        int count = fileInfoList != null ? fileInfoList.size() : 0;
        if (count > 0) {
            if (deleteFile) {
                for (FileInfo fileInfo : fileInfoList) {
                    if (fileInfo != null) {
                        if (DLogger.isDebugEnable()) {
                            DLogger.d("delete record file " + fileInfo.getFileName()
                                    + ",finish time:" + fileInfo.getFinishTime());
                        }
                        fileInfo.deleteFile();
                    }
                }
            }
            r.deleteList(fileInfoList);
        }
        if (tempRecorder) {
            r.release();
        }
        return count;
    }

    /**
     * delete record that hos no local download file
     *
     * @return
     */
    public int deleteInvalidRecord() {
        boolean tempRecorder = recorder == null;
        Recorder r = recorder != null ? recorder : new SqliteRecorder(appContext);
        int count = r.shrink();
        if (tempRecorder) {
            r.release();
        }
        return count;
    }

    /**
     * remove a download task with url
     *
     * @param url download url
     * @return task removed
     */
    public boolean removeTask(String url) {
        DownloadTask task = getTask(url);
        boolean r = false;
        if (task != null) {
            if (!task.dead()) {
                task.stop();
            } else {
                task.removeAllDownloadListener();
            }
            r = true;
        }
        return r;
    }

    /**
     * Just for sqlite test
     *
     * @return Recorder
     */
    @Nullable
    @Deprecated
    public Recorder getRecorder() {
        return recorder;
    }

    /**
     * get local file with url.
     *
     * @param url url
     * @return if the file download completed,return local file,otherwise null.
     */
    @Nullable
    public File getFile(String url) {
        String md5 = CommonUtils.computeTaskMd5(url, downloadConfig.getSaveDirPath());
        boolean tempRecorder = recorder == null;
        Recorder r = recorder != null ? recorder : new SqliteRecorder(appContext);
        FileInfo fileInfo = null;
        try {
            fileInfo = r.get(md5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (tempRecorder) {
            try {
                r.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DLogger.e("getFile info:" + fileInfo);
        return fileInfo != null ? fileInfo.finished() : null;
    }

    @Nullable
    public String getSaveDirPath() {
        return downloadConfig != null ? downloadConfig.getSaveDirPath() : null;
    }

    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public void shutdown() {
        synchronized (TASK_MAP) {
            if (TASK_MAP.size() > 0) {
                Iterator<Map.Entry<String, DownloadTask>> iterator = TASK_MAP.entrySet()
                        .iterator();
                DownloadTask task;
                while (iterator.hasNext()) {
                    task = iterator.next().getValue();
                    task.stop();
                    task.removeAllDownloadListener();
                    DLogger.d("shutdown,remove task " + task.getUrl());
                    iterator.remove();
                }
            }
        }
        threadPoolExecutor.shutdownNow();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        initialized = false;
    }

    private void shrink() {
        synchronized (TASK_MAP) {
            if (TASK_MAP.size() > 0) {
                Iterator<Map.Entry<String, DownloadTask>> iterator = TASK_MAP.entrySet()
                        .iterator();
                DownloadTask task;
                while (iterator.hasNext()) {
                    task = iterator.next().getValue();
                    if (task == null) {
                        iterator.remove();
                        DLogger.d("task is null,remove task");
                    } else if (task.dead()) {
                        task.removeAllDownloadListener();
                        DLogger.d("task dead,remove task " + task.getUrl());
                        iterator.remove();
                    }
                }
            }
        }
    }

}
