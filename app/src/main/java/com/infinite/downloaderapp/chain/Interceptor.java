package com.infinite.downloaderapp.chain;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2020/4/13 - 16:37
 * Description: Class description
 */
public interface Interceptor {
    Response intercept(Chain chain);

    interface Chain {
        Request request();

        Response proceed(Request request);
    }
}
