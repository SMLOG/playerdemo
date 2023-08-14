package com.usbtv.demo.news;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GTExtractor extends ListExtractor {

	@Override
	public Set<UploadItem> getItems() throws Exception {

		String feeds[] = new String[] { "https://www.globaltimes.cn/index.html", };

		Pattern p = Pattern.compile("(/(\\d{4}/\\d{2}/\\d{2})/[^\"\\\\]+)");
		Set<UploadItem> set = new HashSet<UploadItem>();
		Pattern p2 = Pattern.compile("(/page/(\\d{6})/\\d+.shtml)");

		for (String feed : feeds) {
			String resp = get(feed);

			Matcher m = p2.matcher(resp);
			while (m.find()) {
				String url = m.group(1);
				String date = m.group(2);
				Date d = new Date(new Date().getTime() - 12 * 3600 * 1000);

				if (!canSkip(d) && url.endsWith(".shtml")) {
					System.out.println(url);
					UploadItem item = new UploadItem("https://www.globaltimes.cn" + url,
							new SimpleDateFormat("yyyyMMdd").format(d));
					item.setSrc("GT");

					String path = (date + url.replaceAll("[^0-9a-zA-Z]", ""));
					path = path.substring(0, Math.min(30, path.length()));
					item.setP(path.substring(0, 6) + "/" + path.substring(6));
					set.add(item);

				}
			}
		}

		return set;
	}

}
