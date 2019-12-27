package com.infinite.downloaderapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infinite.downloader.DownloadListener;
import com.infinite.downloader.config.DownloadStatus;
import com.infinite.downloader.config.FileInfo;
import com.infinite.downloader.task.DownloadTask;

import java.io.File;

//import com.infinite.downloader.recorder.Recorder;
//import com.infinite.downloader.utils.CommonUtils;

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
                DownloadApp.getDownload().addTask(Urls.IMAGES[index % (Urls.IMAGES.length - 1)], downloadListener);
                index++;
            }
        });
        findViewById(R.id.btEnd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadTask task = DownloadApp.getDownload().getTask(Urls.URLS[0]);
                if (task != null) {
                    task.stop();
                }
            }
        });


        findViewById(R.id.btAllStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCount = 0;
                sbMessage.setLength(0);
                for (String image : Urls.IMAGES) {
                    DownloadApp.getDownload().addTask(image, allDownloadListener);
                }
            }
        });
        findViewById(R.id.btAllEnd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String image : Urls.IMAGES) {
                    DownloadApp.getDownload().removeTask(image);
                }
            }
        });

        findViewById(R.id.btGetFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File file = DownloadApp.getDownload().getFile(Urls.IMAGES[0]);
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
//                Recorder recorder = DownloadApp.getDownload().getRecorder();
//                FileInfo fileInfo;
//                for (int i = 0; i < 30_000; i++) {
//                    fileInfo = new FileInfo();
//                    fileInfo.setRequestUrl(Urls.IMAGES[0] + index);
//                    fileInfo.setDownloadUrl(Urls.IMAGES[0] + index);
//                    fileInfo.setContentType("jpg");
//                    fileInfo.setFileSize(100);
//                    fileInfo.setCostTime(20);
//                    fileInfo.setSupportRange(true);
//                    fileInfo.setFileName("file_name");
//                    fileInfo.setSavePath("/save/path");
//                    index++;
//                    recorder.put(fileInfo.getUrlMd5(), fileInfo);
//                }
            }
        });

        findViewById(R.id.btQueryRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String id = Urls.IMAGES[0] + etId.getText().toString();
//                Recorder recorder = DownloadApp.getDownload().getRecorder();
//                String md5 = CommonUtils.computeMd5(id);
//                FileInfo fileInfo = recorder.get(md5);
//                DLogger.d("query result:" + fileInfo);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadTask task = DownloadApp.getDownload().getTask(Urls.URLS[0]);
        if (task != null) {
            task.removeDownloadListener(downloadListener);
        }
    }

    StringBuilder sbMessage = new StringBuilder();
    private volatile int finishCount;
    private boolean downloading = false;
    private DownloadListener allDownloadListener = new DownloadListener() {
        @Override
        public void onDownloadStatus(final int status, @Nullable final FileInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Urls.IMAGES.length == 1) {
                        switch (status) {
                            case DownloadStatus.STARTED:
                                downloading = false;
                                sbMessage.setLength(0);
                                sbMessage.append("task started\n");
                                break;
                            case DownloadStatus.PREPARE:
                                sbMessage.append("task prepare\n");
                                break;
                            case DownloadStatus.PREPARED:
                                sbMessage.append("task prepared\n");
                                break;
                            case DownloadStatus.DOWNLOADING:
                                if (!downloading) {
                                    downloading = true;
                                    sbMessage.append("task downloading\n");
                                }
                                break;
                            case DownloadStatus.STOP:
                                sbMessage.append("task stop\n");
                                break;
                            case DownloadStatus.ERROR:
                                sbMessage.append("task error\n");
                                break;
                            case DownloadStatus.FINISH:
                                finishCount++;
                                sbMessage.append("task finish, count:").append(finishCount).append("\n");
                                break;
                            default:
                                break;
                        }
                    } else {
                        if (status == DownloadStatus.FINISH) {
                            finishCount++;
                            sbMessage.append("task finish, count:").append(finishCount).append("\n");
                        }
                    }
                    tvResult.setText(sbMessage.toString());
                }
            });
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
