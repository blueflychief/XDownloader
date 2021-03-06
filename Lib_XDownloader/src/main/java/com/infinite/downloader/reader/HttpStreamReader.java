package com.infinite.downloader.reader;

import android.text.TextUtils;

import com.infinite.downloader.config.Config;
import com.infinite.downloader.config.FileInfo;
import com.infinite.downloader.utils.CommonUtils;
import com.infinite.downloader.utils.DLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

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
 * File support range refer link
 * https://www.cnblogs.com/nxlhero/archive/2019/10/14/11670942.html
 */
public class HttpStreamReader implements StreamReader {

    private static final int HTTP_TEMPORARY_REDIRECT = 307;
    private static final int HTTP_PERMANENT_REDIRECT = 308;

    private static final int CONNECT_TIMEOUT = 15_000;
    private static final int READ_TIMEOUT = 15_000;
    private static final int MAX_REDIRECT_COUNT = 5;
    private static final String HEADER_CONTENT_RANGE = "Content-Range";
    private static final String HEADER_CONTENT_MD5 = "Content-MD5";
    private InputStream inputStream = null;
    private Config taskConfig;
    private int readTimeout = READ_TIMEOUT;
    private int connectTimeout = CONNECT_TIMEOUT;

    public HttpStreamReader() {
    }

    public HttpStreamReader(Config taskConfig) {
        this.taskConfig = taskConfig;
        if (taskConfig != null) {
            readTimeout = taskConfig.getReadTimeout();
            connectTimeout = taskConfig.getConnectTimeout();
        }
    }

    @Override
    public FileInfo getFileInfo(String url, long offset) {
        close();
        DLogger.d("start get file info from remote server,offset:" + offset);
        String sourceUrl = url;
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
                            DLogger.d("setSSLSocketFactory");
                        }
                    } catch (Exception e) {
                        DLogger.e("setSSLSocketFactory exception:" + e.getMessage());
                        e.printStackTrace();
                    }
                    connection.setConnectTimeout(connectTimeout);
                    connection.setReadTimeout(readTimeout);
                    connection.setRequestMethod(method);
                    String range = "bytes=" + offset + "-";
                    connection.setRequestProperty("Range", range);
//                    connection.setRequestProperty("Content-Type", "");//这个Content-Type不能设置，有些服务器动态生成图片可能获取到的图片不正确！！！
                    connection.setRequestProperty("Accept-Encoding", "identity");
                    addHeaders(connection);
                    int responseCode = connection.getResponseCode();
                    DLogger.d("responseCode is " + responseCode);
                    if (isRedirect(responseCode)) {
                        redirected = true;
                    } else if (responseCode == HttpURLConnection.HTTP_OK
                            || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                        redirected = false;
                        inputStream = connection.getInputStream();
                        String contentRange = connection.getHeaderField(HEADER_CONTENT_RANGE);
                        long contentLength = connection.getContentLength();
                        boolean supportRange =
                                (responseCode == HttpURLConnection.HTTP_PARTIAL)
                                        && !TextUtils.isEmpty(contentRange);
                        boolean isChunked = false;
                        if (contentLength == FileInfo.CHUNKED_LENGTH) {
                            supportRange = false;
                            //If url just support chunked transfer,set contentLength=-1,otherwise contentLength=0 and not support download!
                            isChunked = isChunkedTransfer(connection);
                            contentLength = isChunked ? FileInfo.CHUNKED_LENGTH : 0;
                        }
                        DLogger.d("contentRange is " + contentRange
                                + ",supportRange:" + supportRange
                                + ",connectionLength:" + contentLength
                                + ",isChunked:" + isChunked
                                + ",download url:" + url);
                        fileInfo.setFileName(CommonUtils.parseFileName(fileInfo.getRequestUrl()));
                        fileInfo.setFileSize(supportRange && contentLength > 0 ? (contentLength + offset) : contentLength);
                        fileInfo.setContentType(connection.getContentType());
                        fileInfo.setCurrentSize(supportRange ? offset : 0);
                        fileInfo.setDownloadUrl(url);
                        fileInfo.setBreakpointDownload(offset > 0 && supportRange);
                        fileInfo.setSupportRange(supportRange);
                        fileInfo.setFileMd5(connection.getHeaderField(HEADER_CONTENT_MD5));
                        fileInfo.setMessage("get remote file ok,content length:" + contentLength);
                    } else {
                        closeConnection(connection);
                        fileInfo.setMessage("error,get remote file with response code:" + responseCode);
                        break;
                    }
                    //如果是重定向
                    if (redirected) {
                        url = connection.getHeaderField("Location");
                        if (DLogger.isDebugEnable()) {
                            DLogger.e("url redirected:" + sourceUrl);
                            DLogger.e("url to:" + url);
                        }
                        redirectCount++;
                        closeConnection(connection);
                        //允许5次重定位
                        if (redirectCount > MAX_REDIRECT_COUNT) {
                            fileInfo.setMessage("error,get remote file with too much redirect");
                            break;
                        }
                    }
                } while (redirected);
            } catch (Exception e) {
                e.printStackTrace();
                closeConnection(connection);
                fileInfo.setMessage("error,get remote file with io exception");
            }
        }
        return fileInfo;
    }

    private boolean isRedirect(int responseCode) {
        return responseCode == HTTP_MOVED_PERM
                || responseCode == HTTP_MOVED_TEMP
                || responseCode == HTTP_SEE_OTHER
                || responseCode == HTTP_TEMPORARY_REDIRECT
                || responseCode == HTTP_PERMANENT_REDIRECT;
    }

    private void addHeaders(HttpURLConnection connection) {
        if (taskConfig != null && taskConfig.getHeaders() != null) {
            Map<String, String> headers = taskConfig.getHeaders();
            if (headers.size() > 0) {
                Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();
                Map.Entry<String, String> header;
                while (iterator.hasNext()) {
                    header = iterator.next();
                    if (header != null) {
                        if (DLogger.isDebugEnable()) {
                            DLogger.d("add request header " + header.getKey() + "--" + header.getValue());
                        }
                        connection.setRequestProperty(header.getKey(), header.getValue());
                    }
                }
            }
        }
    }


    @Override
    public int readInputStream(byte[] buffer) throws IOException {
        int total = inputStream.read(buffer);
//        DLogger.d("Http StreamReader readInputStream total:" + total);
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
            inputStream = null;
            DLogger.d("HttpStreamReader close");
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

    private boolean isChunkedTransfer(HttpURLConnection connection) {
        return "chunked".equals(connection.getHeaderField("Transfer-Encoding"));
    }
}


