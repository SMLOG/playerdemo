package com.usbtv.demo;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;


public interface UrlServiceApi {


    @GET
    Call<ResponseBody> getParams(@Url String url);

    @POST
    Call<ResponseBody> getData(@Url String url,
                               @Field("aid") String aid,
                               @Field("bcid") String bcid,
                               @Field("absign") String absign
    );
}
