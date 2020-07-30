package com.infinite.downloaderapp.json;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2020/4/28 - 12:05
 * Description: Class description
 */
public interface JsonableEnity<T> extends Serializable {

    T toObject(JSONObject object);

    String toJson();
}
