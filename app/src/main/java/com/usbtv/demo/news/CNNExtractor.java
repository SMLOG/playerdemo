package com.usbtv.demo.news;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CNNExtractor extends ListExtractor {

	@Override
	public Set<UploadItem> getItems() throws Exception {

		String feeds[] = new String[] { "https://edition.cnn.com/", "https://edition.cnn.com/business", };

		Pattern p = Pattern.compile("(/(\\d{4}/\\d{2}/\\d{2})/[^\"\\\\]+)");
		Set<UploadItem> set = new HashSet<UploadItem>();

		for (String feed : feeds) {
			String resp = get(feed);

			Matcher m = p.matcher(resp);
			while (m.find()) {
				String url = m.group(1);
				String date = m.group(2);
				Date d = new SimpleDateFormat("yyyy/MM/dd").parse(date);

				if ( !canSkip(d) && url.endsWith(".html")) {
					System.out.println(url);
					UploadItem item = new UploadItem("https://edition.cnn.com" + url,
							new SimpleDateFormat("yyyyMMdd").format(d));

					String path = url.replaceAll("[^0-9a-zA-Z]", "");
					path = path.substring(0, Math.min(30, path.length()));

					item.setP(path.substring(0, 6) + "/" + path.substring(6));
					item.setSrc("cnn");
					set.add(item);
				}
			}
		}

		return set;
	}

}
