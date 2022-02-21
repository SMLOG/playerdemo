package com.usbtv.demo.news.video;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.script.ScriptException;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.usbtv.demo.news.BtoAAtoB;
import com.usbtv.demo.news.PakoGzipUtils;
import com.usbtv.demo.news.video.CcVideo;
import com.usbtv.demo.news.video.CcVideoRepository;


public class VideoTask {

	private static final String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";

	private static String token;

	private CcVideoRepository videoRepository;

	public static String compress(String str) throws IOException {
		if (str == null || str.length() == 0) {
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes());
		gzip.close();
		return out.toString("ISO-8859-1");
	}

	private static boolean isContentExists(String filePath) throws IOException {

		String url = "https://api.github.com/repos/SMLOG/data/contents/" + filePath;
		org.jsoup.Connection.Response res;
		try {
			res = Jsoup.connect(url).userAgent(AGENT).header("Authorization", "token " + token).ignoreContentType(true)
					.execute();
			String body = res.body();
			JSONObject json = JSONObject.parseObject(body);
		} catch (HttpStatusException e) {
			// e.printStackTrace();
			if (e.getStatusCode() == 404)
				return false;

			throw e;
		}
		return true;
	}

	private static void syncWithRemote(CcVideoRepository videoRepository) throws IOException, ParseException {

		String path = "vl.json";

		String since = videoRepository.getSince();
		String commitsUrl = "https://api.github.com/repos/SMLOG/data/commits?path=" + URLEncoder.encode(path);

		if (since != null)
			commitsUrl += "&since=" + URLEncoder.encode(since);

		org.jsoup.Connection.Response res = Jsoup.connect(commitsUrl).header("accept", "pplication/vnd.github.v3+json")
				.header("Authorization", "token " + token).userAgent(AGENT).ignoreContentType(true).execute();
		String body = res.body();
		JSONArray commits = JSONObject.parseArray(body);

		System.out.print(commits);

		String Lastsince = null;
		for (int i = 0; i < commits.size(); i++) {

			if (i == 0) {
				Lastsince  = getString(commits.getJSONObject(i), "commit.committer.date");

			}

			JSONObject obj = commits.getJSONObject(i);
			String sha = obj.getString("sha");

			JSONObject json2 = getSha(path + "?ref=" + sha);
			String content = json2.getString("content");
			String raw = BtoAAtoB.atob(content);

			//PakoGzipUtils.compress(content)
			try {
				raw = PakoGzipUtils.uncompress(raw);
			}catch(Throwable e) {

			}
			System.out.println(raw);

			List<CcVideo> items = JSON.parseArray(raw, CcVideo.class);

			for (int j = 0; j < items.size(); j++) {

				String date = items.get(j).getDate();

				Date d = new SimpleDateFormat("yyyy/MM/dd").parse(date);
				if (System.currentTimeMillis() - d.getTime() > 5 * 24 * 3600 * 1000) {

					return;

				}

				List<CcVideo> exists = videoRepository.findByVid(items.get(j).getVid());

				if (exists == null || exists.size() == 0) {
					videoRepository.save(items.get(j));
				} else {
					return;
				}
			}

		}

		if(Lastsince!=null)
			since = videoRepository.saveSince(Lastsince);

		/* */
	}

	private static String getString(JSONObject jsonObject, String string) {
		String[] parts = string.split("\\.");
		JSONObject json = jsonObject;

		for (int i = 0; i < parts.length - 1; i++) {
			json = json.getJSONObject(parts[i]);
		}
		return json.getString(parts[parts.length - 1]);
	}

	private static JSONObject getSha(String filePath) throws IOException {

		String url = "https://api.github.com/repos/SMLOG/data/contents/" + filePath;
		org.jsoup.Connection.Response res;
		try {
			res = Jsoup.connect(url).userAgent(AGENT).header("Authorization", "token " + token).ignoreContentType(true)
					.execute();
			String body = res.body();
			JSONObject json = JSONObject.parseObject(body);
			return json;
		} catch (HttpStatusException e) {
			// e.printStackTrace();
			if (e.getStatusCode() == 404)
				return null;
			throw e;

		}
	}



	private static boolean uploadList(CcVideoRepository uploadItemRepository) throws IOException {

		//	List<Video> list = uploadItemRepository.findAllBySrcAndCcIsNull("cbs");


		/*for (int i=0;i<list.size();i++) {
			Video item=list.get(i);

			try {
				if (item.getCc()!= null &&item.getCc().startsWith("http")) {

					uploadCC(item);
					item.setStatus(0);
					uploadItemRepository.save(item);
				}


			} catch (Exception e) {
				e.printStackTrace();

			}

		}*/

		List<CcVideo>  list = uploadItemRepository.findAllByStatusAndDtGreaterThanOrderByDtDesc(0,System.currentTimeMillis()- 5 * 24 * 3600 * 1000);
		System.out.println("uploading size:" + list.size());
		if (list.size() > 0) {
			String filePath = "vl.json";


			for (int i=0;i<list.size();i++) {
				CcVideo item=list.get(i);

				item.setStatus(1);

			}

			String content = JSON.toJSONString(list);
			JSONObject json = getSha(filePath);
			JSONObject resp = uploadApi(filePath, content, new Date().toGMTString(), true,
					json != null ? json.getString("sha") : null);
			if (resp != null) {

				uploadItemRepository.saveSince(getString(resp, "commit.committer.date"));

				uploadItemRepository.saveAll(list);

				System.out.println("uploaded size:" + list.size());

			} else {
				return false;
			}
		}

		return true;
	}

	private static void uploadCC(CcVideo item) throws IOException {

		String filePath =  new SimpleDateFormat("yyyyMM").format(new Date(item.getDt())) + "/" + item.getVid()
				+ ".vtt";
		Document doc = Jsoup.connect(item.getOrgCc()).userAgent(AGENT).get();

		StringBuilder sb = new StringBuilder();
		sb.append("WEBVTT	#Elemental Media Engine(TM) 2.17.0.0");

		Elements ps = doc.select("body p");
		for (int i = 0; i < ps.size(); i++) {
			Element p = ps.get(i);
			// 00:00:01.768 --> 00:00:01.901

			sb.append("\n").append("\n").append(p.attr("begin")).append("0").append(" --> ").append(p.attr("end")).append("0").append("\n")
					.append(p.text().replaceAll("\n", " "));

		}

		uploadApi(filePath, sb.toString(), item.getDate(), false, null);
		item.setCc(filePath);

	}

	public static String encodeURIComponent(String s) {
		String result = null;

		try {
			result = URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20").replaceAll("\\%21", "!")
					.replaceAll("\\%27", "'").replaceAll("\\%28", "(").replaceAll("\\%29", ")")
					.replaceAll("\\%7E", "~");
		}

		// This exception should never occur.
		catch (UnsupportedEncodingException e) {
			result = s;
		}

		return result;
	}

	private static JSONObject uploadApi(String filePath, String content, String message, boolean gzip, String sha)
			throws IOException {
		String url = "https://api.github.com/repos/SMLOG/data/contents/" + filePath;
		org.jsoup.Connection.Response res;
		try {

			String compressContent = gzip ? PakoGzipUtils.compress(content) : content;

			Map<String, String> bodyMap = new HashMap<String, String>();

			bodyMap.put("message", message);
			bodyMap.put("content", BtoAAtoB.btoa((compressContent)));
			if (sha != null) {
				bodyMap.put("sha", sha);
			}

			res = Jsoup.connect(url).method(Method.PUT).requestBody(JSON.toJSONString(bodyMap))
					.header("accept", "application/vnd.github.v3+json").header("Authorization", "token " + token)
					.userAgent(AGENT).ignoreContentType(true).execute();
			String body = res.body();
			JSONObject json = JSONObject.parseObject(body);
			System.out.println(json);
			return json;

		} catch (HttpStatusException e) {
			e.printStackTrace();
			if (e.getStatusCode() == 404 ||e.getStatusCode()==422)
				return null;

			throw e;
		}
	}

	static synchronized void getList(CcVideoRepository videoRepository)
			throws IOException, ParseException, InterruptedException {

		token = videoRepository.getToken();
		if (token == null || token.trim().equals("")) {
			System.err.println("token is empty.");
			return;
		}

		syncWithRemote(videoRepository);
		Pattern pt = Pattern.compile("CBSNEWS.defaultPayload = (.*?)\n");
		SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd");

		String[] parts= {
				//	"/video/xhr/collection/component/live-channels/",
				"/video/xhr/collection/component/top-stories-for-ott/",
				//"/video/xhr/collection/component/ott-vod-main/",
				"/video/xhr/collection/component/cbs-village-vod-latest-ott-video-door/",
				"/video/xhr/collection/component/full-episodes-auto-vod/",
				//"/video/xhr/collection/editorial/2d12ffd0-3890-419f-9b7f-3fe2860979d1/",
				"/video/xhr/collection/component/coronavirus-vod-ott-schedule/",
				//"/video/xhr/collection/component/ott-playlists/",
				"/video/xhr/collection/component/cbs-reports-ott/",
				// "/video/xhr/collection/component/cbsn-where-to-watch/",
				"/video/xhr/collection/component/cbs-weekend-news-ott/",
				"/video/xhr/collection/component/show-the-uplift-ott/",
				"/video/xhr/collection/component/60-minutes-overtime-ott/",
				"/video/xhr/collection/component/cbs-this-morning-branded/",
				"/video/xhr/collection/component/evening-news-ott/",
				"/video/xhr/collection/component/60-minutes-ott/",
				"/video/xhr/collection/component/red-and-blue-ott/",
				"/video/xhr/collection/component/ott-cbs-saturday-morning/",
				"/video/xhr/collection/component/48-hours-ott/",
				"/video/xhr/collection/component/sunday-morning-ott/",
				"/video/xhr/collection/component/face-the-nation-ott/",
				"/video/xhr/collection/component/the-takeout-ott/",
				//	"/video/xhr/collection/component/emergency-component-cbsn/"
		};

	/*	String[] feeds= {
				"https://www.cbsnews.com/video/xhr/collection/component/evening-news-ott/?is_logged_in=0?is_logged_in=0",
				"https://www.cbsnews.com/video/xhr/collection/component/video-overlay-popular/?is_logged_in=0?is_logged_in=0",
				};*/

		for(String feed:parts) {
			Response res = Jsoup.connect("https://www.cbsnews.com"+feed+"?is_logged_in=0?is_logged_in=0")
					.userAgent(AGENT).ignoreContentType(true).execute();

			String body = res.body();

			JSONObject json = JSONObject.parseObject(body);
			JSONArray items = json.getJSONArray("items");
			for (int i = 0; i < items.size(); i++) {
				JSONObject obj = (JSONObject) items.get(i);

				String vid = obj.getString("id");

				List<CcVideo> exist = videoRepository.findByVid(vid);
				if (exist == null || exist.size() == 0) {
					String title = obj.getString("fulltitle");
					String cc = obj.getString("captions");
					String url = obj.getString("video2");
					if(!obj.getString("type").equals("vod"))continue;
					long dt = obj.getLong("timestamp");
					String d = sd.format(new Date(dt));

					CcVideo video = new CcVideo();
					video.setTitle(title);
					video.setVid(vid);
					video.setOrgCc(cc);
					video.setCc(cc);

					video.setDate(d);
					video.setDt(dt);
					video.setSrc("cbs");
					video.setStatus(0);
					video.setUrl(url);
					videoRepository.save(video);
				}

			}
		}
/*
		SimpleDateFormat sd2 = new SimpleDateFormat("MMddyy");
		Calendar cl = Calendar.getInstance();
		cl.add(cl.DATE, -6);
		for (int i = 0; i < 5; i++) {
			cl.add(cl.DATE, 1);

			if (cl.get(Calendar.DAY_OF_WEEK) == cl.SATURDAY || cl.get(Calendar.DAY_OF_WEEK) == cl.SUNDAY)
				continue;

			String id = sd2.format(cl.getTime()) + "-cbs-evening-news";
			List<Video> exist = videoRepository.findByVid(id);
			if (exist != null && exist.size() > 0 && exist.get(0).getCc() != null)
				continue;

			try {
				Document doc = Jsoup.connect("https://www.cbsnews.com/video/" + id + "/").userAgent(AGENT).get();

				Matcher m = pt.matcher(doc.html());

				while (m.find()) {
					String jsonStr = m.group(1);

					JSONObject obj = JSONObject.parseObject(jsonStr);
					 items = obj.getJSONArray("items");
					if (items.size() > 0) {
						obj = items.getJSONObject(0);

						String title = obj.getString("fulltitle");
						String cc = obj.getString("captions");
						String url = obj.getString("video2");
						long dt = obj.getLong("timestamp");
						String d = sd.format(new Date(dt));

						Video video = null;

						if (exist != null && exist.size() > 0 ) {

							video = exist.get(0);


						} else
							video = new Video();

						video.setTitle(title);
						video.setVid(id);
						video.setOrgCc(cc);
						video.setCc(cc);
						video.setDate(d);
						video.setDt(dt);
						video.setSrc("cbs");
						video.setStatus(0);
						video.setUrl(url);
						videoRepository.save(video);

					}

				}
			}catch(Throwable ee) {
				ee.printStackTrace();
			}



		}*/

		uploadList(videoRepository);

	}

}
