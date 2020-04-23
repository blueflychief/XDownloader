package com.infinite.downloaderapp.chain;

import com.infinite.downloaderapp.chain.interceptor.CacheInterceptor;
import com.infinite.downloaderapp.chain.interceptor.ConnectInterceptor;
import com.infinite.downloaderapp.chain.interceptor.RetryInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2020/4/13 - 17:10
 * Description: Class description
 */
public class OkHttpClient {

    public Response startRequest() {
        Request startRequest = new Request("start Request");
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.add(new CacheInterceptor());
        interceptors.add(new RetryInterceptor());
        interceptors.add(new ConnectInterceptor());
        return new RealInterceptorChain(0, startRequest, interceptors).proceed(startRequest);
    }
}
