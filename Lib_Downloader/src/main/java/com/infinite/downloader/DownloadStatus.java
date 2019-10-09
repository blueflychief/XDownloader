package com.infinite.downloader;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 10/9/2019 - 21:58
 * Description: class description
 */
public class DownloadStatus {
    public static final int ERROR = 0;
    public static final int PREPARE = 1;
    public static final int PREPARED = 2;
    public static final int STARTED = 3;
    public static final int DOWNLOADING = 4;
    public static final int FINISH = 5;
    public static final int STOP = 6;

    @IntDef({
            ERROR, PREPARE, PREPARED, STARTED, DOWNLOADING, FINISH, STOP
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {

    }
}

