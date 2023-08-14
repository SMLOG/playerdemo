package com.usbtv.demo.comm;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by wangyuhang@evergrande.cn on 2017/8/23 0023.
 */

public interface RetrofitServiceApi {
    @POST
    Call<ResponseBody> reqPost(@Url String url, @Body RequestBody requestBody);

    @GET
    Call<ResponseBody> reqGet(@Url String url, @QueryMap Map<String, String> options);

    @GET
    Call<ResponseBody> reqGet(@Url String url);
}