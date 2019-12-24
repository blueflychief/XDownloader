package com.infinite.downloader.reader;

import android.text.TextUtils;

import com.infinite.downloader.config.FileInfo;
import com.infinite.downloader.utils.CommonUtils;
import com.infinite.downloader.utils.DLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
            String method = "GET";
            try {
                do {
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    try {
                        if (connection instanceof HttpsURLConnection) {
                            ((HttpsURLConnection) connection).setSSLSocketFactory(createSSLSocketFactory());
                            DLogger.d("=====setSSLSocketFactory");
                        }
                    } catch (Exception e) {
                        DLogger.e("=====setSSLSocketFactory exception:" + e.getMessage());
                        e.printStackTrace();
                    }
                    connection.setConnectTimeout(CONNECT_TIMEOUT);
                    connection.setReadTimeout(READ_TIMEOUT);
                    connection.setRequestMethod(method);
                    String range = "bytes=" + offset + "-";
                    connection.setRequestProperty("Range", range);
                    connection.setRequestProperty("Content-Type", "");
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_BAD_METHOD) {
                        closeConnection(connection);
                        fileInfo.setMessage("error,get remote file with too much redirect");
                        break;
                    } else if (responseCode == HTTP_MOVED_PERM
                            || responseCode == HTTP_MOVED_TEMP
                            || responseCode == HTTP_SEE_OTHER) {  //重定向
                        redirected = true;
                    } else if (responseCode == HttpURLConnection.HTTP_OK
                            || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                        redirected = false;
                        inputStream = connection.getInputStream();
                        fileInfo.setFileName(CommonUtils.parseFileName(fileInfo.getRequestUrl()));
                        fileInfo.setFileSize(connection.getContentLength() + offset);
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
                        closeConnection(connection);
                        //允许5次重定位
                        if (redirectCount > 5) {
                            fileInfo.setMessage("error,get remote file with too much redirect");
                            break;
                        }
                    }
                } while (redirected);
            } catch (IOException e) {
                e.printStackTrace();
                closeConnection(connection);
                fileInfo.setMessage("error,get remote file with io exception");
            }
        }
        return fileInfo;
    }


    @Override
    public int readInputStream(byte[] buffer) throws IOException {
        int total = inputStream.read(buffer);
        DLogger.d("Http StreamReader readInputStream total:" + total);
        return total;
    }

    @Override
    public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }

    public static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private void closeConnection(HttpURLConnection conn) {
        if (conn != null) {
            conn.disconnect();
        }
    }
}


