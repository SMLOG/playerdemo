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


    @ResponseBody
    @GetMapping("/api/translateAll")
    public String translateAll() throws Exception {

        TransApi api = new TransApi();

        Dao<ResItem, Integer> dao = App.getHelper().getDao(ResItem.class);
        List<ResItem> list = dao.queryForAll();
        for(ResItem item:list){

            //String transResult = api.getTransResult(item.getCnText(), "zh", "jp");
            //item.setJpText(transResult);

             //transResult = api.getTransResult(item.getCnText(), "zh", "en");
            //item.setEnText(transResult);

            try {
                System.out.println(item.getCnText());
                System.out.println(item.getEnText());

                if(!item.getEnText().matches("^[a-z A-Z.'?!-]*")){
                    System.out.println(item.getEnText());
                    dao.delete(item);
                }
              //  dao.update(item);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }

        return "done";
    }



}
