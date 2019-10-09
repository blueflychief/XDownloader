package com.infinite.downloader.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.infinite.downloader.Config;
import com.infinite.downloader.DownloadListener;
import com.infinite.downloader.DownloadStatus;
import com.infinite.downloader.FileInfo;
import com.infinite.downloader.HttpStreamReader;
import com.infinite.downloader.StreamReader;
import com.infinite.downloader.recorder.Recorder;
import com.infinite.downloader.recorder.SqliteRecorder;
import com.infinite.downloader.utils.Logger;
import com.infinite.downloader.writer.FileWriter;
import com.infinite.downloader.writer.Writer;

import java.io.File;
import java.nio.MappedByteBuffer;
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
    private static final int BUFFER_SIZE = 8192;
    private Config config;
    private Recorder recorder;
    private Writer writer;
    private StreamReader streamReader;
    private List<DownloadListener> downloadListenerList;
    private DownloadListener downloadListener;
    private String requestUrl;
    private FileInfo fileInfo;

    public DownloadTask(Context context, String url) {
        this(context, url, null, null, null);
    }

    public DownloadTask(Context context, String url, Recorder recorder, Writer writer, Config config) {
        this.requestUrl = url;
        this.recorder = recorder == null ? new SqliteRecorder(context) : recorder;
        this.writer = writer == null ? new FileWriter() : writer;
        this.config = config == null ? Config.defaultConfig(context) : config;
        this.streamReader = new HttpStreamReader();
        this.downloadListenerList = new ArrayList<>(4);
        this.downloadListener = new UiListenerHandler(this.downloadListenerList);
        this.fileInfo = this.recorder.get(url);
        this.downloadListener.onDownloadStatus(DownloadStatus.PREPARE, this.fileInfo);
    }

    @Override
    public void run() {
        while (!isStopped()) {
            Logger.d(TAG, "task start running");
            FileInfo info = streamReader.getFileInfo(requestUrl, this.fileInfo == null ? 0 : this.fileInfo.getCurrentSize());
            if (!isStopped()) {
                if (info != null && info.canDownload()) {
                    String savePath = config.getSaveDirPath() + File.separator + info.getFileName();
                    info.setSavePath(savePath);
                    downloadListener.onDownloadStatus(DownloadStatus.PREPARED, info);
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len;
                    MappedByteBuffer saveBuffer;
                    if (info.changed(fileInfo)) {
                        recorder.put(requestUrl, info);
                        //文件发生改变，重新下载
                    } else {
                        //文件未改变，继续下载
                    }
                } else {
                    downloadListener.onDownloadStatus(DownloadStatus.ERROR, info);
                }
            }
        }
        Logger.e(TAG, "task end running");
    }

    public void addDownloadListener(DownloadListener listener) {
        downloadListenerList.add(listener);
    }

    public void removeDownloadListener(DownloadListener listener) {
        downloadListenerList.remove(listener);
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
