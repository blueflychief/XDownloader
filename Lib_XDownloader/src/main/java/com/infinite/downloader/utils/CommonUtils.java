package com.infinite.downloader.utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-10-09 - 15:53
 * Description: Class description
 */
public class CommonUtils {
    private static final long GRADE_128K = 131_072L;
    private static final long GRADE_1MB = 1_048_576L;
    private static final long GRADE_10MB = 10_485_760L;
    private static final long GRADE_100MB = 104_857_600L;

    private static final String FILE_NAME_JOIN = "_";
    private static final int MAX_FILE_NAME_LENGTH = 13;

    @Nullable
    public static String compute16Md5(String string) {
        String s = computeMd5(string);
        if (!TextUtils.isEmpty(s)) {
            return s.substring(8, 24);
        }
        return s;
    }

    @Nullable
    public static String computeMd5(String string) {
        String md5 = "";
        if (!TextUtils.isEmpty(string)) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                byte[] digestBytes = messageDigest.digest(string.getBytes());
                return bytesToHexString(digestBytes);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return md5;
    }

    public static String computeTaskMd5(String url, String saveDirPath) {
        if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(saveDirPath)) {
            return computeMd5(url + saveDirPath);
        }
        return "";
    }

    public static String parseFileName(String url) {
        String fileName = "";
        if (!TextUtils.isEmpty(url)) {
            String urlMd5 = CommonUtils.compute16Md5(url);
            int index = url.lastIndexOf("/");
            if (index > -1 && index < url.length() - 1) {
                fileName = url.substring(index + 1);
                if (!TextUtils.isEmpty(fileName) && fileName.length() > MAX_FILE_NAME_LENGTH) {
                    fileName = fileName.substring(fileName.length() - MAX_FILE_NAME_LENGTH);
                }
                fileName = urlMd5 + FILE_NAME_JOIN + fileName;
            } else {
                fileName = urlMd5;
            }
        }
        return fileName;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static <T> String getListString(List<T> list) {
        StringBuilder sb = new StringBuilder();
        for (T t : list) {
            sb.append(t).append("\n");
        }
        return sb.toString();
    }

    public static boolean isUrl(String s) {
        if (!TextUtils.isEmpty(s)) {
//            return Patterns.WEB_URL.matcher(s).matches();
//            String p = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";
//            return s.matches(p);
            return s.startsWith("http://")
                    || s.startsWith("https://")
                    || s.startsWith("ftp://")
                    || s.startsWith("rtsp://");
        }
        return false;
    }

    public static boolean deleteFile(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
            return true;
        }
        return true;
    }

    public static float computeSpeed(long length, long time) {
        //KB/s
        return length / 1024f / (time / 1000f);
    }

    public static int notifyGrade(long fileLength) {
        if (fileLength > GRADE_100MB) {
            //2M
            return 21;
        }
        if (fileLength > GRADE_10MB) {
            //1MB
            return 20;
        }
        if (fileLength > GRADE_1MB) {
            //256KB
            return 18;
        }
        if (fileLength > GRADE_128K) {
            //32KB
            return 15;
        }
        //8KB
        return 13;
    }
}
