package com.usbtv.demo.sync;

import com.alibaba.fastjson.JSONObject;
import com.usbtv.demo.comm.SSLSocketClient;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DownloadBiV8 {

	public static final String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36";
	private static V8ScriptEngine scriptEngine;
	

	public static String join(String s, List<String> values) {
		StringBuilder sb = new StringBuilder();
		for (String v : values) {
			sb.append(v).append(s);
		}
		return sb.toString();
	}

	public static JSONObject getVideoInfo(V8ScriptEngine scriptEngine, String link)
			throws  IOException, InterruptedException {

		OkHttpClient okHttpClient = new OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS)// 设置连接超时时间
				.readTimeout(30, TimeUnit.SECONDS)// 设置读取超时时间
				.sslSocketFactory(SSLSocketClient.getSSLSocketFactory())// 配置
				.hostnameVerifier(SSLSocketClient.getHostnameVerifier()).build();

		Request request;
		Call call;
		Response response;
		List<String> cookies = new ArrayList<String>();
		receiveCookies(okHttpClient, cookies, "https://bilibili.iiilab.com/", null, 0, null);

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("cookie", join(";", cookies));
		headers.put("origin", "https://bilibili.iiilab.com/");
		headers.put("referer", "https://bilibili.iiilab.com/");

		MediaType JSON = MediaType.parse("application/json;charset=utf-8");

		receiveCookies(okHttpClient, cookies, "https://bilibili.iiilab.com/sponsor", headers, 1,
				RequestBody.create(JSON, String.valueOf("")));

		JSONObject jsonObj = requestData(scriptEngine, link, okHttpClient, cookies, JSON);

		return jsonObj;

	}

	private static JSONObject requestData(V8ScriptEngine scriptEngine, String link, OkHttpClient okHttpClient,
			List<String> cookies, MediaType JSON) throws  IOException {
		for (int k = 0; k < 2; k++) {

			Request request;
			Call call;
			Response response;
			JSONObject json = new JSONObject();

			String e = (String) scriptEngine.eval("Math.random().toString(10).substring(2)");

			String i = (String) scriptEngine.eval("tool.cal('" + link + "'+'@'+" + e + ").toString(10)");

			String phead = (String) scriptEngine.eval("tool.uc('" + link + "'," + "'bilibili')");
			String elink = (String) scriptEngine.eval("tool.encode('" + link + "')");
			json.put("link", elink + "@" + e + "@" + i);

			RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

			cookies.add(scriptEngine.eval("_0x5c74a7(-0x27b, -0x284, -0x292, -0x28d)") + "=1");
			String cookieStr = join(";", cookies);
			System.out.println("sendcookie:" + cookieStr);

			request = new Builder().url("https://bilibili.iiilab.com/media")

					.addHeader("Origin", "https://bilibili.iiilab.com/")
					.addHeader("Referer", "https://bilibili.iiilab.com/").addHeader("User-Agent", AGENT)
					.addHeader("Cookie", cookieStr)// .addHeader("X-Client-Data", xclientdata)
					.addHeader("accept-patch", phead).post(requestBody).build();
			call = okHttpClient.newCall(request);
			response = call.execute();
			String rsp = response.body().string();
			System.out.println(rsp);
			JSONObject jsonObj = JSONObject.parseObject(rsp);

			if (jsonObj.getString("data") != null && jsonObj.getIntValue("code") == 200) {

				String decode = (String) scriptEngine.eval("tool.decode('" + jsonObj.getString("data") + "')");
				System.out.println(decode);
				String video = ((JSONObject) JSONObject.parseObject(decode).getJSONArray("medias").get(0))
						.getString("resource_url");
				System.out.println(video);
				JSONObject data = new JSONObject();
				jsonObj.put("video", video);
				jsonObj.put("data", data);
				return jsonObj;

			}

		}
		return null;
	}

	private static List<String> receiveCookies(OkHttpClient okHttpClient, List<String> cookies, String url,
			Map<String, String> headerMap, int methodType, RequestBody requestBody) throws IOException {
		Builder builder = new Builder().url(url).addHeader("User-Agent", AGENT);
		if (headerMap != null) {
			headerMap.keySet().forEach(e -> {
				builder.addHeader(e, headerMap.get(e));
			});
		}
		if (methodType == 1)
			builder.post(requestBody);
		Request request = builder.build();

		Call call = okHttpClient.newCall(request);
		Response response = call.execute();

		for (String cookie : response.headers().values("Set-Cookie")) {
			String value = cookie.split(";")[0];
			if (cookies.indexOf(value) > -1)
				continue;
			cookies.add(value);
			System.out.println(cookie);

		}
		response.close();
		return cookies;
	}

	public static String getObject(JSONObject obj, String string) {

		if (string.indexOf(".") == -1)
			return obj.getString(string);
		String key = string.substring(0, string.indexOf("."));

		return getObject(obj.getJSONObject(key), string.substring(string.indexOf(".") + 1));
	}

	public static void main(String[] args) throws  IOException, InterruptedException {

		String script = "'hello'";
		//ScriptEngineManager manager = new ScriptEngineManager();
		//ScriptEngine scriptEngine = manager.getEngineByName("js");
		V8ScriptEngine scriptEngine =new V8ScriptEngine();

		String path = Thread.currentThread().getContextClassLoader().getResource("").getPath(); // 获取targe路径
		System.out.println(path);
		// FileReader的参数为所要执行的js文件的路径
		scriptEngine.eval(new FileReader(path + "/md5.js"));
		scriptEngine.eval(new FileReader(path + "/crypto.js"));
		scriptEngine.eval(new FileReader(path + "/time2.js"));
		scriptEngine.eval("tool.encode('hello')");
		JSONObject info = getVideoInfo(scriptEngine,
				"https://www.bilibili.com/video/av554544026");

	}

	
	public synchronized static  JSONObject  getVideoUrl(String link) throws Exception {

		if(scriptEngine == null) {
			//ScriptEngineManager manager = new ScriptEngineManager();
			 //scriptEngine = manager.getEngineByName("js");
			 scriptEngine =  new V8ScriptEngine();
			
			String path = Thread.currentThread().getContextClassLoader().getResource("").getPath(); 
			scriptEngine.eval(new FileReader(path + "/md5.js"));
			scriptEngine.eval(new FileReader(path + "/crypto.js"));
			scriptEngine.eval(new FileReader(path + "/time2.js"));
		}

		
		JSONObject info = getVideoInfo(scriptEngine,link);
		
		return info;

	}
	public static String toVideoUrl(String url) {
		try {
			if(url.indexOf("p=1")>-1){
				url = url.split("\\?")[0];
			}
			System.out.println(url);
			JSONObject object = getVideoUrl(url);
			return object.getString("video");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
}
