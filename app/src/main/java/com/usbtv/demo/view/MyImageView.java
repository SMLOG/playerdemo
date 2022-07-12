package com.usbtv.demo.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.usbtv.demo.comm.NetUtils;

public class MyImageView extends ImageView {
    public MyImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private String url;

    private class MyTask extends AsyncTask<ImageView, Integer, Drawable> {

        public MyTask(ImageView imageView, String url) {
        }
        @Override
        protected Drawable doInBackground(ImageView... params) {


            return NetUtils.loadImageFromNetwork(MyImageView.this.url);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            // 执行完毕后，则更新UI
            MyImageView.this.setImageDrawable(result);
        }

    }
    public MyImageView(Context context) {
        super(context);
    }
    public void setUrl(String url){
        this.url=url;
        new MyTask(this,null).execute();

    }

    public String getUrl() {
        return url;
    }
}
