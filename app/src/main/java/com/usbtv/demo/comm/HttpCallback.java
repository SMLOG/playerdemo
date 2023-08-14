package com.usbtv.demo.comm;

public interface HttpCallback {
    void onSuccess(int req_id, String method, String result);

    void onError(String toString);
}
