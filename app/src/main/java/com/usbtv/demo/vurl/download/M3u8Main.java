package com.usbtv.demo.vurl.download;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liyaling
 * @email ts_liyaling@qq.com
 * @date 2019/12/14 16:02
 */

public class M3u8Main {

	// private static final String M3U8URL =
	// "https://youku.cdn-56.com/20180109/2SwCGxb4/index.m3u8";
	private static final String M3U8URL = "https://v1.cdtlas.com/20210927/1szIc0Gb/index.m3u8";

	private static List<String> list = Arrays
			.asList(new String[] { 
					"https://v1.cdtlas.com/20210927/1szIc0Gb/index.m3u8",
					"https://v1.cdtlas.com/20210927/Cvd563vq/index.m3u8",
					"https://v1.cdtlas.com/20210927/YCpG4jQD/index.m3u8",
					"https://v1.cdtlas.com/20210927/tbsMhm6w/index.m3u8"
					});

	public static void main(String[] args) {

		
		
		
		ArrayList<String> urls = new ArrayList<String>();
		urls.addAll(list);
		
		startDownload(urls,"/Volumes/Portable/videos/走向共和");
	}
	
	
	public static void startDownload(List<String> urls,String dir) {


		String name = null;
		String url = null;
		do {
			if (urls.size() == 0)
				return;

			url = urls.get(urls.size() - 1);
			name = "" + urls.size();
			urls.remove(urls.size() - 1);

		}

		while (new File(dir + "/" + name + ".mp4").exists());
		
		System.out.println(name+":"+url);

		M3u8DownloadFactory.M3u8Download m3u8Download = M3u8DownloadFactory.getInstance(url);
		// 设置生成目录
		m3u8Download.setDir(dir);
		// 设置视频名称
		m3u8Download.setFileName(name);
		// 设置线程数
		m3u8Download.setThreadCount(30);
		// 设置重试次数
		m3u8Download.setRetryCount(15);
		// 设置连接超时时间（单位：毫秒）
		m3u8Download.setTimeoutMillisecond(10000L);
		/*
		 * 设置日志级别 可选值：NONE INFO DEBUG ERROR
		 */
		m3u8Download.setLogLevel(Constant.INFO);
		// 设置监听器间隔（单位：毫秒）
		m3u8Download.setInterval(500L);
		// 添加额外请求头
		/*
		 * Map<String, Object> headersMap = new HashMap<>();
		 * headersMap.put("Content-Type", "text/html;charset=utf-8");
		 * m3u8Download.addRequestHeaderMap(headersMap);
		 */
		// 添加监听器
		m3u8Download.addListener(new DownloadListener() {
			@Override
			public void start() {
				System.out.println("开始下载！");
			}

			@Override
			public void process(String downloadUrl, int finished, int sum, float percent) {
				System.out
						.println("下载网址：" + downloadUrl + "\t已下载" + finished + "个\t一共" + sum + "个\t已完成" + percent + "%");
			}

			@Override
			public void speed(String speedPerSecond) {
				System.out.println("下载速度：" + speedPerSecond);
			}

			@Override
			public void end() {
				System.out.println("下载完毕");

				startDownload(urls,dir);
			}
		});
		// 开始下载
		m3u8Download.start();
	}
}
