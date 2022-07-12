package com.usbtv.demo.news;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class CCTVExtractor extends ListExtractor {

	@Override
	public Set<UploadItem> getItems() throws Exception {

		String feeds[] = new String[] {
				"https://api.cntv.cn/NewArticle/getArticleListByPageId?serviceId=pcenglish&id=PAGE1394789601117162&p=1&n=20" };
		Set<UploadItem> set = new HashSet<UploadItem>();

		for (String feed : feeds) {
			String resp = get(feed);
			JSONObject data = JSON.parseObject(resp);
			data=data.getJSONObject("data");
			JSONArray list = data.getJSONArray("list");
			for (int k = 0; k < list.size(); k++) {
				JSONObject item = (JSONObject) list.getJSONObject(k);
				String url = item.getString("url");
				Date d = new Date(item.getLong("focus_date"));
				if(canSkip(d))continue;
				
				UploadItem	uploadItem=new UploadItem( url, new SimpleDateFormat("yyyyMMdd").format(d));
				uploadItem.setTitle(item.getString("titie"));
				
				String path = url.split("\\.com/")[1].replaceAll("[^0-9a-zA-Z]", "");
				path = path.substring(0, Math.min(30, path.length()));

				uploadItem.setP(path.substring(0, 6) + "/" + path.substring(6));
				uploadItem.setSrc("cctv");
				
				set.add(uploadItem);
			}
		}

		return set;
	}



}
