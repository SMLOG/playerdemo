package com.llw.androidtvdemo;


import com.yanzhenjie.andserver.annotation.Controller;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.ResponseBody;

@Controller
public class IndexController {

    @ResponseBody
    @GetMapping("/project/info")
    public String newInfo() {
        return "";
    }

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
    @GetMapping("/projectInfo")
    public String oldInfo() {
        return "forward:/project/info";
    }
}
