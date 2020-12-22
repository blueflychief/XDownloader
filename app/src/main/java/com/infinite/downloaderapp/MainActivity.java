package com.infinite.downloaderapp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.infinite.downloader.DownloadListener;
import com.infinite.downloader.config.FileInfo;
import com.infinite.downloader.task.DownloadTask;

import java.io.File;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends BaseActivity {

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private EditText etIndex;
    private static final String URL = "http:/www.baidu.com/";

    private Thread downloadThread;
    private TextView tvResult;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etIndex = findViewById(R.id.etIndex);
        tvResult = findViewById(R.id.tvResult);
        findViewById(R.id.btHttp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HttpActivity.class));
            }
        });
        findViewById(R.id.btAsyncTask).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, AsyncTaskActivity.class));
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        glideDownloadFile(Urls.FILE_DYNAMIC_GENERATE);
                    }
                }.start();
            }
        });
        findViewById(R.id.btDownloadPage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DownloadActivity.class));
            }
        });


        findViewById(R.id.btStartDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DownloadTask downloadTask = new DownloadTask(MainActivity.this, Urls.FILE_DYNAMIC_GENERATE);
//                downloadThread = new Thread(downloadTask);
//                downloadTask.addDownloadListener(downloadListener);
//                downloadThread.start();

                DownloadApp.getDownload().addTask(Urls.FILE_DYNAMIC_GENERATE);
            }
        });

        findViewById(R.id.btStopDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                downloadThread.interrupt();
                DownloadApp.getDownload().removeTask(Urls.FILE_DYNAMIC_GENERATE);
            }
        });

        findViewById(R.id.btStartAllDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executorService = Executors.newFixedThreadPool(10);
                DownloadTask downloadTask;
                for (String url : Urls.URLS) {
                    downloadTask = new DownloadTask(MainActivity.this, url);
                    downloadTask.addDownloadListener(downloadListener);
                    executorService.submit(downloadTask);
                }

            }
        });
        findViewById(R.id.btStopAllDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (executorService != null) {
                    executorService.shutdownNow();
                }
            }
        });


        findViewById(R.id.btMultiDownloader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MultiDownloaderActivity.class));
            }
        });

        if (!permissionsCheck(PERMISSIONS)) {
            permissionRequest(this, PERMISSIONS, 100);
        }
        handleSSLHandshake();
    }


    private void glideDownloadFile(String url) {
        try {
            File file = Glide.with(tvResult.getContext())
                    .load(url)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
            Log.e("glideDownloadFile", "file downloaded,file size:" + file.length());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (permissionsGranted(grantResults)) {

            }
        }
    }

    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onDownloadStatus(final int status, @Nullable final FileInfo info) {
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

    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("TLS");
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }
}
