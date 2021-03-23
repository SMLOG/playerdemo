package com.llw.androidtvdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.MediaType;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;


public class FileDownload implements ResponseBody{
    private final HttpRequest request;
    private final HttpResponse response;
    private String file;
    private long contentLength;
    private long endByte;
    private long startByte;

    public FileDownload(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
        String path ="/storage/udisk0/part1/bilibili/285817851/1/285817851_1_0.mp4";
        this.fileChunkDownload(path,request,response);
    }

    /**
     * 文件支持分块下载和断点续传
     * @param filePath 文件完整路径
     * @param request 请求
     * @param response 响应
     */
    public void fileChunkDownload(String filePath, HttpRequest request, HttpResponse response) {
        String range = request.getHeader("Range");
        File file = new File(filePath);
        //开始下载位置
        long startByte = 0;
        //结束下载位置
        long endByte = file.length() - 1;

        //有range的话
        if (range != null && range.contains("bytes=") && range.contains("-")) {
            range = range.substring(range.lastIndexOf("=") + 1).trim();
            String[] ranges = range.split("-");
            try {
                //判断range的类型
                if (ranges.length == 1) {
                    //类型一：bytes=-2343
                    if (range.startsWith("-")) {
                        endByte = Long.parseLong(ranges[0]);
                    }
                    //类型二：bytes=2343-
                    else if (range.endsWith("-")) {
                        startByte = Long.parseLong(ranges[0]);
                    }
                }
                //类型三：bytes=22-2343
                else if (ranges.length == 2) {
                    startByte = Long.parseLong(ranges[0]);
                    endByte = Long.parseLong(ranges[1]);
                }

            } catch (NumberFormatException e) {
                startByte = 0;
                endByte = file.length() - 1;
            }
        }

        //要下载的长度
        long contentLength = endByte - startByte + 1;
        //文件名
        String fileName = file.getName();
        //文件类型
        String contentType = "video/mp4";

        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        fileName = new String(fileNameBytes, 0, fileNameBytes.length, StandardCharsets.ISO_8859_1);

        //各种响应头设置
        //支持断点续传，获取部分字节内容：
        response.setHeader("Accept-Ranges", "bytes");
        //http状态码要为206：表示获取部分内容
        response.setStatus(HttpResponse.SC_PARTIAL_CONTENT);
        //response.setContentType(contentType);
        response.setHeader("Content-Type", contentType);
        //inline表示浏览器直接使用，attachment表示下载，fileName表示下载的文件名
        response.setHeader("Content-Disposition", "inline;filename=" + fileName);
        response.setHeader("Content-Length", String.valueOf(contentLength));
        // Content-Range，格式为：[要下载的开始位置]-[结束位置]/[文件总大小]
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length());
        this.startByte=startByte;
        this.endByte =endByte;
        this.contentLength = contentLength;

    }

    @Override
    public long contentLength() {
        return this.contentLength;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.ALL;
    }

    @Override
    public void writeTo(@NonNull OutputStream output) throws IOException {


        BufferedOutputStream outputStream = null;
        RandomAccessFile randomAccessFile = null;
        //已传送数据大小
        long transmitted = 0;
        try {
            randomAccessFile = new RandomAccessFile(this.file, "r");

            outputStream = new BufferedOutputStream(output);
            byte[] buff = new byte[4096];
            int len = 0;
            randomAccessFile.seek(this.startByte);
            //warning：判断是否到了最后不足4096（buff的length）个byte这个逻辑（(transmitted + len) <= contentLength）要放前面
            //不然会会先读取randomAccessFile，造成后面读取位置出错;
            while ((transmitted + len) <= this.contentLength && (len = randomAccessFile.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
                transmitted += len;
            }
            //处理不足buff.length部分
            if (transmitted < this.contentLength) {
                len = randomAccessFile.read(buff, 0, (int) (this.contentLength - transmitted));
                outputStream.write(buff, 0, len);
                transmitted += len;
            }

            outputStream.flush();
            //response.flushBuffer();
            randomAccessFile.close();
            System.out.println("下载完毕：" + this.startByte + "-" + this.endByte + "：" + transmitted);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }///end try

    }
}

