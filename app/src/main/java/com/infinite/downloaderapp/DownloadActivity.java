package com.infinite.downloaderapp;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infinite.downloader.DownloadListener;
import com.infinite.downloader.XDownload;
import com.infinite.downloader.config.Config;
import com.infinite.downloader.config.DownloadStatus;
import com.infinite.downloader.config.FileInfo;
import com.infinite.downloader.lru.TotalSizeLruDiskUsage;
import com.infinite.downloader.task.DownloadTask;
import com.infinite.downloader.utils.DLogger;

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
    private EditText etFinishTime;
    private int index;
    private XDownload xDownload1;
    private XDownload xDownload2;
    private XDownload xDownload3;
    private XDownload xDownload4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        tvResult = findViewById(R.id.tvResult);
        etId = findViewById(R.id.etId);
        etFinishTime = findViewById(R.id.etFinishTime);
        findViewById(R.id.btStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sbMessage.setLength(0);
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
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        for (String image : Urls.JD_DY) {
                            DownloadApp.getDownload().addTask(image, allDownloadListener);
//                            try {
//                                Thread.sleep(200);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }
                }.start();

            }
        });
        findViewById(R.id.btAllEnd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String image : Urls.JD_DY) {
                    DownloadApp.getDownload().removeTask(image);
                }
            }
        });

        findViewById(R.id.btGetFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (String image : Urls.IMAGES) {
                    File file = xDownload1.getFile(image);
                    DLogger.e("file cached ?" + (file != null));
                }

                File file = xDownload1.getFile(Urls.IMAGES[0]);
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


        findViewById(R.id.btDeleteByFinishTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timestamp = etFinishTime.getEditableText().toString();
                if (!TextUtils.isEmpty(timestamp)) {
                    long startTime = SystemClock.elapsedRealtime();
                    DownloadApp.getDownload().deleteByFinishTime(0L, parseLong(timestamp), true);
                    DLogger.d("delete file finish,cost time " + (SystemClock.elapsedRealtime() - startTime));
                }
            }
        });
        etFinishTime.setText(String.valueOf(System.currentTimeMillis()));


        findViewById(R.id.btMultiStart).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        xDownload1 = createDownload("cache_dir1");
                        loopDownload(xDownload1);
                    }
                }.start();

                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        xDownload3 = createDownload("cache_dir3");
                        loopDownload(xDownload3);
                    }
                }.start();
                xDownload2 = createDownload("cache_dir2");
                xDownload4 = createDownload("cache_dir4");
                loopDownload(xDownload2);
                loopDownload(xDownload4);
            }

        });

        findViewById(R.id.btMultiEnd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDownload(xDownload1);
                stopDownload(xDownload2);
                stopDownload(xDownload3);
                stopDownload(xDownload4);
            }
        });
    }


    private XDownload createDownload(String savePath) {
        Config config = Config.defaultConfig(DownloadActivity.this);
        File saveDir = new File(getExternalCacheDir().getAbsolutePath(), savePath);
        if (!saveDir.isDirectory() || !saveDir.exists()) {
            saveDir.mkdir();
        }
        config.setSaveDirPath(saveDir.getAbsolutePath());
        config.setDiskUsage(new TotalSizeLruDiskUsage(400 * 1024 * 1024));
        XDownload xDownload = new XDownload(DownloadActivity.this, config);
        return xDownload;
    }


    private void loopDownload(XDownload xDownload) {
        for (String image : Urls.IMAGES) {
            xDownload.addTask(image);
        }
    }

    private void stopDownload(XDownload xDownload) {
        if (xDownload != null) {
            for (String image : Urls.IMAGES) {
                xDownload.removeTask(image);
            }
        }
    }

    private static long parseLong(String time) {
        try {
            return Long.parseLong(time);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
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
                                sbMessage.append("task started\n");
                                break;
                            case DownloadStatus.PREPARE:
                                sbMessage.append("task prepare\n");
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
//                    tvResult.setText(sbMessage.toString());
                    tvResult.setText("task finish, count:" + finishCount);
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
