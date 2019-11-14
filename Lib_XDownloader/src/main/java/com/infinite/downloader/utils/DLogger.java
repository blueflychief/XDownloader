package com.infinite.downloader.utils;

import android.util.Log;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-08-28 - 18:20
 * Description: Class description
 */
public class DLogger {
    private static final String DEBUG_TAG = "[DownloadTask]";
    private static boolean debugEnable = false;

    public static void enable() {
        debugEnable = true;
    }

    public static void d(String message) {
        if (debugEnable) {
            Log.d(DEBUG_TAG, message);
        }
    }

    public static void d(String tag, String message) {
        if (debugEnable) {
            Log.d(tag, message);
        }
    }

    public static void e(String message) {
        if (debugEnable) {
            Log.e(DEBUG_TAG, message);
        }
    }

    public static void e(String tag, String message) {
        if (debugEnable) {
            Log.e(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (debugEnable) {
            Log.d(tag, message);
        }
    }

    public static void i(String message) {
        if (debugEnable) {
            Log.d(DEBUG_TAG, message);
        }
    }
}
