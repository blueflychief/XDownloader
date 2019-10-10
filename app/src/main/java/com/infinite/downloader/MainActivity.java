package com.infinite.downloader;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infinite.downloader.recorder.Recorder;
import com.infinite.downloader.recorder.SqliteRecorder;
import com.infinite.downloader.task.DownloadTask;
import com.infinite.downloader.utils.CommonUtils;
import com.infinite.downloader.utils.Logger;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btStart;
    private Button btStop;
    private Button btSleep;
    private Button btResume;
    private DownloadTask demoTask;
    private Thread thread;
    private EditText etIndex;
    private Recorder recorder;
    private int index;
    private static final String URL = "http:/www.baidu.com/";

    private DownloadTask downloadTask;
    private Thread downloadThread;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btStart = findViewById(R.id.btStart);
        btStop = findViewById(R.id.btStop);
        btSleep = findViewById(R.id.btSleep);
        btResume = findViewById(R.id.btResume);
        etIndex = findViewById(R.id.etIndex);
        tvResult = findViewById(R.id.tvResult);
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thread = new Thread(demoTask);
                thread.start();
            }
        });

        btSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//                    thread.wait(5000L);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    Log.e("MainActivity", e.getMessage());
//                }
            }
        });
        btResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thread.interrupt();
            }
        });

        recorder = new SqliteRecorder(this);

        findViewById(R.id.btAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String index = etIndex.getText().toString();
                String url = URL + index;
                FileInfo fileInfo = new FileInfo();
                fileInfo.setRequestUrl(url);
                fileInfo.setDownloadUrl(url);
                recorder.put(url, fileInfo);

            }
        });
        findViewById(R.id.btQuery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String index = etIndex.getText().toString();
                recorder.get(URL + index);
            }
        });
        findViewById(R.id.btDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String index = etIndex.getText().toString();
                recorder.delete(URL + index);
            }
        });
        findViewById(R.id.btQueryAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<FileInfo> list = recorder.queryAll();
                Logger.d("query result:\n" + CommonUtils.getListString(list));
            }
        });

        final String downloadUrl = Urls.URLS[0];
        findViewById(R.id.btStartDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadTask = new DownloadTask(MainActivity.this, downloadUrl);
                downloadThread = new Thread(downloadTask);
                downloadTask.addDownloadListener(new DownloadListener() {
                    @Override
                    public void onDownloadStatus(int status, @Nullable FileInfo info) {
                        if (info != null) {
                            tvResult.setText("status:" + status + ",message:" + info.getMessage());
                        }
                    }
                });
                downloadThread.start();
            }
        });

        findViewById(R.id.btStopDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadThread.interrupt();
            }
        });
    }
}
