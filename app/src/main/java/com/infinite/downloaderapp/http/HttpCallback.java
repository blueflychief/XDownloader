package com.infinite.downloaderapp.http;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2020/4/10 - 12:02
 * Description: Class description
 */
public interface HttpCallback<T> {
    void onOk(T data);

    void onFail(int errorCode, String errorMessage);
}
