package com.usbtv.demo.news;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.script.ScriptException;

import org.jsoup.Connection.Method;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


public class NewsFullText {

	private static final String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";

	private static String token;

	private UploadItemRepository uploadItemRepository;



	public static void main(String[] args) throws ScriptException, IOException, ParseException, InterruptedException {

		// getList();

		// String str= encode("Hello World\n");
		String str = PakoGzipUtils.compress("hello world");
		// System.out.println(str);
		// MTIwLDE1NiwyMDMsNzIsMjA1LDIwMSwyMDEsODcsNDAsMjA3LDQ3LDIwMiw3MywxLDAsMjYsMTEsNCw5Mw==
		// H4sIAAAAAAAAA8tIzcnJVyjPL8pJAQCFEUoNCwAAAA==
		System.out.println(BtoAAtoB.btoa(str));
		// str=Base64.getUrlEncoder().withoutPadding().encodeToString(str.getBytes(StandardCharsets.UTF_8));
		// System.out.println(str);

	}

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

	private static void syncWithRemote(UploadItemRepository uploadItemRepository) throws IOException, ParseException {

		String path = "l.json";

		String since = uploadItemRepository.getSince();
		String commitsUrl = "https://api.github.com/repos/SMLOG/data/commits?path=" + URLEncoder.encode(path);

		if (since != null)
			commitsUrl += "&since=" + URLEncoder.encode(since);

		org.jsoup.Connection.Response res = Jsoup.connect(commitsUrl).header("accept", "pplication/vnd.github.v3+json")
				.header("Authorization", "token " + token).userAgent(AGENT).ignoreContentType(true).execute();
		String body = res.body();
		JSONArray commits = JSONObject.parseArray(body);

		System.out.print(commits);

		for (int i = 0; i < commits.size(); i++) {

			if (i == 0)
				since = uploadItemRepository.saveSince(getString(commits.getJSONObject(i), "commit.committer.date"));

			JSONObject obj = commits.getJSONObject(i);
			String sha = obj.getString("sha");

			JSONObject json2 = getSha(path + "?ref=" + sha);
			String content = json2.getString("content");
			String deBase64 = BtoAAtoB.atob(content);
			String raw = PakoGzipUtils.uncompress(deBase64);
			System.out.println(raw);

			List<UploadItem> items = JSON.parseArray(raw, UploadItem.class);

			for (int j = 0; j < items.size(); j++) {

				String date = items.get(j).getDate();

				Date d = new SimpleDateFormat("yyyyMMdd").parse(date);
				if (System.currentTimeMillis() - d.getTime() > 2 * 24 * 3600 * 1000) {

					return;

				}

				List<UploadItem> exists = uploadItemRepository.findByP(items.get(j).getP());

				if (exists == null || exists.size() == 0) {
					uploadItemRepository.save(items.get(j));
				} else {
					return;
				}
			}

		}

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

	private static boolean uploadItem(UploadItem item) throws IOException {

		String filePath = item.getP();
		String content = item.getContent();

		return uploadApi(filePath, content, item.getDate(), true, null) != null;
	}

	private static boolean uploadList(UploadItemRepository uploadItemRepository) throws IOException {

		List<UploadItem> list = uploadItemRepository.findAllByStatusOrderByDate(0);
		System.out.println("uploading size:"+list.size());
		if (list.size() > 0) {
			String filePath = "l.json";
			for (UploadItem item : list) {
				item.setStatus(1);
			}

			String content = JSON.toJSONString(list);
			JSONObject json = getSha(filePath);
			JSONObject resp = uploadApi(filePath, content, new Date().toGMTString(), true,
					json != null ? json.getString("sha") : null);
			if (resp != null) {

				uploadItemRepository.saveSince(getString(resp, "commit.committer.date"));

				uploadItemRepository.saveAll(list);
				
				System.out.println("uploaded size:"+list.size());

			} else {
				return false;
			}
		}

		return true;
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
			if (e.getStatusCode() == 404)
				return null;

			throw e;
		}
	}

	static synchronized void getList(UploadItemRepository uploadItemRepository)
			throws IOException, ParseException, InterruptedException {

		token = uploadItemRepository.getToken();
		if (token == null || token.trim().equals("")) {
			System.err.println("token is empty.");
			return;
		}

		syncWithRemote(uploadItemRepository);

		Set<UploadItem> successSet = new HashSet<UploadItem>();

		List<ListExtractor> extractors = ExtractorsFactor.getListExtractors();




		Set<UploadItem> set = new HashSet<UploadItem>();

		for (int i = 0; i < extractors.size(); i++) {

			Set<UploadItem> items;
			try {
				items = extractors.get(i).getItems();
				set.addAll(items);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		Whitelist wl = Whitelist.basicWithImages().removeTags("a").addTags("div");
		for (UploadItem item : set) {
			try {
				List<UploadItem> exist = uploadItemRepository.findByP(item.getP());
				if ((exist == null || exist.size() == 0) && !isContentExists(item.getP())) {

					Document doc2 = Jsoup.connect("https://ftr.fivefilters.net/makefulltextfeed.php")
							.data("url", item.getUrl()).data("max", "1").userAgent(AGENT).get();
					Elements description = doc2.select("description").eq(1);

					String text = description.text();

					String clean = Jsoup.clean(text, wl);
					// Jsoup.clean(text,
					// Whitelist.basicWithImages().removeTags("a").addTags("div"));

					System.out.println(text);
					clean = clean.replace("<p><strong>Adblock test</strong> (Why?)</p>", "");
					clean = clean.replaceAll("<p></p>", "");
					String title = doc2.select("title").eq(1).text();
					item.setTitle(title);
					item.setContent(clean);
					if (text.length() > 200 && uploadItem(item)) {
						successSet.add(item);
						uploadItemRepository.save(item);
					}

					Thread.sleep(1000 * 10);

				} else {
					successSet.add(item);
				}

			} catch (Exception e) {
				e.printStackTrace();

			}
			if (uploadItemRepository.findAllByStatusOrderByDate(0).size() > 10) {
				uploadList(uploadItemRepository);
			}

		}

		if (successSet.size() > 0) {
			uploadList(uploadItemRepository);
		}

	}



}
