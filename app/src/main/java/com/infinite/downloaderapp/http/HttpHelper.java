package com.infinite.downloaderapp.http;

import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.infinite.downloaderapp.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2020/4/10 - 12:00
 * Description: Class description
 */
public class HttpHelper {
    private static final int HTTP_307 = 307;
    private static final int CONNECT_TIMEOUT = 10_000;
    private static final int READ_TIMEOUT = 10_000;
    private static final int MAX_REDIRECT = 5;
    private static final String USER_AGENT_FORMAT = "%s/%s (%s-%s;Android %s; %s;)";
    private static final boolean isDebug = BuildConfig.DEBUG;

    private static final int cpuCount = Runtime.getRuntime().availableProcessors();
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            cpuCount + 1,
            cpuCount << 3 + 1,
            60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadFactory() {
                private int index;

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "[HttpHelper-" + index + "]");
                    index++;
                    return thread;
                }
            });

    public static FutureTask doJsonPost(final String path, final String jsonStr,
                                        HttpCallback callback) {
        final WeakReference<HttpCallback> ref = new WeakReference<>(callback);
        FutureTask<Object> task = new FutureTask<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                doJsonPost(path, jsonStr, null, ref);
                return new Object();
            }
        });
        threadPoolExecutor.submit(task);
        return task;
    }


    public static void doGet(String path, String userAgent,
                             HttpCallback<String> callback) {
        if (!isUrl(path)) {
            if (callback != null) {
                callback.onFail(-1, "bad request url:" + path);
            }
            return;
        }
        HttpURLConnection conn = null;
        boolean redirected;
        int redirectCount = 0;
        try {
            do {
                conn = (HttpURLConnection) new URL(path).openConnection();
                try {
                    if (conn instanceof HttpsURLConnection) {
                        ((HttpsURLConnection) conn).setSSLSocketFactory(createSSLSocketFactory());
                        KLog.d("setSSLSocketFactory");
                    }
                } catch (Exception e) {
                    KLog.printStackTrace(e);
                }
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setUseCaches(false);
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("Accept-Encoding", "gzip");
                try {
                    conn.addRequestProperty("User-Agent", getUserAgent(userAgent));
                } catch (Exception e) {
                    KLog.printStackTrace(e);
                }
                int responseCode = conn.getResponseCode();
                if (needRedirects(responseCode)) {
                    redirected = true;
                } else if (responseCode == HttpURLConnection.HTTP_OK) {
                    redirected = false;
                } else {
                    String info = readInputStreamInfo(conn, conn.getErrorStream());
                    conn = null;
                    if (callback != null) {
                        callback.onFail(-1, "bad request,error info " + info);
                    }
                    KLog.d("bad request method:" + info);
                    return;
                }
                if (redirected) {
                    path = getLocation(conn, path);
                    conn.disconnect();
                    redirectCount++;
                    KLog.d("need redirect to:" + path);
                    if (redirectCount > MAX_REDIRECT) {
                        if (callback != null) {
                            callback.onFail(-1, "too many redirect");
                        }
                        KLog.d("too many redirect");
                        return;
                    }
                }
            } while (redirected);
            String response = readInputStreamInfo(conn, conn.getInputStream());
            printResponse("GET", response);
            if (callback != null) {
                callback.onOk(response);
            }
            KLog.d("get data ok,response:" + response);
        } catch (IOException e) {
            if (callback != null) {
                callback.onFail(-1, e.getMessage());
            }
            KLog.printStackTrace(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static void doJsonPost(String path, String jsonStr, String userAgent,
                                   WeakReference<HttpCallback> callback) {
        if (!isUrl(path)) {
            onRequestFail(callback, -1, "bad request url:" + path);
            return;
        }
        HttpURLConnection conn = null;
        boolean redirected;
        int redirectCount = 0;
        try {
            do {
                URL url = new URL(path);
                conn = (HttpURLConnection) url.openConnection();
                try {
                    if (conn instanceof HttpsURLConnection) {
                        ((HttpsURLConnection) conn).setSSLSocketFactory(createSSLSocketFactory());
                        KLog.d("setSSLSocketFactory");
                    }
                } catch (Exception e) {
                    KLog.printStackTrace(e);
                }
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                try {
                    conn.addRequestProperty("User-Agent", getUserAgent(userAgent));
                } catch (Exception e) {
                    KLog.printStackTrace(e);
                }
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("accept", "application/json");
                conn.setRequestProperty("Accept-Encoding", "gzip");
                if (!TextUtils.isEmpty(jsonStr)) {
                    byte[] jsonBytes = jsonStr.getBytes();
                    KLog.d("json bytes length:" + jsonBytes.length);
                    conn.setRequestProperty("Content-Length", String.valueOf(jsonBytes.length));
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonBytes);
                    os.flush();
                    os.close();
                }
                int responseCode = conn.getResponseCode();
                if (needRedirects(responseCode)) {
                    redirected = true;
                } else if (responseCode == HttpURLConnection.HTTP_OK) {
                    redirected = false;
                } else {
                    String info = readInputStreamInfo(conn, conn.getErrorStream());
                    conn.disconnect();
                    conn = null;
                    onRequestFail(callback, -1, "bad request,error info " + info);
                    KLog.d("bad request method:" + info);
                    return;
                }
                if (redirected) {
                    path = getLocation(conn, path);
                    conn.disconnect();
                    redirectCount++;
                    KLog.d("need redirect to:" + path);
                    if (redirectCount > MAX_REDIRECT) {
                        onRequestFail(callback, -1, "too many redirect");
                        KLog.d("too many redirect");
                        return;
                    }
                }
            } while (redirected);
            String info = readInputStreamInfo(conn, conn.getInputStream());
            printResponse("POST", info);
            onRequestOk(callback, info);
        } catch (Exception e) {
            onRequestFail(callback, -1, e.getMessage());
            KLog.printStackTrace(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    private static <T> void onRequestOk(WeakReference<HttpCallback> callback, T data) {
        if (callback != null && callback.get() != null) {
            callback.get().onOk(data);
        }
    }

    private static void onRequestFail(WeakReference<HttpCallback> callback, int errorCode, String errorMessage) {
        if (callback != null && callback.get() != null) {
            callback.get().onFail(errorCode, errorMessage);
        }
    }

    private static void printResponse(String method, String info) {
        if (isDebug) {
            KLog.d(method + " response info is:");
            if (!TextUtils.isEmpty(info)) {
                if ((info.startsWith("[") && info.endsWith("]"))
                        || (info.startsWith("{") && info.endsWith("}"))) {
                    KLog.json(info);
                } else {
                    KLog.e(info);
                }
            } else {
                KLog.e(info);
            }
        }
    }

    private static String readInputStreamInfo(HttpURLConnection conn, InputStream in)
            throws IOException {
        if (in == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader rd;
        String encoding = conn.getContentEncoding();
        if (encoding != null && encoding.contains("gzip")) {
            KLog.d("is gzip input stream");
            rd = new BufferedReader(new InputStreamReader(new GZIPInputStream(in),
                    "UTF-8"));
        } else {
            rd = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        }
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        in.close();
        return sb.toString();
    }

    private static String getUserAgent(String userAgent) {
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = System.getProperty("http.agent");
        }
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = getDefaultUserAgent();
        }
        return userAgent;
    }

    private static String getDefaultUserAgent() {
        return getValidatedHeaderString(
                String.format(USER_AGENT_FORMAT,
                        "HttpHelper",
                        "ua",
                        Build.BRAND,
                        Build.MODEL,
                        Build.VERSION.SDK_INT,
                        Locale.getDefault().getCountry()
                ));
    }

    public static boolean isUrl(String s) {
        // TODO: 2020/4/10
        if (!TextUtils.isEmpty(s)) {
            return true;
        }
        return false;
    }

    private static boolean needRedirects(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_MOVED_PERM
                || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                || responseCode == HTTP_307;
    }

    private static String getLocation(HttpURLConnection connection, String path)
            throws MalformedURLException {
        if (connection == null || TextUtils.isEmpty(path)) {
            return null;
        }
        String location = connection.getHeaderField("Location");
        if (TextUtils.isEmpty(location)) {
            location = connection.getHeaderField("location");
        }
        if (TextUtils.isEmpty(location)) {
            return null;
        }
        if (!(location.startsWith("http://") || location
                .startsWith("https://"))) {
            //某些时候会省略host，只返回后面的path，所以需要补全url
            URL originUrl = new URL(path);
            location = originUrl.getProtocol() + "://"
                    + originUrl.getHost() + location;
        }
        return location;
    }

    public static String getValidatedHeaderString(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, length = value.length(); i < length; i++) {
            char c = value.charAt(i);
            if ((c <= '\u001f' && c != '\t') || c >= '\u007f') {
                builder.append("_");
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static SSLSocketFactory createSSLSocketFactory() {
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


    public enum Method {
        /**
         * http request method
         */
        GET("GET"), POST("POST"), HEAD("HEAD"), PUT("PUT"),
        DELETE("DELETE"), CONNECT("CONNECT"), OPTIONS("OPTIONS"), TRACE("TRACE");

        Method(String method) {
            this.method = method;
        }

        private String method;

        public String getMethod() {
            return method;
        }
    }

    public static class Builder {
        private long connectTimeout = 15_000L;
        private long readTimeout = 5_000L;
        private int maxRedirectCount = 5;
        private int retryCount = 0;
        private boolean trustAllHttps = true;
        private Method method = Method.GET;
        private String userAgent;
        private Map<String, Object> queryParameters;
        private Map<String, String> headerList = new HashMap<>(8);

        public long getConnectTimeout() {
            return connectTimeout;
        }

        public Builder setConnectTimeout(long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public long getReadTimeout() {
            return readTimeout;
        }

        public Builder setReadTimeout(long readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public int getMaxRedirectCount() {
            return maxRedirectCount;
        }

        public Builder setMaxRedirectCount(int maxRedirectCount) {
            this.maxRedirectCount = maxRedirectCount;
            return this;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public Builder setRetryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public boolean isTrustAllHttps() {
            return trustAllHttps;
        }

        public Builder setTrustAllHttps(boolean trustAllHttps) {
            this.trustAllHttps = trustAllHttps;
            return this;
        }

        public Method getMethod() {
            return method;
        }

        public Builder setMethod(Method method) {
            this.method = method;
            return this;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public Builder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public void addHeader(String key, String value) {
            this.headerList.put(key, value);
        }

        @NonNull
        public Map<String, String> getHeaders() {
            return headerList;
        }

        public Builder addQueryParameter(String key, Object value) {
            if (queryParameters == null) {
                queryParameters = new HashMap<>(8);
            }
            queryParameters.put(key, value);
            return this;
        }

        @Nullable
        public Map<String, Object> getQueryParameters() {
            return queryParameters;
        }

        public Builder build() {
            Builder builder = new Builder();
            builder.connectTimeout = connectTimeout;
            builder.readTimeout = readTimeout;
            builder.maxRedirectCount = maxRedirectCount;
            builder.retryCount = retryCount;
            builder.trustAllHttps = trustAllHttps;
            builder.method = method;
            builder.userAgent = TextUtils.isEmpty(userAgent) ? getDefaultUserAgent() : userAgent;
            builder.headerList = headerList;
            builder.queryParameters = queryParameters;
            return builder;
        }
    }

}
