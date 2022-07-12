package com.usbtv.demo.comm;

import android.graphics.Color;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtil {

    public static RetrofitServiceApi getApi(String baseUrl){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

     return   retrofit.create(RetrofitServiceApi.class);
    }
    public static void reqGetHttp(final RetrofitServiceApi retrofitServiceApi,final int req_id, final String method, String url,
                           Map<String, String> options, final HttpCallback callback) {


        Call<ResponseBody> call = null;
        if (options == null) {
            call = retrofitServiceApi.reqGet(url);
        } else {
            call = retrofitServiceApi.reqGet(url, options);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result = response.body().string();

                    if (callback != null) {
                        callback.onSuccess(req_id, method, result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    if (callback != null) {
                        callback.onError(e.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (callback != null) {
                    callback.onError(t.toString());
                }
            }
        });
    }

    public static class Utils2 {
        public static String inputStreamToString(InputStream inputStream) {
            try {
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes, 0, bytes.length);
                String json = new String(bytes);
                return json;
            } catch (IOException e) {
                return null;
            }
        }

        public static int getRandColor() {
            Random random = new Random();
            return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
    }
}

