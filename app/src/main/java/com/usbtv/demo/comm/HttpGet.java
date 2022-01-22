package com.usbtv.demo.comm;

import com.usbtv.demo.data.VFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HttpGet {

    public final static boolean DEBUG = true;//调试用
    private static int BUFFER_SIZE = 8096;//缓冲区大小
    private final Lock lock = new ReentrantLock();

    private BlockingQueue<GetInfo> queue = new LinkedBlockingQueue<GetInfo>();

    /**
     * 构造方法
     */
    public HttpGet() {
    }

    /**
     * 清除下载列表
     */
    public void resetList() {
        queue.clear();
    }

    /**
     * 增加下载列表项
     *
     * @param vfile
     * @param url      String
     * @param filename String
     */

    public  void addItem(VFile vfile, String url, String filename) {
        queue.add(new GetInfo(vfile,url,filename));
    }

    /**
     * 根据列表下载资源
     */
    public synchronized void downLoadByList() {

        if(lock.tryLock()){
            try{
                while (true){
                    GetInfo next = queue.take();
                    if(next==null || next.url == null )break;
                    saveToFile(next.url, next.savePath);
                    //System.out.println("资源[" + next.url + "]下载失败!!!");
                    System.out.println("下载完成!!!"+next.url+":"+next.savePath);

                    next.vFile.setP(next.vFile.getRelativePath());
                    App.getHelper().getDao(VFile.class).update(next.vFile);
                }

            }catch (Exception e){
                e.printStackTrace();

            }finally {
                lock.unlock();
            }

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
        OutputStream fos = null;
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

        //new File(fileName).getParentFile().mkdirs();
        DocumentsUtils.mkdirs(App.getInstance().getApplicationContext(),new File(fileName).getParentFile());
        fos = App.getInstance().documentStream(fileName);
        //fos = new FileOutputStream(fileName);


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

class GetInfo{
     String url;
     String savePath;
     VFile vFile;

    public GetInfo(VFile vFile,String url, String savePath) {
        this.url = url;
        this.savePath = savePath;
        this.vFile=vFile;
    }
}