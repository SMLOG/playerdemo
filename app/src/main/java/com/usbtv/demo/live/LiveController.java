package com.usbtv.demo.live;

import com.j256.ormlite.stmt.QueryBuilder;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


@RestController
public class LiveController {
    
    public static final String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";
    
    public static String get(String url) throws IOException {


        OkHttpClient okHttpClient = new OkHttpClient();

        Request.Builder reqBuild = new Request.Builder();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        reqBuild.url(urlBuilder.build());
        Request request = reqBuild.addHeader("User-Agent", AGENT).build();
        final Call call = okHttpClient.newCall(request);
        Response response = call.execute();

        String resp = response.body().string();


        return resp;
    }
    
    @GetMapping("/hls/vod.m3u8")
    public String hlsLive(HttpRequest request,
                          HttpResponse response,
                          @RequestParam(name = "folderId", required = false, defaultValue = "0") int folderId,
                           @RequestParam(name = "rate", required = false, defaultValue = "1080") int rate
    )
            throws IOException {

        response.setHeader("Content-Type", "application/x-mpegURL");
        

        List<String> urls = getUrls(folderId,rate);

        StringBuilder sb = new StringBuilder();

        sb.append("#EXTM3U\n" + "#EXT-X-VERSION:7\n" + "#EXT-X-PLAYLIST-TYPE:VOD\n" + "#EXT-X-INDEPENDENT-SEGMENTS\n"
                + "#EXT-X-TARGETDURATION:60\n"
                + "#EXT-X-MEDIA-SEQUENCE:0\n");

        for(String url:urls){

            String content = get(url);
            content = content.replaceAll("#EXT-X-ENDLIST","").trim();
            String[] sp = content.split("(?=#EXTINF)");

            String lastTsUrl = null;
            for (int k = 1; k < sp.length; k++) {
                if(sp[k].trim().equals(""))continue;

                String extinfo = null;
                String tsUrl = null;
                for (String p : sp[k].trim().split("\n")) {
                    if (p.startsWith("#EXTINF")) extinfo=p;
                    else if (!p.startsWith("#")) tsUrl=getAbsUrl(url,p);
                }
                if(tsUrl.equals(lastTsUrl))continue;
                lastTsUrl = tsUrl;
                sb.append(extinfo).append("\n").append(tsUrl).append("\n");

            }

        }
        sb.append("#EXT-X-ENDLIST").append("\n");

        return sb.toString();
        //response.setBody(new StringBody(sb.toString()));
        //response.getWriter().write(sb.getNextM3u8UrltoString());

    }

    private String getAbsUrl(String url, String p) {
        return url.substring(0,url.lastIndexOf("/")+1)+p;
    }


    private List<String> getUrls(int folderId, int rate) {


        List<String> ret=new ArrayList<>();
        QueryBuilder<Folder, ?> foldersQueryBuilder = null;
        try {
            foldersQueryBuilder = App.getHelper().getDao(Folder.class).
                    queryBuilder();

            foldersQueryBuilder.where().eq("typeId", 3);
            foldersQueryBuilder.query();
            List<VFile> list = App.getHelper().getDao(VFile.class).
                    queryBuilder().where()
                    .eq("folder_id",folderId)
                    .queryBuilder()
                    .orderBy("id", false)
                    .query();

            int fileId = rate>=1080?7:(rate>=720?6:5);
            for(VFile vfile:list){
                String url = vfile.getdLink();
                ret.add(url.substring(0, url.lastIndexOf("/") + 1) + "media-"+fileId+"/iframes.m3u8");
            }
       
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


        return ret;
    }


}
