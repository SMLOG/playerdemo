package com.usbtv.demo.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.leanback.widget.BrowseFrameLayout;

public class MyRelativeLayout extends RelativeLayout {
    private BrowseFrameLayout.OnFocusSearchListener onFocusSearchListener;
    public MyRelativeLayout(Context context) {
        super(context);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public View focusSearch(View focused, int direction) {
       /* if(onFocusSearchListener!=null) {
            View nextFocus = onFocusSearchListener.onFocusSearch(focused, direction);
            if(nextFocus!=null)return nextFocus;
        }*/
        return super.focusSearch(focused, direction);
    }
    public void setOnFocusSearchListener(BrowseFrameLayout.OnFocusSearchListener onFocusSearchListener) {
        this.onFocusSearchListener = onFocusSearchListener;
    }

}
