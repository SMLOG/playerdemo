package com.usbtv.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

public class FocusFixedLinearLayoutManager extends LinearLayoutManager {


    public FocusFixedLinearLayoutManager(Context context) {
        super (context);
    }
    public FocusFixedLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context,orientation,reverseLayout);
    }

    public FocusFixedLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr,
                                         int defStyleRes) {
        super(context,attrs,defStyleAttr,defStyleRes);
    }


    public View onInterceptFocusSearch2(View focused, int direction) {

        int pos = getPosition(focused);
        int count = getItemCount();
        int orientation = getOrientation();
        if (direction == View.FOCUS_RIGHT) {
            View view = getChildAt(getChildCount() - 1);
            if (view == focused){
                return focused;}
        } else if (direction == View.FOCUS_LEFT) {
            View view = getChildAt(0);
            if (view == focused) {
                return focused;
            }
        }
        return super .onInterceptFocusSearch(focused, direction);
    }
    @Override
    public View onInterceptFocusSearch(View focused, int direction) {

        int pos = getPosition(focused);
        int count = getItemCount();
        int orientation = getOrientation();
        if (direction == View.FOCUS_RIGHT) {
            if (pos == count - 1) {
                return focused;
            }
        } else if (direction == View.FOCUS_LEFT) {
            if (pos == 0) {
                return focused;
            }
        }
        return super.onInterceptFocusSearch(focused, direction);
    }
}