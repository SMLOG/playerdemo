package com.usbtv.demo.vurl;


import com.usbtv.demo.comm.App;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.Interceptor;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.HttpResponse;

import java.io.IOException;

@RestController
@Interceptor
public class M3UController {


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
