package com.infinite.downloader.task;

import android.content.Context;
import android.os.SystemClock;

import androidx.annotation.Nullable;

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
        this.streamReader = new HttpStreamReader(this.taskConfig);
        this.downloadListenerSet = new HashSet<>(4);
    }

    @Override
    public void run() {
        updateStatus(DownloadStatus.PREPARE, this.fileInfo);
        startTime = SystemClock.elapsedRealtime();
        if (!isStopped()) {
            DLogger.d("task start running");
            this.fileInfo = this.recorder.get(requestUrlMd5);
            DLogger.d("get local record:" + this.fileInfo);
            if (fileInfo != null) {
                if (fileInfo.finished() != null) {
                    updateStatus(DownloadStatus.STARTED, this.fileInfo);
                    boolean checkRemote = taskConfig.isCheckRemote();
                    DLogger.d("file has downloaded already,need check remote?" + checkRemote);
                    if (!checkRemote) {
                        onTaskFinish(false);
                        DLogger.e("task end running，file downloaded already.");
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
            if (!taskConfig.tryCreateSaveDir()) {
                if (fileInfo != null) {
                    fileInfo.setMessage("please ensure the save dir exist!!!");
                }
                onTaskError("save dir not exist");
                DLogger.e("task end running，save dir not exist.");
                return;
            }
            long rangeOffset = this.fileInfo == null
                    || this.fileInfo.finished() != null
                    || !this.fileInfo.isSupportRange() ?
                    0 : this.fileInfo.getCurrentSize();
            FileInfo remoteInfo = streamReader.getFileInfo(requestUrl, rangeOffset);
            DLogger.d("received file info from remote server:" + remoteInfo);
            if (!isStopped()) {
                if (remoteInfo != null && remoteInfo.canDownload()) {
                    remoteInfo.setSaveDirPath(taskConfig.getSaveDirPath());
                    //if remote file has changed and rangeOffset not zero,we need request input stream with offset 0
                    if (remoteInfo.changed(fileInfo)) {
                        resetDownloadInfo();
                        if (rangeOffset > 0) {
                            //Has started download before.
                            DLogger.d("remote file has changed,need download file from offset 0");
                            remoteInfo = streamReader.getFileInfo(requestUrl, 0);
                            DLogger.d("received new file info from remote server:" + remoteInfo);
                            if (!isStopped()) {
                                if (remoteInfo != null && remoteInfo.canDownload()) {
                                    remoteInfo.setUrlMd5(requestUrlMd5);
                                    remoteInfo.setSaveDirPath(taskConfig.getSaveDirPath());
                                    fileInfo = remoteInfo;
                                    DLogger.d("download with new file info");
                                    download();
                                } else {
                                    onTaskError("get file info error:" + (remoteInfo != null ? remoteInfo.getMessage() : ""));
                                }
                            } else {
                                onTaskTerminal();
                            }
                        } else {
                            //Not started download before,or file changed after download finished,or not support range download.
                            DLogger.d("local file not start download or file changed after download finished," +
                                    "or not support range download,so start download with offset 0");
                            remoteInfo.setUrlMd5(requestUrlMd5);
                            remoteInfo.setSaveDirPath(taskConfig.getSaveDirPath());
                            fileInfo = remoteInfo;
                            download();
                        }
                    } else {
                        DLogger.d("remote file not change,continue download");
                        if (fileInfo.finished() != null) {
                            updateStatus(DownloadStatus.STARTED, this.fileInfo);
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
        if (downloadListenerSet.size() > 0) {
            DLogger.d("removeAllDownloadListener,url:" + requestUrl);
            downloadListenerSet.clear();
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

    private void close() {
        if (needCloseRecorder && recorder != null) {
            recorder.release();
            recorder = null;
            DLogger.d("task close recorder");
        }
        if (writer != null) {
            writer.close();
            writer = null;
            DLogger.d("task close,writer");
        }
        if (streamReader != null) {
            streamReader.close();
            streamReader = null;
            DLogger.d("task close,streamReader");
        }
    }

    private void download() {
        updateStatus(DownloadStatus.STARTED, fileInfo);
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        long count = 1;
        long currentSize = fileInfo.getCurrentSize();
        long startSize = currentSize;
        long start = currentSize;
        long costTime;
        long nowTime = 0;
        long notifyGrade = CommonUtils.notifyGrade(fileInfo.getFileSize());
        DLogger.d("start download file,start size:" + start + ",notify grade:" + notifyGrade);
        try {
            writer = new FileWriter(fileInfo.getFileSavePath(), currentSize);
            fileInfo.setBreakpointDownload(currentSize > 0);
            fileInfo.setStartTime(System.currentTimeMillis());
            updateStatus(DownloadStatus.DOWNLOADING, fileInfo);
            recorder.put(requestUrlMd5, fileInfo);
            long startTimestamp = SystemClock.elapsedRealtime();
            while ((length = streamReader.readInputStream(buffer)) != -1) {
                if (!isStopped()) {
                    currentSize = writer.saveFile(buffer, length);
                    nowTime = SystemClock.elapsedRealtime();
                    costTime = nowTime - startTime;
                    startTime = nowTime;
                    fileInfo.setCurrentSize(currentSize);
                    fileInfo.setMessage("file is downloading");
                    fileInfo.setCostTime(costTime + fileInfo.getCostTime());
                    fileInfo.setSpeed(CommonUtils.computeSpeed(currentSize - startSize,
                            nowTime - startTimestamp));
                    recorder.put(requestUrlMd5, fileInfo);
                    //256k
                    if (currentSize > ((count << notifyGrade) + start)) {
                        if (DLogger.isDebugEnable()) {
                            DLogger.d("file " + fileInfo.getFileName()
                                    + " is downloading,current size:" + currentSize
                                    + ",speed:" + fileInfo.getSpeed() + "KB/s");
                        }
                        updateStatus(DownloadStatus.DOWNLOADING, fileInfo);
                        count++;
                    }
                } else {
                    onTaskTerminal();
                    DLogger.d("task end running,file download stop,current size:" + currentSize);
                    return;
                }
            }
            if (DLogger.isDebugEnable()) {
                DLogger.d("file download finish,current size:" + currentSize
                        + "，average speed:" + CommonUtils.computeSpeed(currentSize - startSize,
                        nowTime - startTimestamp) + "KB/s");
            }
            fileInfo.setFileSize(currentSize);
            fileInfo.setFinishTime(System.currentTimeMillis());
            recorder.put(requestUrlMd5, fileInfo);
            onTaskFinish(true);
        } catch (Exception e) {
            e.printStackTrace();
            fileInfo.setMessage(e.getMessage());
            recorder.put(requestUrlMd5, fileInfo);
            onTaskError(e.getMessage());
        } finally {
            DLogger.d("on download completed");
            reset();
        }
    }

    private void onTaskFinish(boolean newComplete) {
        String info = "onTaskFinish,file is finish download already,is new complete?" + newComplete
                + ",file name:" + (fileInfo != null ? fileInfo.getFileName() : "");
        DLogger.d(info);
        File file = null;
        if (fileInfo != null) {
            fileInfo.setMessage(info);
            file = fileInfo.getLocalFile();
        }
        updateStatus(DownloadStatus.FINISH, fileInfo);
        reset();
        if (file != null && taskConfig != null && taskConfig.getDiskUsage() != null) {
            try {
                taskConfig.getDiskUsage().touch(file);
            } catch (IOException e) {
                e.printStackTrace();
                DLogger.e("shrink file error:" + e.getMessage());
            }
        }
    }

    private void onTaskError(String message) {
        if (fileInfo != null) {
            fileInfo.setMessage(message);
        }
        updateStatus(DownloadStatus.ERROR, fileInfo);
        DLogger.e("onTaskError,download file exception:" + message);
        reset();
    }

    private void onTaskTerminal() {
        if (fileInfo != null) {
            long costTime = SystemClock.elapsedRealtime() - startTime;
            fileInfo.setCostTime(fileInfo.getCostTime() + costTime);
            fileInfo.setMessage("task is terminal,stop download file");
            recorder.put(requestUrlMd5, fileInfo);
        }
        updateStatus(DownloadStatus.STOP, fileInfo);
        DLogger.d("onTaskTerminal");
        reset();
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

    private void updateStatus(int status, @Nullable FileInfo info) {
        int size = downloadListenerSet != null ? downloadListenerSet.size() : 0;
        if (status != DownloadStatus.DOWNLOADING) {
            DLogger.d("updateStatus listener,status:" + status + ",listener size:"
                    + size + ",is stopped:" + stopped + ",url:" + requestUrl);
        }
        if (size > 0) {
            for (DownloadListener listener : downloadListenerSet) {
                if (listener != null) {
                    listener.onDownloadStatus(status, info);
                }
            }
        }
    }

    private void reset() {
        close();
        stopped = true;
        removeAllDownloadListener();
    }

}
