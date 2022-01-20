package com.usbtv.demo.news;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class ListExtractor {
	static final String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";

	abstract Set<UploadItem> getItems() throws Exception;
	 public static String get(String url) throws IOException {
		OkHttpClient okHttpClient = new OkHttpClient();

		Request.Builder reqBuild = new Request.Builder();
		HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
		reqBuild.url(urlBuilder.build());
		Request request = reqBuild.addHeader("User-Agent", AGENT).build();
		final Call call = okHttpClient.newCall(request);
		Response response = call.execute();

		String resp = response.body().string();
		System.out.println(resp);
		return resp;
	}
	boolean canSkip(String date) throws ParseException{
		Date d = new SimpleDateFormat("yyyyMMdd").parse(date);
		return canSkip(d);

	}
	
	 boolean canSkip(Date d) {
		return System.currentTimeMillis() - d.getTime() > 2 * 24 * 3600 * 1000;
	}
}
