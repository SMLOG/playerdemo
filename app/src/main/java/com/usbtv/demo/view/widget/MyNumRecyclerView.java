package com.usbtv.demo.view.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.usbtv.demo.PlayerController;
import com.usbtv.demo.view.adapter.MyRecycleViewAdapter;

public class MyNumRecyclerView extends RecyclerView {
    //private int mlastFocusPosition = 0;


    public MyNumRecyclerView(Context context) {
        super(context);
        setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        setFocusable(true);
    }

    public MyNumRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        setFocusable(true);
    }

    public MyNumRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        setFocusable(true);
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

            View lastFocusedview = getLayoutManager().findViewByPosition(PlayerController.getInstance().getCurIndex());
            if (lastFocusedview != null) {
                scrollToPosition(PlayerController.getInstance().getCurIndex());
                lastFocusedview.requestFocus();
               // lastFocusedview.setSelected(true);
                return false;
            }


        return super.requestFocus(direction, previouslyFocusedRect);
    }


}