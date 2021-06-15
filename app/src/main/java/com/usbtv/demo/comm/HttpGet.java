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
            System.out.println("下载完成!!!"+url+":"+filename);
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
        httpUrl.setFollowRedirects(true);
        httpUrl.setInstanceFollowRedirects(true);

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

            oInstance.downLoadByList();
        } catch (Exception err) {
            System.out.println(err.getMessage());
        }
    }
}
