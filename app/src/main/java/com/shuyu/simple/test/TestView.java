package com.shuyu.simple.test;

import android.content.Context;
import android.util.AttributeSet;

import com.shuyu.simple.video.StandardGSYVideoPlayer;
import com.usbtv.demo.R;

public class TestView extends StandardGSYVideoPlayer {
    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
    }
}
