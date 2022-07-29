package com.usbtv.demo.proxy;

import java.util.List;
import java.util.Map;

public class CacheItem {
    public byte[] data;
    public String contentType;
    public int status;
    public String url;
    public int id;
    public Map<String, List<String>> headers;
}