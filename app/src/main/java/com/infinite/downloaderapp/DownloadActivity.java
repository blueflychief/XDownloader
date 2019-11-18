package com.infinite.downloaderapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infinite.downloader.DownloadListener;
import com.infinite.downloader.XDownload;
import com.infinite.downloader.config.DownloadStatus;
import com.infinite.downloader.config.FileInfo;
import com.infinite.downloader.task.DownloadTask;
import com.infinite.downloader.utils.DLogger;

import java.io.File;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-11-12 - 11:01
 * Description: Class description
 */
public class DownloadActivity extends AppCompatActivity {
    private TextView tvResult;
    private EditText etId;
    private int index;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        tvResult = findViewById(R.id.tvResult);
        etId = findViewById(R.id.etId);
        findViewById(R.id.btStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XDownload.get().addTask(Urls.IMAGES[index % (Urls.IMAGES.length - 1)], downloadListener);
                index++;
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

        findViewById(R.id.btGetFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File file = XDownload.get().getFile(Urls.IMAGES[0]);
                String fileInfo = "file name:";
                if (file != null) {
                    fileInfo += (file.getName() + ",length:" + file.length());
                }
                tvResult.setText(fileInfo);
            }
        });


        findViewById(R.id.btAddRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a.a.a.d.a recorder = XDownload.get().getRecorder();
                FileInfo fileInfo;
                for (int i = 0; i < 30_000; i++) {
                    fileInfo = new FileInfo();
                    fileInfo.setRequestUrl(Urls.IMAGES[0] + index);
                    fileInfo.setDownloadUrl(Urls.IMAGES[0] + index);
                    fileInfo.setContentType("jpg");
                    fileInfo.setFileSize(100);
                    fileInfo.setCostTime(20);
                    fileInfo.setSupportRange(true);
                    fileInfo.setFileName("file_name");
                    fileInfo.setSavePath("/save/path");
                    index++;
                    recorder.a(fileInfo.getUrlMd5(), fileInfo);
                }
            }
        });

        findViewById(R.id.btQueryRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = Urls.IMAGES[0] + etId.getText().toString();
                a.a.a.d.a recorder = XDownload.get().getRecorder();
                String md5 = a.a.a.f.a.a(id);
                FileInfo fileInfo = recorder.a(md5);
                DLogger.d("query result:" + fileInfo);
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
