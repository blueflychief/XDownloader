package com.infinite.downloaderapp.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-11-22 - 15:00
 * Description: Class description
 */
public class JsonUtil {

    public static String toJson(Object o) {
        String result = "";
        if (o == null) {
            return result;
        }
        Gson gson = new Gson();
        try {
            result = gson.toJson(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String toJsonWithNull(Object o) {
        String result = "";
        if (o == null) {
            return result;
        }
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            result = gson.toJson(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
