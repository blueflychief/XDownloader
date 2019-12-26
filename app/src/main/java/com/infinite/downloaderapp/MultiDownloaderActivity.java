package com.infinite.downloaderapp;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.Nullable;

import com.infinite.downloader.DownloadListener;
import com.infinite.downloader.XDownload;
import com.infinite.downloader.config.Config;
import com.infinite.downloader.config.FileInfo;

import java.io.File;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-12-24 - 17:16
 * Description: Class description
 */
public class MultiDownloaderActivity extends BaseActivity {


    private XDownload download1;
    private XDownload download2;
    private XDownload download3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_downloader);

        findViewById(R.id.btDownload1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download1 = getDownload(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "111");
                for (String image : Urls.IMAGES) {
                    download1.addTask(image, new DownloadListener() {
                        @Override
                        public void onDownloadStatus(int status, @Nullable FileInfo info) {

                        }
                    });
                }
            }
        });

        findViewById(R.id.btDownload1Stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (download1 != null) {
                    download1.shutdown();
                }
            }
        });


        findViewById(R.id.btDownload2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download2 = getDownload(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "222");
                for (String image : Urls.IMAGES) {
                    download2.addTask(image, new DownloadListener() {
                        @Override
                        public void onDownloadStatus(int status, @Nullable FileInfo info) {

                        }
                    });
                }
            }
        });

        findViewById(R.id.btDownload2Stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (download2 != null) {
                    download2.shutdown();
                }
            }
        });


        findViewById(R.id.btDownload3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download3 = getDownload(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "333");
                for (String image : Urls.IMAGES) {
                    download3.addTask(image, new DownloadListener() {
                        @Override
                        public void onDownloadStatus(int status, @Nullable FileInfo info) {

                        }
                    });
                }
            }
        });

        findViewById(R.id.btDownload3Stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (download3 != null) {
                    download3.shutdown();
                }
            }
        });

    }


    private XDownload getDownload(String savePath) {
        Config config = Config.defaultConfig(MultiDownloaderActivity.this);
        config.setSaveDirPath(savePath);
        XDownload xDownload = new XDownload(MultiDownloaderActivity.this, config);
        return xDownload;
    }
}
