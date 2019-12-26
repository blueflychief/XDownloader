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
    private Recorder recorder;
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
            recorder = new SqliteRecorder(appContext);
            int cpuCount = Runtime.getRuntime().availableProcessors();
            threadPoolExecutor = new ThreadPoolExecutor(
                    cpuCount + 1,
                    cpuCount << 2 + 1,
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
            recorder.shrink();
            initialized = true;
            DLogger.d("XDownload is initialized");
        } else {
            DLogger.d("XDownload has initialized already!");
        }
    }

    public DownloadTask addTask(String url) {
        return addTask(url, null);
    }

    public DownloadTask addTask(String url, DownloadListener listener) {
        DownloadTask task = null;
        String md5 = CommonUtils.computeTaskMd5(url, downloadConfig.getSaveDirPath());
        if (!TextUtils.isEmpty(md5)) {
            synchronized (TASK_MAP) {
                DownloadTask t = TASK_MAP.get(md5);
                if (t == null || t.dead()) {
                    task = new DownloadTask(appContext, url, recorder, downloadConfig);
                    TASK_MAP.put(md5, task);
                    threadPoolExecutor.submit(task);
                    DLogger.d("add a new task:" + task.getUrlMd5());
                } else {
                    task = t;
                    DLogger.d("task:" + task.getUrlMd5() + " has exist already");
                }
            }
            if (listener != null) {
                task.addDownloadListener(listener);
            }
        } else {
            DLogger.e("add task fail,md5 is " + md5);
        }
        shrink();
        return task;
    }

    @Nullable
    public DownloadTask getTask(String url) {
        if (!TextUtils.isEmpty(url)) {
            String md5 = CommonUtils.computeTaskMd5(url, downloadConfig.getSaveDirPath());
            synchronized (TASK_MAP) {
                DownloadTask task = TASK_MAP.get(md5);
                return task != null && !task.dead() ? task : null;
            }
        }
        return null;
    }

    public boolean removeTask(String url) {
        DownloadTask task = getTask(url);
        boolean r = false;
        if (task != null) {
            task.stop();
            task.removeAllDownloadListener();
            r = true;
        }
        shrink();
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

    @Nullable
    public File getFile(String url) {
        String md5 = CommonUtils.computeTaskMd5(url, downloadConfig.getSaveDirPath());
        boolean tempRecorder = recorder == null;
        Recorder r = recorder != null ? recorder : new SqliteRecorder(appContext);
        FileInfo fileInfo = r.get(md5);
        if (tempRecorder) {
            r.release();
        }
        DLogger.e("getFile info:" + fileInfo);
        return fileInfo != null && fileInfo.finished() ? fileInfo.getLocalFile() : null;
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
