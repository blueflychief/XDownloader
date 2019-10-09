package com.infinite.downloader.download;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-09-24 - 18:41
 * Description: Class description
 */
public interface IUpdateListener {
    int DATA_UPDATE = 2;

    int ERROR_UPDATE = -1;

    int INFO_UPDATE = 3;

    int NO_UPDATE = 0;

    int PROG_UPDATE = 1;

    int STATUS_ERROR = -1;

    int STATUS_PROCESS = 1;

    int STATUS_SUCCESS = 0;

    boolean onCheckResult(int paramInt, String paramString);

    boolean onDownload(int paramInt1, int paramInt2, int paramInt3, String paramString);
}
