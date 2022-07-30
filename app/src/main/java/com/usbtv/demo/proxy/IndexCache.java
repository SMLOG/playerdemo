package com.usbtv.demo.proxy;/*
 *Copyright © 2022 SMLOG
 *SMLOG
 *https://smlog.github.io
 *All rights reserved.
 */

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;



public class IndexCache {

    private LinkedHashMap<String, Integer> urlMap = new LinkedHashMap();

    private int lastReqIndex = 0;
    private int downloadIndex = 0;

    private List<CacheItem> list = new ArrayList<CacheItem>();

    private ExecutorService pool;

    long accessTime;

    private int fromDownIndex;

    private int finishId;


    public IndexCache(ArrayList<String> tsUrls) {
        for (int i = 0; i < tsUrls.size(); i++) {
            urlMap.put(tsUrls.get(i), i);
            CacheItem item = new CacheItem();
            item.url = tsUrls.get(i);
            item.status = 0;
            item.id = i;
            list.add(item);
        }

    }

    public int belongCache(String url) {
        Integer index = urlMap.get(url);
        return index == null ? -1 : index;
    }

    public boolean isIndexReady(int index) {
        CacheItem item = list.get(index);
        return item.status == 2;

    }

    public CacheItem waitForReady(int reqIndex) {

        CacheItem item = list.get(reqIndex);
        try {
            synchronized (item) {

                System.out.println("req " + reqIndex+" fromDownIndex:"+fromDownIndex +" lastReqIndex:"+lastReqIndex);

                if(list.get(reqIndex).status <=0) this.fromDownIndex=reqIndex;
                this.lastReqIndex  = reqIndex;

                accessTime = System.currentTimeMillis();

                if(needStartCache(reqIndex)) {
                    startDownload();
                    synchronized (this) {
                        this.notifyAll();
                    }
                }

                if (item.status < 2) {
                    item.wait();
                }
                this.finishId=reqIndex;
                System.out.println("finish " + finishId);

            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

        if (item != null) {
            // list.remove(index);
            return item;
        }
        return null;

    }

    public int len() {
        return urlMap.size();
    }

    private boolean pause = false;

    private boolean stop;

    public void stopAllDownload() {
        this.stop = true;

        synchronized (this) {
            this.notifyAll();
            return;
        }
    }

    public synchronized void startDownload() {

        synchronized (this) {
            if(!stop&&pool!=null) {
                this.notifyAll();
                return;
            }

        }
        stop = false;

        Runnable tRunner = () -> {

            while (true) {
                CacheItem item = null;

                boolean needDown = false;
                int j;
                synchronized (list) {
                    j = this.fromDownIndex++;
                    //if(j<0)j=0;
                    if (j <= list.size() - 1) {
                        item = list.get(j);
                        needDown = item.status <= 0;
                        if(needDown)item.status=1;

                    }

                }

                if (needDown) {
                    item.status = 1;
                    downloadItem(item);
                    downloadIndex = j;
                    System.out.println("downloadIndex:" + downloadIndex + " lastReqIndex:" + lastReqIndex+" fromDownIndex:"+fromDownIndex
                            + " size:" + list.size());

                }
                if (j >= list.size() - 1 || stop) {

                    System.out.println("finish downloadIndex:" + downloadIndex + " lastReqIndex:"
                            + lastReqIndex + " size:" + list.size());
                    this.pool = null;

                    return;
                }
                synchronized (IndexCache.this) {
                    if (this.fromDownIndex - lastReqIndex > 10) {
                        try {
                            System.out.println("wait...");
                            IndexCache.this.wait();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }

            }

        };
        if (this.pool == null) {

            this.pool = Executors.newFixedThreadPool(5);

            for (int i = 0; i < 3; i++) {
                final int threadid = i;
                if(pool!=null)
                    pool.execute(tRunner);

            }
            pool.shutdown();
        }

    }

    private void downloadItem(CacheItem item) {

        int count = 1;
        int retryCount = 3;
        HttpURLConnection httpURLConnection = null;
        long timeoutMillisecond = 100000L;

        HashMap<String, String> requestHeaderMap = new HashMap<String, String>();
        requestHeaderMap.put("User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36");
        String contentType = null;

        InputStream in = null;

        // 重试次数判断
        while (count <= retryCount) {
            try {
                System.out.println(item.url);
                httpURLConnection = (HttpURLConnection) new URL(item.url).openConnection();
                httpURLConnection.setConnectTimeout((int) timeoutMillisecond);
                for (Entry<String, String> entry : requestHeaderMap.entrySet())
                    httpURLConnection.addRequestProperty(entry.getKey(), entry.getValue().toString());
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setReadTimeout((int) timeoutMillisecond);
                httpURLConnection.setDoInput(true);

                contentType = httpURLConnection.getHeaderField("content-type");
                in = httpURLConnection.getInputStream();

                ByteArrayOutputStream byos = new ByteArrayOutputStream();
                byte[] bytes = new byte[8196];
                boolean needSkip = !item.url.contains(".ts");


                int len = 0;
                while ((len = in.read(bytes)) != -1) {
                    byos.write(bytes, 0, len);
                }
                in.close();



                if(needSkip){
                    bytes = byos.toByteArray();
                    byos.reset();

                    int beg=-1;

                    for(int i=0;i<bytes.length;) {

                        if(bytes[i]==0x47 ) {
                            if(i<bytes.length-188&&  bytes[i+188]==0x47 || i==bytes.length-188) {

                                if(beg<0) {
                                    beg=i;
                                }
                                byos.write(bytes, i, 188);
                                i+=188;
                                continue;
                            }

                        }
                        i++;

                    }

                }


                bytes = new byte[byos.size()];
                System.arraycopy(byos.toByteArray(), 0, bytes, 0, bytes.length);
                byos.close();
                item.data = bytes;

                Map<String, List<String>> hfs = httpURLConnection.getHeaderFields();
                item.headers = hfs;
                item.contentType=contentType;
                System.out.println("down:" + item.url);

                break;

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        synchronized (item) {
            System.out.println("notify:" + item.id);
            item.status = 2;
            item.notifyAll();
        }

    }

    public boolean canDel() {
        return this.finishId >= list.size()-1 || System.currentTimeMillis() - accessTime > 10*60 * 1000;
    }

    public boolean needStartCache(int index) {
        return !isIndexReady(index)|| this.finishId!=this.list.size()-1 && downloadIndex -index  <10 || stop;
    }

}
