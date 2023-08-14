package com.usbtv.demo.comm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.usbtv.demo.proxy.HttpBuffer;
import com.usbtv.demo.proxy.ULinkDownload;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;


public class ConvertToInlineHttp implements ResponseBody {
    private final HttpRequest request;
    private final HttpResponse response;
    private final String bvid;
    private final int p;
    private final int typeid;

    private String url;
    private String contentType;
    private int id ;





    public ConvertToInlineHttp(HttpRequest request, HttpResponse response, String bvid, int p, int s,int typeid) {
        this.request = request;
        this.response = response;
        this.bvid = bvid;
        this.p=p;
        this.id=s;
        this.typeid=typeid;

    }


    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isChunked() {
        return true;
    }

    @Override
    public long contentLength() {
        return 0l;
    }

    @Nullable
    @Override
    public MediaType contentType() {


        return MediaType.getFileMediaType("a.mp4");
    }
    @Override
    public synchronized void writeTo(@NonNull OutputStream outputStream) throws IOException {

               HttpBuffer.getBuffer(bvid,p,id,typeid,outputStream);


    }
}

