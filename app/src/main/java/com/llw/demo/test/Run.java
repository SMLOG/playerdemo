package com.llw.demo.test;

import com.llw.demo.Utils;

import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author wuyoushan
 * @date 2017/3/20.
 */
public class Run {

    static public class MyTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("运行了！时间为:" +new Date());
        }
    }

    public static void main(String[] args) {
        Timer timer = new Timer();
        MyTask task = new MyTask();
        System.out.println("当前时间:"+new Date().toString());
        timer.schedule(task, 7000);

        File dir = new File("/Users/alexwang/www/bilibili/");
        String result = Utils.exec("find "+dir.getAbsolutePath()+"/ -name *.dvi");
        JSONObject obj = Utils.getVideoInfo(result.split("\n")[0]);
        System.out.println(result);
    }

}