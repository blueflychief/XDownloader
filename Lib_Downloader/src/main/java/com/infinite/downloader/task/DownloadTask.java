package com.infinite.downloader.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.infinite.downloader.DownloadListener;
import com.infinite.downloader.config.Config;
import com.infinite.downloader.config.DownloadStatus;
import com.infinite.downloader.config.FileInfo;
import com.infinite.downloader.reader.HttpStreamReader;
import com.infinite.downloader.reader.StreamReader;
import com.infinite.downloader.recorder.Recorder;
import com.infinite.downloader.recorder.SqliteRecorder;
import com.infinite.downloader.utils.CommonUtils;
import com.infinite.downloader.utils.Logger;
import com.infinite.downloader.writer.FileWriter;
import com.infinite.downloader.writer.Writer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private List<DownloadListener> downloadListenerList;
    private DownloadListener downloadListener;
    private String requestUrl;
    private FileInfo fileInfo;
    private long startTime;

    public DownloadTask(Context context, String url) {
        this(context, url, null, null);
    }

    public DownloadTask(Context context, String url, Recorder recorder, Config config) {
        this.requestUrl = url;
        this.recorder = recorder == null ? new SqliteRecorder(context) : recorder;
        this.config = config == null ? Config.defaultConfig(context) : config;
        this.streamReader = new HttpStreamReader();
        this.downloadListenerList = new ArrayList<>(4);
        this.downloadListener = new UiListenerHandler(this.downloadListenerList);
        this.fileInfo = this.recorder.get(url);
        this.downloadListener.onDownloadStatus(DownloadStatus.PREPARE, this.fileInfo);
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        if (!isStopped()) {
            Logger.d(TAG, "task start running");
            if (fileInfo != null) {
                if (fileInfo.finished()) {
                    boolean checkRemote = config.isCheckRemote();
                    Logger.d("file has downloaded already,need check remote?" + checkRemote);
                    if (!checkRemote) {
                        downloadListener.onDownloadStatus(DownloadStatus.FINISH, fileInfo);
                        return;
                    }
                } else {
                    boolean fileAvailable = fileInfo.localFileAvailable();
                    Logger.d("local save file available?" + fileAvailable);
                    boolean supportRange = fileInfo.isSupportRange();
                    Logger.d("remote server support range?" + supportRange);
                    if (!fileAvailable || !supportRange) {
                        Logger.d("need delete local file");
                        resetDownloadInfo();
                    }
                }
            }
            Logger.d("start get file info from remote server");
            FileInfo info = streamReader.getFileInfo(requestUrl,
                    this.fileInfo == null
                            || this.fileInfo.finished()
                            || !this.fileInfo.isSupportRange() ?
                            0 : this.fileInfo.getCurrentSize());
            Logger.d("get file info from remote server:" + info);
            if (!isStopped()) {
                if (info != null && info.canDownload()) {
                    if (!config.existSaveDir()) {
                        Logger.d("save dir not exist");
                        fileInfo.setMessage("please ensure the save dir exist!!!");
                        downloadListener.onDownloadStatus(DownloadStatus.ERROR, fileInfo);
                        return;
                    }
                    String savePath = config.getSaveDirPath() + File.separator + info.getFileName();
                    info.setSavePath(savePath);
                    if (info.changed(fileInfo)) {
                        Logger.d("remote file has changed or local file not exist,need download file");
                        fileInfo = info;
                        resetDownloadInfo();
                        download();
                    } else {
                        Logger.d("remote file not change,continue download");
                        if (fileInfo.finished()) {
                            Logger.d("file has downloaded already");
                            fileInfo.setMessage("file is finish download already");
                            downloadListener.onDownloadStatus(DownloadStatus.FINISH, fileInfo);
                        } else {
                            Logger.d("file download incomplete,continue download");
                            download();
                        }
                    }
                } else {
                    Logger.d("get file info error:" + (info != null ? info.getMessage() : ""));
                    downloadListener.onDownloadStatus(DownloadStatus.ERROR, info);
                }
            } else {
                onTaskTerminal();
            }
        } else {
            onTaskTerminal();
        }
        Logger.e(TAG, "task end running");
    }

    private void download() {
        downloadListener.onDownloadStatus(DownloadStatus.PREPARED, fileInfo);
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        long count = 1;
        long currentSize = fileInfo.getCurrentSize();
        long start = currentSize;
        long costTime;
        long nowTime;
        Logger.d(TAG, "start download file,start size:" + start);
        try {
            writer = new FileWriter(fileInfo.getSavePath(), currentSize);
            fileInfo.setBreakpointDownload(currentSize > 0);
            downloadListener.onDownloadStatus(DownloadStatus.DOWNLOADING, fileInfo);
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
                    recorder.put(fileInfo.getRequestUrl(), fileInfo);
                    //128k
                    if (currentSize > ((count << 17) + start)) {
                        Logger.d("file downloading,current size:" + currentSize);
                        downloadListener.onDownloadStatus(DownloadStatus.DOWNLOADING, fileInfo);
                        count++;
                    }
                } else {
                    Logger.d("file download stop,current size:" + currentSize);
                    onTaskTerminal();
                }
            }
            Logger.d("file download finish,current size:" + currentSize);
            fileInfo.setMessage("file is finish download");
            recorder.put(fileInfo.getRequestUrl(), fileInfo);
            downloadListener.onDownloadStatus(DownloadStatus.FINISH, fileInfo);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e("download file exception:" + e.getMessage());
            fileInfo.setMessage(e.getMessage());
            recorder.put(fileInfo.getRequestUrl(), fileInfo);
            downloadListener.onDownloadStatus(DownloadStatus.ERROR, fileInfo);
        } finally {
            close();
        }
    }

    public void addDownloadListener(DownloadListener listener) {
        downloadListenerList.add(listener);
    }

    public void removeDownloadListener(DownloadListener listener) {
        downloadListenerList.remove(listener);
    }

    public void close() {
        if (writer != null) {
            writer.close();
        }
        if (streamReader != null) {
            streamReader.close();
        }
    }

    private float computeSpeed(long length, long time) {
        //KB/s
        return length / 1024f / (time / 1000f);
    }

    private void resetDownloadInfo() {
        fileInfo.setCostTime(0);
        fileInfo.setCurrentSize(0);
        CommonUtils.deleteFile(fileInfo.getSavePath());
        recorder.put(requestUrl, fileInfo);
    }

    private void onTaskTerminal() {
        if (fileInfo != null) {
            long costTime = System.currentTimeMillis() - startTime;
            fileInfo.setCostTime(fileInfo.getCostTime() + costTime);
            fileInfo.setMessage("task is terminal,stop download file");
            recorder.put(fileInfo.getRequestUrl(), fileInfo);
        }
        downloadListener.onDownloadStatus(DownloadStatus.STOP, fileInfo);
        close();
    }

    private static class UiListenerHandler extends Handler implements DownloadListener {
        private List<DownloadListener> downloadListenerList;

        public UiListenerHandler(List<DownloadListener> downloadListenerList) {
            super(Looper.getMainLooper());
            this.downloadListenerList = downloadListenerList;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int status = msg.arg1;
            FileInfo fileInfo = null;
            if (msg.obj instanceof FileInfo) {
                fileInfo = (FileInfo) msg.obj;
            }
            if (downloadListenerList != null && !downloadListenerList.isEmpty()) {
                for (DownloadListener listener : downloadListenerList) {
                    if (listener != null) {
                        listener.onDownloadStatus(status, fileInfo);
                    }
                }
            }
        }

        @Override
        public void onDownloadStatus(int status, FileInfo info) {
            Message message = Message.obtain(this);
            message.arg1 = status;
            message.obj = info;
            sendMessage(message);
        }
    }
}
