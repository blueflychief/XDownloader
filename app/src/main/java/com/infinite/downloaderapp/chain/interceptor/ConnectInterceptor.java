package com.infinite.downloaderapp.chain.interceptor;

import com.infinite.downloaderapp.chain.Interceptor;
import com.infinite.downloaderapp.chain.Request;
import com.infinite.downloaderapp.chain.Response;
import com.infinite.downloaderapp.http.KLog;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2020/4/13 - 17:08
 * Description: Class description
 */
public class ConnectInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) {
        Request request = chain.request();
        request.process("connect request");
        KLog.d("ConnectInterceptor intercept process");
        //这是最后一个拦截器，所以这里是真正进行请求事件处理的地方
        return new Response("process connect response;\n", request);
    }
}
