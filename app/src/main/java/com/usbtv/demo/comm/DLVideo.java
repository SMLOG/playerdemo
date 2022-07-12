package com.usbtv.demo.comm;

import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class DLVideo {

	private static final String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";

	public static void main(String[] args) throws IOException, SQLException {

		getList();

	}

	
	
	public static void getList() throws IOException, SQLException {
		
		//String raw  = get("https://91mjw.com/last-update");
		//System.out.println(raw);
		try{
			Document doc = Jsoup.connect("https://91mjw.com/last-update").userAgent(AGENT).get();
			Elements items = doc.select("article.u-movie");

			System.out.println(items.size());

			for(Element e:items) {
				String title = e.select("h2").text().trim();
				String cover = e.select("img").attr("data-original");
				String link = e.select("a").attr("href");
				String score = e.select(".pingfen span").text();

				System.out.println(title);


				Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);
				Folder folder = folderDao.queryBuilder().where().eq("name",title).queryForFirst();
				if(folder==null){
					folder=new Folder();
					folder.setCoverUrl(cover);
					folder.setLink(link);
					folder.setName(title);
					folder.setAid(title);
					folder.setScore(score);
					folderDao.create(folder);

				}


				List<String> ids = getIds(link);
				Dao<VFile, Integer> vfileDao = App.getHelper().getDao(VFile.class);

				for(int i=0;i<ids.size();i++){

					String id = ids.get(i);
					String detailLink = "https://91mjw.com/vplay/"+id+".html";

					VFile vFile = vfileDao.queryBuilder().where().eq("folder_id",folder.getId()).and().eq("page",i+1).queryForFirst();

					if(vFile==null){
						vFile = new VFile();
						vFile.setPage(i+1);
						vFile.setdLink(detailLink);
						vFile.setFolder(folder);
						vfileDao.create(vFile);
					}


					//String m3u8=getM3U8(detailLink);
					//System.out.println(m3u8);

				}



			}
		}catch (Throwable t){
			t.printStackTrace();
		}

		
	}
	
	

	public static String getM3U8(String link) throws IOException {
		Document doc = Jsoup.connect(link).userAgent(AGENT).get();


		String part=doc.html().split("var vid=\"")[1];
	 
		return java.net.URLDecoder.decode(part.substring(0,part.indexOf("\"")), "UTF-8");
	}

	private static List<String> getIds(String link) throws IOException {
		Document doc = Jsoup.connect(link).userAgent(AGENT).get();
		Elements items = doc.select(".vlink a");
		
		ArrayList<String> list = new ArrayList<String>();
		for(Element e:items) {
			String id = e.attr("id");
			list.add(id);
			
		}
		return list;
	}



}
