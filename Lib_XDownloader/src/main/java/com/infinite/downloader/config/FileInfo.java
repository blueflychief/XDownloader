package com.infinite.downloader.config;

import android.text.TextUtils;

import androidx.annotation.Nullable;

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
    private String saveDirPath;
    private String message;
    private long costTime;
    private String fileName;
    private float speed;
    private boolean breakpointDownload;

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

    public String getSaveDirPath() {
        return saveDirPath;
    }

    public void setSaveDirPath(String savePath) {
        this.saveDirPath = savePath;
    }

    public boolean canDownload() {
        return fileSize > 0;
    }

    public String getFileSavePath() {
        return saveDirPath + File.separator + fileName;
    }

    public boolean changed(FileInfo info) {
        return info == null
                || fileSize != info.getFileSize()
                || !TextUtils.equals(fileName, info.getFileName())
                || !TextUtils.equals(fileMd5, info.getFileMd5())
                || !TextUtils.equals(downloadUrl, info.getDownloadUrl())
                || !TextUtils.equals(saveDirPath, info.getSaveDirPath());
    }

    public boolean finished() {
        return fileSize > 0
                && fileSize == currentSize
                && fileSize == getLocalFileSize();
    }

    public boolean recordInvalid() {
        return fileSize > 0 && fileSize == currentSize && !localFileExists();
    }

    public long getLocalFileSize() {
        File file = getLocalFile();
        return file != null ? file.length() : 0;
    }

    @Nullable
    public File getLocalFile() {
        if (TextUtils.isEmpty(saveDirPath)) {
            return null;
        }
        File file = new File(saveDirPath, fileName);
        return file.exists() && file.isFile() ? file : null;
    }

    public boolean localFileAvailable() {
        return currentSize == getLocalFileSize();
    }

    public boolean localFileExists() {
        return getLocalFile() != null;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCostTime() {
        return costTime;
    }

    public void setCostTime(long costTime) {
        this.costTime = costTime;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isBreakpointDownload() {
        return breakpointDownload;
    }

    public void setBreakpointDownload(boolean breakpointDownload) {
        this.breakpointDownload = breakpointDownload;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
//                ", requestUrl='" + requestUrl + '\'' +
//                ", downloadUrl='" + downloadUrl + '\'' +
                ", fileSize=" + fileSize +
                ", urlMd5='" + urlMd5 + '\'' +
                ", fileMd5='" + fileMd5 + '\'' +
                ", contentType='" + contentType + '\'' +
                ", supportRange=" + supportRange +
                ", currentSize=" + currentSize +
                ", saveDirPath='" + saveDirPath + '\'' +
                ", message='" + message + '\'' +
                ", costTime='" + costTime + '\'' +
                ", fileName='" + fileName + '\'' +
                ", speed='" + speed + '\'' +
                ", breakpointDownload='" + breakpointDownload + '\'' +
                '}';
    }
}
