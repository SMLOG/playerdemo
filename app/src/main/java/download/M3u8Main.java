package download;

import java.io.File;


/**
 * @author liyaling
 * @email ts_liyaling@qq.com
 * @date 2019/12/14 16:02
 */

public class M3u8Main {


	public static M3u8DownloadFactory.M3u8Download startDownload(String url, String dir, String name) throws Exception {

		if (new File(dir + "/" + name + ".mp4").exists()) return null;
		
		System.out.println(name+":"+url);

		M3u8DownloadFactory.M3u8Download m3u8Download = new M3u8DownloadFactory.M3u8Download(url);
		// 设置生成目录
		m3u8Download.setDir(dir);
		// 设置视频名称
		m3u8Download.setFileName(name);
		// 设置线程数
		m3u8Download.setThreadCount(1);
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

		// 添加监听器
		m3u8Download.addListener(new DownloadListener() {
			@Override
			public void start() {
				System.out.println("开始下载！");
			}

			@Override
			public void process(String downloadUrl, int finished, int sum, float percent) {
				System.out.println("下载网址：" + downloadUrl + "\t已下载" + finished + "个\t一共" + sum + "个\t已完成" + percent + "%");
			}

			@Override
			public void speed(String speedPerSecond) {
				System.out.println("下载速度：" + speedPerSecond);
			}

			@Override
			public void end() {
				System.out.println("下载完毕");
			}
		});
		// 开始下载
		m3u8Download.start();
		return m3u8Download;
	}
}
