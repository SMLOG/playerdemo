package com.usbtv.demo.vurl;


import com.usbtv.demo.App;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.Interceptor;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.HttpResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
