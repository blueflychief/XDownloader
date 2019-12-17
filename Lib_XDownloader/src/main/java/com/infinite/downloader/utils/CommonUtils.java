package com.infinite.downloader.utils;

import android.text.TextUtils;

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

    public static String computeMd5(String string) {
        String md5 = null;
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
                    || s.startsWith("ftp://");
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
}
