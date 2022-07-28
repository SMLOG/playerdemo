package com.usbtv.demo.vurl;


import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.PlayerController;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.sync.SyncCenter;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.Interceptor;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.HttpResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Interceptor
public class M3UController {

    @GetMapping(path = "/api/m3uUrls")
    String m3uUrls(
            @RequestParam(name = "name", required = true) String name,
            @RequestParam(name = "urls", required = true) String urls
    ) throws IOException, InterruptedException, SQLException {


        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);
        Dao<VFile, Integer> vFileDao = App.getHelper().getDao(VFile.class);
        Folder folder = null;

        folder = folderDao.queryBuilder().where().eq("typeId", 1).and().eq("name",name).queryForFirst();

        if(folder==null){
            folder = new Folder();
            folder.setName(name);
            folder.setTypeId(1);
            folderDao.create(folder);
        }

        int i=1;
        for(String url:urls.trim().split(",")){
            VFile vfile = vFileDao.queryBuilder().where().eq("folder_id", folder.getId())
                    .and().eq("dLink", url).queryForFirst();
            if(vfile==null){
                vfile = new VFile();
                vfile.setdLink(url);
                vfile.setPage(i++);
                vfile.setFolder(folder);
                vfile.setName("");
                vfile.setTypeId(1);
                vfile.setFolder(folder);
                vFileDao.create(vfile);
            }
        }
        Map<String, Integer> typesMap = new LinkedHashMap<>();
        typesMap.put("Manual",1);
        SyncCenter.updateScreenTabs(typesMap);
        PlayerController.getInstance().play(folder,0);
        return "OK";
    }


    @GetMapping(path = "/api/m3u")
    String m3u(
            @RequestParam(name = "s", required = false, defaultValue = "0") int showSummary,
            HttpResponse response
    ) throws IOException, InterruptedException {

        StringBuilder outSb = new StringBuilder();
        outSb.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"></head><body>");



       StringBuilder sb =  App.updateM3U(true);

        outSb.append("<body></html>");



        if(showSummary>1){
            response.setHeader("Content-Type", "text/html");
            return outSb.toString();

        }else {
            response.setHeader("Content-Type", "application/x-mpegURL");
            return sb.toString();
        }
    }



}
