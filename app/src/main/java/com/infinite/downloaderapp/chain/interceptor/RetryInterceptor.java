package com.infinite.downloaderapp.chain.interceptor;

import com.infinite.downloaderapp.chain.Interceptor;
import com.infinite.downloaderapp.chain.Request;
import com.infinite.downloaderapp.chain.Response;
import com.infinite.downloaderapp.http.KLog;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2020/4/13 - 17:09
 * Description: Class description
 */
public class RetryInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) {
        KLog.d("RetryInterceptor intercept process");
        Request request = chain.request();
        request.process("retry request");
        Response proceed = chain.proceed(request);
        proceed.process("retry response");
        return proceed;
    }
}
