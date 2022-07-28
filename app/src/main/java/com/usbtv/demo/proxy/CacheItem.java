package com.usbtv.demo.proxy;

import java.util.List;
import java.util.Map;

public class CacheItem {
    public byte[] data;
    public String contentType;
    protected int status;
    public String url;
    protected int id;
    public Map<String, List<String>> headers;
}