package com.infinite.downloader;

import android.text.TextUtils;

import com.infinite.downloader.utils.CommonUtils;
import com.infinite.downloader.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 10/9/2019 - 22:23
 * Description: class description
 */
public class HttpStreamReader implements StreamReader {
    private static final int CONNECT_TIMEOUT = 15_000;
    private static final int READ_TIMEOUT = 15_000;
    private InputStream inputStream = null;

    @Override
    public FileInfo getFileInfo(String url, long offset) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setRequestUrl(url);
        fileInfo.setDownloadUrl(url);
        if (CommonUtils.isUrl(url)) {
            HttpURLConnection connection = null;
            boolean redirected = false;
            int redirectCount = 0;
            int tryCount = 0;
            String method = "HEAD";
            Logger.d("尝试HEAD方式获取文件信息");
            try {
                do {
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setConnectTimeout(CONNECT_TIMEOUT);
                    connection.setReadTimeout(READ_TIMEOUT);
                    connection.setRequestMethod(method);
                    String range = "bytes=" + offset + "-";
                    connection.setRequestProperty("Range", range);
                    connection.setRequestProperty("Content-Type", "");
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_BAD_METHOD) {
                        connection.disconnect();
                        connection = null;
                        method = "GET";
                        redirected = true;
                        Logger.d("尝试HEAD方式失败，改用GET方式请求:" + tryCount);
                        tryCount++;
                        if (tryCount > 5) {
                            fileInfo.setMessage("error,get remote file with too much redirect");
                            break;
                        }
                    } else if (responseCode == HTTP_MOVED_PERM
                            || responseCode == HTTP_MOVED_TEMP
                            || responseCode == HTTP_SEE_OTHER) {  //重定向
                        redirected = true;
                    } else if (responseCode == HttpURLConnection.HTTP_OK
                            || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                        redirected = false;
                        inputStream = connection.getInputStream();
                        fileInfo.setFileSize(connection.getContentLength());
                        fileInfo.setContentType(connection.getContentType());
                        fileInfo.setDownloadUrl(url);
                        fileInfo.setSupportRange(!TextUtils.isEmpty(connection.getHeaderField("Content-Range")));
                        fileInfo.setFileMd5(connection.getHeaderField("Content-MD5"));
                        fileInfo.setMessage("get remote file ok");
                    }
                    //如果是重定向
                    if (redirected) {
                        url = connection.getHeaderField("Location");
                        redirectCount++;
                        connection.disconnect();
                        //允许5次重定位
                        if (redirectCount > 5) {
                            fileInfo.setMessage("error,get remote file with too much redirect");
                            break;
                        }
                    }
                } while (redirected);
            } catch (IOException e) {
                e.printStackTrace();
                fileInfo.setMessage("error,get remote file with io exception");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return fileInfo;
    }

    @Override
    public int readInputStream(byte[] buffer) throws IOException {
        return inputStream.read(buffer);
    }
}


