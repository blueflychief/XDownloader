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
    private static final String TAG = "DownloadTask";
    private static final int BUFFER_SIZE = 8_192;
    private Config config;
    private Recorder recorder;
    private Writer writer;
    private StreamReader streamReader;
    private Set<DownloadListener> downloadListenerSet;
    private String requestUrlMd5;
    private String requestUrl;
    private FileInfo fileInfo;
    private long startTime;

    public DownloadTask(Context context, String url) {
        this(context, url, null, null);
    }

    public DownloadTask(Context context, String url, Recorder recorder, Config config) {
        super();
        this.requestUrl = url;
        this.requestUrlMd5 = CommonUtils.computeMd5(url);
        this.recorder = recorder == null ? new SqliteRecorder(context) : recorder;
        this.config = config == null ? Config.defaultConfig(context) : config;
        this.streamReader = new HttpStreamReader();
        this.downloadListenerSet = new HashSet<>(4);
        this.fileInfo = this.recorder.get(requestUrlMd5);
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        updateStatus(DownloadStatus.PREPARE, this.fileInfo);
        if (!isStopped()) {
            DLogger.d(TAG, "task start running");
            if (fileInfo != null) {
                if (fileInfo.finished()) {
                    boolean checkRemote = config.isCheckRemote();
                    DLogger.d("file has downloaded already,need check remote?" + checkRemote);
                    if (!checkRemote) {
                        onFinishDownload();
                        return;
                    }
                } else {
                    boolean fileAvailable = fileInfo.localFileAvailable();
                    boolean supportRange = fileInfo.isSupportRange();
                    DLogger.d("local save file available?" + fileAvailable + "，supportRange：" + supportRange);
                    if (!fileAvailable || !supportRange) {
                        DLogger.d("need delete local file");
                        resetDownloadInfo();
                    }
                }
            }
            DLogger.d("start get file info from remote server");
            FileInfo info = streamReader.getFileInfo(requestUrl,
                    this.fileInfo == null
                            || this.fileInfo.finished()
                            || !this.fileInfo.isSupportRange() ?
                            0 : this.fileInfo.getCurrentSize());
            DLogger.d("get file info from remote server:" + info);
            if (!isStopped()) {
                if (info != null && info.canDownload()) {
                    if (!config.existSaveDir()) {
                        DLogger.d("save dir not exist");
                        fileInfo.setMessage("please ensure the save dir exist!!!");
                        updateStatus(DownloadStatus.ERROR, fileInfo);
                        stopped = true;
                        return;
                    }
                    String savePath = config.getSaveDirPath() + File.separator + info.getFileName();
                    info.setSavePath(savePath);
                    if (info.changed(fileInfo)) {
                        DLogger.d("remote file has changed or local file not exist,need download file");
                        fileInfo = info;
                        resetDownloadInfo();
                        download();
                    } else {
                        DLogger.d("remote file not change,continue download");
                        if (fileInfo.finished()) {
                            DLogger.d("file has downloaded already");
                            onFinishDownload();
                        } else {
                            DLogger.d("file download incomplete,continue download");
                            download();
                        }
                    }
                } else {
                    DLogger.d("get file info error:" + (info != null ? info.getMessage() : ""));
                    updateStatus(DownloadStatus.ERROR, info);
                    stopped = true;
                    downloadListenerSet.clear();
                }
            } else {
                onTaskTerminal();
            }
        } else {
            onTaskTerminal();
        }
        DLogger.e(TAG, "task end running");
    }

    public String getUrlMd5() {
        return requestUrlMd5;
    }

    public String getUrl() {
        return requestUrl;
    }

    public void addDownloadListener(DownloadListener listener) {
        downloadListenerSet.add(listener);
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
        removeAllDownloadListener();
    }

    public void close() {
        if (writer != null) {
            writer.close();
        }
        if (streamReader != null) {
            streamReader.close();
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
        DLogger.d(TAG, "start download file,start size:" + start);
        try {
            writer = new FileWriter(fileInfo.getSavePath(), currentSize);
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
                    recorder.put(fileInfo.getUrlMd5(), fileInfo);
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
            recorder.put(fileInfo.getUrlMd5(), fileInfo);
            onFinishDownload();
        } catch (IOException e) {
            e.printStackTrace();
            DLogger.e("download file exception:" + e.getMessage());
            fileInfo.setMessage(e.getMessage());
            recorder.put(fileInfo.getUrlMd5(), fileInfo);
            updateStatus(DownloadStatus.ERROR, fileInfo);
        } finally {
            close();
        }
    }

    private void onFinishDownload() {
        String info = "file is finish download already:" + (fileInfo != null ? fileInfo.getFileName() : "");
        DLogger.d(info);
        fileInfo.setMessage(info);
        updateStatus(DownloadStatus.FINISH, fileInfo);
        stopped = true;
        downloadListenerSet.clear();
        recorder.shrink();
        File file = fileInfo.getLocalFile();
        if (config.getDiskUsage() != null && file != null) {
            try {
                config.getDiskUsage().touch(file);
            } catch (IOException e) {
                e.printStackTrace();
                DLogger.e("shrink file error:" + e.getMessage());
            }
        }
    }

    private void resetDownloadInfo() {
        fileInfo.setCostTime(0);
        fileInfo.setCurrentSize(0);
        CommonUtils.deleteFile(fileInfo.getSavePath());
        recorder.put(requestUrlMd5, fileInfo);
    }

    private void onTaskTerminal() {
        if (fileInfo != null) {
            long costTime = System.currentTimeMillis() - startTime;
            fileInfo.setCostTime(fileInfo.getCostTime() + costTime);
            fileInfo.setMessage("task is terminal,stop download file");
            recorder.put(fileInfo.getUrlMd5(), fileInfo);
        }
        updateStatus(DownloadStatus.STOP, fileInfo);
        close();
        stopped = true;
        downloadListenerSet.clear();
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
