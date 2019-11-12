package com.infinite.downloader;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infinite.downloader.config.DownloadStatus;
import com.infinite.downloader.config.FileInfo;
import com.infinite.downloader.task.DownloadTask;
import com.infinite.downloader.utils.DLogger;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-11-12 - 11:01
 * Description: Class description
 */
public class DownloadActivity extends AppCompatActivity {
    private TextView tvResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        tvResult = findViewById(R.id.tvResult);
        DLogger.enable();
        findViewById(R.id.btStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XDownload.get().addTask(Urls.URLS[0], downloadListener);
            }
        });
        findViewById(R.id.btEnd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadTask task = XDownload.get().getTask(Urls.URLS[0]);
                if (task != null) {
                    task.stop();
                }
            }
        });


        findViewById(R.id.btAllStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCount = 0;
                for (String image : Urls.IMAGES) {
                    XDownload.get().addTask(image, allDownloadListener);
                }
            }
        });
        findViewById(R.id.btAllEnd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String image : Urls.IMAGES) {
                    XDownload.get().removeTask(image);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadTask task = XDownload.get().getTask(Urls.URLS[0]);
        if (task != null) {
            task.removeDownloadListener(downloadListener);
        }
    }

    private int finishCount;
    private DownloadListener allDownloadListener = new DownloadListener() {
        @Override
        public void onDownloadStatus(final int status, @Nullable final FileInfo info) {
            if (status == DownloadStatus.FINISH) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finishCount++;
                        tvResult.setText("Finish count:" + finishCount);
                    }
                });

            }
        }
    };

    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onDownloadStatus(final int status, @Nullable final FileInfo info) {
            Log.d("Downloader", "onDownloadStatus:" + status);
            if (info != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setText("status:" + status + ",message:" + info.getMessage()
                                + "\nspeed:" + String.format("%.2f", info.getSpeed())
                                + "\ncurrent:" + info.getCurrentSize()
                                + "\ncost time:" + info.getCostTime()
                                + "\nbreakpoint download:" + info.isBreakpointDownload());
                    }
                });

            }
        }
    };
}
