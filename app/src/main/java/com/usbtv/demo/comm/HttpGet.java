package com.usbtv.demo.comm;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class HttpGet {

    public final static boolean DEBUG = true;//调试用
    private static int BUFFER_SIZE = 8096;//缓冲区大小
    private Vector vDownLoad = new Vector();//URL列表
    private Vector vFileList = new Vector();//下载后的保存文件名列表

    /**
     * 构造方法
     */
    public HttpGet() {
    }

    /**
     * 清除下载列表
     */
    public void resetList() {
        vDownLoad.clear();
        vFileList.clear();
    }

    /**
     * 增加下载列表项
     *
     * @param url      String
     * @param filename String
     */

    public synchronized void addItem(String url, String filename) {

        vDownLoad.add(url);
        vFileList.add(filename);
    }

    /**
     * 根据列表下载资源
     */
    public synchronized void downLoadByList() {

        String url = null;
        String filename = null;

        //按列表顺序保存资源
        for (int i = 0; i<vDownLoad.size();i++){
            url = (String) vDownLoad.get(i);
            filename = (String) vFileList.get(i);

            try {
                saveToFile(url, filename);
            } catch (IOException err) {
                if (DEBUG) {
                    System.out.println("资源[" + url + "]下载失败!!!");
                }
            }
        }

        if (DEBUG) {
            System.out.println("下载完成!!!");
        }
    }

    /**
     * 将HTTP资源另存为文件
     *
     * @param destUrl  String
     * @param fileName String
     * @throws Exception
     */
    public void saveToFile(String destUrl, String fileName) throws IOException {
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        HttpURLConnection httpUrl = null;
        URL url = null;
        byte[] buf = new byte[BUFFER_SIZE];
        int size = 0;

        //建立链接
        url = new URL(destUrl);
        httpUrl = (HttpURLConnection) url.openConnection();
        //连接指定的资源
        httpUrl.connect();
        //获取网络输入流
        bis = new BufferedInputStream(httpUrl.getInputStream());
        //建立文件
        fos = new FileOutputStream(fileName);


        while ((size = bis.read(buf)) != -1)
            fos.write(buf, 0, size);

        fos.close();
        bis.close();
        httpUrl.disconnect();
    }

    /**
     * 设置代理服务器
     *
     * @param proxy     String
     * @param proxyPort String
     */
    public void setProxyServer(String proxy, String proxyPort) {
        //设置代理服务器
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", proxy);
        System.getProperties().put("proxyPort", proxyPort);
    }


    /**
     * 主方法(用于测试)
     *
     * @param argv String[]
     */
    public static void main(String argv[]) {
        HttpGet oInstance = new HttpGet();
        try {
            //增加下载列表（此处用户可以写入自己代码来增加下载列表）
            oInstance.addItem("http://www.ebook.com/java/网络编程001.zip", "./网络编程1.zip");
            oInstance.addItem("http://www.ebook.com/java/网络编程002.zip", "./网络编程2.zip");
            oInstance.addItem("http://www.ebook.com/java/网络编程003.zip", "./网络编程3.zip");
            oInstance.addItem("http://www.ebook.com/java/网络编程004.zip", "./网络编程4.zip");
            oInstance.addItem("http://www.ebook.com/java/网络编程005.zip", "./网络编程5.zip");
            oInstance.addItem("http://www.ebook.com/java/网络编程006.zip", "./网络编程6.zip");
            oInstance.addItem("http://www.ebook.com/java/网络编程007.zip", "./网络编程7.zip");
            //开始下载
            oInstance.downLoadByList();
        } catch (Exception err) {
            System.out.println(err.getMessage());
        }
    }
}
