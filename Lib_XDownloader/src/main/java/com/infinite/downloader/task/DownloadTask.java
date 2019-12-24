package com.infinite.downloader.task;

import android.content.Context;

import com.infinite.downloader.DownloadListener;
import com.infinite.downloader.config.Config;
import com.infinite.downloader.config.DownloadStatus;
import com.infinite.downloader.config.FileInfo;
import com.infinite.downloader.reader.HttpStreamReader;
import com.infinite.downloader.reader.StreamReader;
import com.infinite.downloader.recorder.Recorder;
import com.infinite.downloader.recorder.SqliteRecorder;
import com.infinite.downloader.utils.CommonUtils;
import com.infinite.downloader.utils.DLogger;
import com.infinite.downloader.writer.FileWriter;
import com.infinite.downloader.writer.Writer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-09-10 - 12:00
 * Description: Class description
 */
public class DownloadTask extends ComparableTask {
    private static final int BUFFER_SIZE = 8_192;
    private Config taskConfig;
    private Recorder recorder;
    private Writer writer;
    private StreamReader streamReader;
    private Set<DownloadListener> downloadListenerSet;
    private String requestUrlMd5;
    private String requestUrl;
    private FileInfo fileInfo;
    private long startTime;
    private boolean needCloseRecorder;

    public DownloadTask(Context context, String url) {
        this(context, url, null, null);
    }

    public DownloadTask(Context context, String url, Recorder recorder, Config config) {
        super();
        this.requestUrl = url;
        this.needCloseRecorder = recorder == null;
        this.recorder = recorder == null ? new SqliteRecorder(context) : recorder;
        this.taskConfig = config == null ? Config.defaultConfig(context) : config;
        this.requestUrlMd5 = CommonUtils.computeTaskMd5(url, this.taskConfig.getSaveDirPath());
        this.streamReader = new HttpStreamReader();
        this.downloadListenerSet = new HashSet<>(4);
        this.fileInfo = this.recorder.get(requestUrlMd5);
        DLogger.d("get local record:" + this.fileInfo);
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        updateStatus(DownloadStatus.PREPARE, this.fileInfo);
        if (!isStopped()) {
            DLogger.d("task start running");
            if (fileInfo != null) {
                if (fileInfo.finished()) {
                    boolean checkRemote = taskConfig.isCheckRemote();
                    DLogger.d("file has downloaded already,need check remote?" + checkRemote);
                    if (!checkRemote) {
                        onTaskFinish(false);
                        return;
                    }
                } else {
                    boolean fileAvailable = fileInfo.localFileAvailable();
                    boolean supportRange = fileInfo.isSupportRange();
                    DLogger.d("local save file available?" + fileAvailable + "，supportRange：" + supportRange);
                    if (!fileAvailable || !supportRange) {
                        DLogger.d("local file is not correct or not support range download," +
                                "need delete local file,fileAvailable:"
                                + fileAvailable + ",supportRange:" + supportRange);
                        resetDownloadInfo();
                    }
                }
            }
            DLogger.d("start get file info from remote server");
            FileInfo remoteInfo = streamReader.getFileInfo(requestUrl,
                    this.fileInfo == null || this.fileInfo.finished() || !this.fileInfo.isSupportRange()
                            ? 0 : this.fileInfo.getCurrentSize());
            DLogger.d("get file info from remote server:" + remoteInfo);
            if (!isStopped()) {
                if (remoteInfo != null && remoteInfo.canDownload()) {
                    remoteInfo.setUrlMd5(requestUrlMd5);
                    remoteInfo.setSaveDirPath(taskConfig.getSaveDirPath());
                    if (!taskConfig.tryCreateSaveDir()) {
                        if (fileInfo != null) {
                            fileInfo.setMessage("please ensure the save dir exist!!!");
                        }
                        onTaskError("save dir not exist");
                        return;
                    }
                    if (remoteInfo.changed(fileInfo)) {
                        DLogger.d("remote file has changed or local file not exist,need download file");
                        fileInfo = remoteInfo;
                        resetDownloadInfo();
                        download();
                    } else {
                        DLogger.d("remote file not change,continue download");
                        if (fileInfo.finished()) {
                            onTaskFinish(false);
                        } else {
                            DLogger.d("file download incomplete,continue download");
                            download();
                        }
                    }
                } else {
                    onTaskError("get file info error:" + (remoteInfo != null ? remoteInfo.getMessage() : ""));
                }
            } else {
                onTaskTerminal();
            }
        } else {
            onTaskTerminal();
        }
        DLogger.e("task end running");
    }

    public String getUrlMd5() {
        return requestUrlMd5;
    }

    public String getUrl() {
        return requestUrl;
    }

    public void addDownloadListener(DownloadListener listener) {
        if (!downloadListenerSet.contains(listener)) {
            downloadListenerSet.add(listener);
        }
    }

    public void removeDownloadListener(DownloadListener listener) {
        downloadListenerSet.remove(listener);
    }

    public void removeAllDownloadListener() {
        downloadListenerSet.clear();
    }

    @Override
    public void stop() {
        super.stop();
    }

    private void close() {
        DLogger.d("task close");
        closeRecorder();
        if (writer != null) {
            writer.close();
        }
        if (streamReader != null) {
            streamReader.close();
        }
    }

    private void closeRecorder() {
        DLogger.d("task close recorder");
        if (needCloseRecorder && recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    private void download() {
        updateStatus(DownloadStatus.PREPARED, fileInfo);
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        long count = 1;
        long currentSize = fileInfo.getCurrentSize();
        long start = currentSize;
        long costTime;
        long nowTime;
        DLogger.d("start download file,start size:" + start);
        try {
            writer = new FileWriter(fileInfo.getFileSavePath(), currentSize);
            fileInfo.setBreakpointDownload(currentSize > 0);
            updateStatus(DownloadStatus.DOWNLOADING, fileInfo);
            while ((length = streamReader.readInputStream(buffer)) != -1) {
                if (!isStopped()) {
                    currentSize = writer.saveFile(buffer, length);
                    nowTime = System.currentTimeMillis();
                    costTime = nowTime - startTime;
                    startTime = nowTime;
                    fileInfo.setCurrentSize(currentSize);
                    fileInfo.setMessage("file is downloading");
                    fileInfo.setCostTime(costTime + fileInfo.getCostTime());
                    fileInfo.setSpeed(computeSpeed(length, costTime));
                    recorder.put(requestUrlMd5, fileInfo);
                    //128k
                    if (currentSize > ((count << 17) + start)) {
                        DLogger.d("file downloading,current size:" + currentSize);
                        updateStatus(DownloadStatus.DOWNLOADING, fileInfo);
                        count++;
                    }
                } else {
                    DLogger.d("file download stop,current size:" + currentSize);
                    onTaskTerminal();
                }
            }
            DLogger.d("file download finish,current size:" + currentSize);
            recorder.put(requestUrlMd5, fileInfo);
            onTaskFinish(true);
        } catch (IOException e) {
            e.printStackTrace();
            fileInfo.setMessage(e.getMessage());
            recorder.put(requestUrlMd5, fileInfo);
            onTaskError(e.getMessage());
        } finally {
            close();
            stopped = true;
            downloadListenerSet.clear();
        }
    }

    private void onTaskFinish(boolean newComplete) {
        String info = "file is finish download already,is new complete?" + newComplete
                + ",file name:" + (fileInfo != null ? fileInfo.getFileName() : "");
        DLogger.d(info);
        fileInfo.setMessage(info);
        updateStatus(DownloadStatus.FINISH, fileInfo);
        File file = fileInfo.getLocalFile();
        if (taskConfig.getDiskUsage() != null && file != null) {
            try {
                taskConfig.getDiskUsage().touch(file);
            } catch (IOException e) {
                e.printStackTrace();
                DLogger.e("shrink file error:" + e.getMessage());
            }
        }
        close();
        stopped = true;
        downloadListenerSet.clear();
    }

    private void onTaskError(String message) {
        DLogger.e("download file exception:" + message);
        updateStatus(DownloadStatus.ERROR, fileInfo);
        stopped = true;
        downloadListenerSet.clear();
    }

    private void onTaskTerminal() {
        if (fileInfo != null) {
            long costTime = System.currentTimeMillis() - startTime;
            fileInfo.setCostTime(fileInfo.getCostTime() + costTime);
            fileInfo.setMessage("task is terminal,stop download file");
            recorder.put(requestUrlMd5, fileInfo);
        }
        updateStatus(DownloadStatus.STOP, fileInfo);
        close();
        stopped = true;
        downloadListenerSet.clear();
    }

    private void resetDownloadInfo() {
        if (fileInfo != null) {
            fileInfo.setCostTime(0);
            fileInfo.setCurrentSize(0);
            try {
                CommonUtils.deleteFile(fileInfo.getFileSavePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            recorder.put(requestUrlMd5, fileInfo);
        }
    }

    private void updateStatus(int status, FileInfo info) {
        if (downloadListenerSet != null && !downloadListenerSet.isEmpty()) {
            for (DownloadListener listener : downloadListenerSet) {
                if (listener != null) {
                    listener.onDownloadStatus(status, info);
                }
            }
        }
    }

    private float computeSpeed(long length, long time) {
        //KB/s
        return length / 1024f / (time / 1000f);
    }
}
