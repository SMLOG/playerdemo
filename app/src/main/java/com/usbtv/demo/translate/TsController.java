package com.usbtv.demo.translate;


import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.App;
import com.usbtv.demo.data.ResItem;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.annotation.RestController;

import java.util.List;

@RestController
public class TsController {
    private static String TAG = "TsController";


    @ResponseBody
    @GetMapping("/api/translate")
    public String translate(@RequestParam(name = "from",required = false,defaultValue = "auto") String from,
                            @RequestParam(name = "to") String to,
                            @RequestParam(name = "q") String q) throws Exception {

        TransApi api = new TransApi();


        String transResult = api.getTransResult(q, from, to);


        return transResult;
    }





}
