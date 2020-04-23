package com.infinite.downloaderapp.chain;

import java.util.List;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2020/4/13 - 16:40
 * Description: Class description
 */
public class RealInterceptorChain implements Interceptor.Chain {

    private int index;
    private Request request;
    private List<Interceptor> interceptors;

    public RealInterceptorChain(int index, Request request, List<Interceptor> interceptors) {
        this.index = index;
        this.request = request;
        this.interceptors = interceptors;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response proceed(Request request) {
        RealInterceptorChain realInterceptorChain = new RealInterceptorChain(index + 1, request, interceptors);
        return interceptors.get(index).intercept(realInterceptorChain);
    }

}
