package com.usbtv.demo;

import androidx.annotation.NonNull;

import com.yanzhenjie.andserver.annotation.Interceptor;
import com.yanzhenjie.andserver.annotation.Resolver;
import com.yanzhenjie.andserver.framework.ExceptionResolver;
import com.yanzhenjie.andserver.framework.HandlerInterceptor;
import com.yanzhenjie.andserver.framework.handler.MethodHandler;
import com.yanzhenjie.andserver.framework.handler.RequestHandler;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;

@Resolver
public class MyInterceptor implements ExceptionResolver {


    @Override
    public void onResolve(@NonNull HttpRequest request, @NonNull HttpResponse response, @NonNull Throwable e) {
        System.out.println("OK");
        if("HEAD".equalsIgnoreCase(request.getMethod().toString())){
            response.setHeader("content-type","video/mp2t");
            response.setHeader("access-control-allow-headers","X-Requested-With");
            response.setHeader("access-control-allow-methods","POST, GET, OPTIONS");
            response.setHeader("access-control-allow-origin","*");
            response.setHeader(" Content-Length","381681");


        }
    }
}
