package com.infinite.downloader;

import android.text.TextUtils;

import com.infinite.downloader.utils.CommonUtils;

import java.io.File;
import java.io.Serializable;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-08-28 - 18:10
 * Description: Class description
 */
public class FileInfo implements Serializable {
    private long id;
    private String requestUrl;
    private String downloadUrl;
    private long fileSize;
    private String urlMd5;
    private String fileMd5;
    private String contentType;
    private boolean supportRange;
    private long currentSize;
    private String savePath;
    private String message;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
        this.urlMd5 = CommonUtils.computeMd5(requestUrl);
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUrlMd5() {
        return urlMd5;
    }

    public void setUrlMd5(String urlMd5) {
        this.urlMd5 = urlMd5;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isSupportRange() {
        return supportRange;
    }

    public void setSupportRange(boolean supportRange) {
        this.supportRange = supportRange;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public boolean canDownload() {
        return fileSize > 0;
    }

    public boolean changed(FileInfo info) {
        return info == null
                || fileSize != info.getFileSize()
                || !TextUtils.equals(fileMd5, info.getFileMd5())
                || !localFileExists();
    }


    public boolean localFileExists() {
        File file = new File(savePath);
        return file.exists() && file.isFile();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileName() {
        int index = requestUrl.lastIndexOf("/");
        if (index > -1) {
            return requestUrl.substring(index + 1);
        }
        return urlMd5;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", requestUrl='" + requestUrl + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", fileSize=" + fileSize +
                ", urlMd5='" + urlMd5 + '\'' +
                ", fileMd5='" + fileMd5 + '\'' +
                ", contentType='" + contentType + '\'' +
                ", supportRange=" + supportRange +
                ", currentSize=" + currentSize +
                ", savePath='" + savePath + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
