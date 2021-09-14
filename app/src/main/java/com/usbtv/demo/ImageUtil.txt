package com.usbtv.demo;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.collection.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class ImageUtil {
    private static BitmapCache cache = new  BitmapCache();
    public static void displayImg(ImageView p_w_picpathView, String url) {
        RequestQueue mQueue = Volley.newRequestQueue(App.getInstance().getApplicationContext());

        ImageLoader p_w_picpathLoader = new ImageLoader(mQueue, cache);

        ImageLoader.ImageListener listener = ImageLoader.getImageListener(p_w_picpathView, R.mipmap.ic_launcher, R.mipmap.ic_launcher);
        //p_w_picpathLoader.get(url, listener,100,100);
        p_w_picpathLoader.get(url, listener,150,150);
        //指定图片允许的最大宽度和高度
//        p_w_picpathLoader.get("http://developer.android.com/p_w_picpaths/home/aw_dac.png",listener, 200, 200);
    }


}
 class BitmapCache implements ImageLoader.ImageCache {

    private LruCache<String, Bitmap> cache;

    public BitmapCache() {
        cache = new LruCache<String, Bitmap>(100 * 1024 * 1024) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    @Override
    public Bitmap getBitmap(String url) {
        return cache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        cache.put(url, bitmap);
    }
}