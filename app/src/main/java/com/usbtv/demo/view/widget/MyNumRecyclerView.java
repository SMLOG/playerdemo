package com.usbtv.demo.view.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.usbtv.demo.view.adapter.MyRecycleViewAdapter;

public class MyNumRecyclerView extends RecyclerView {
    //private int mlastFocusPosition = 0;


    public MyNumRecyclerView(Context context) {
        super(context);
    }

    public MyNumRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyNumRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void requestChildFocus(View child, View focused) {

        super.requestChildFocus(child, focused);
        if (child != null) {
           // mlastFocusPosition = getChildViewHolder(child).getAdapterPosition();
        }
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        MyRecycleViewAdapter adapter  = (MyRecycleViewAdapter) getAdapter();
        if(adapter!=null){
            View lastFocusedview = getLayoutManager().findViewByPosition(adapter.getCurIndex());
            if (lastFocusedview != null) {
                scrollToPosition(adapter.getCurIndex());
                lastFocusedview.requestFocus();
                //lastFocusedview.setSelected(true);
                return false;
            }
        }

        return super.requestFocus(direction, previouslyFocusedRect);
    }


}