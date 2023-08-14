package com.usbtv.demo.view.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.usbtv.demo.PlayerController;

public class MyNumRecyclerView extends RecyclerView {
    private int mlastFocusPosition = 0;


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
            int newPosition =   getChildViewHolder(child).getAdapterPosition();
            if(Math.abs(newPosition-mlastFocusPosition)>1){
                mlastFocusPosition= PlayerController.getInstance().getFileIndexOfFolder();

               scrollToPosition(mlastFocusPosition);
                final View lastFocusedview = getLayoutManager().findViewByPosition(mlastFocusPosition);
                if (lastFocusedview != null) {
                    this.post(new Runnable() {
                        @Override
                        public void run() {
                            lastFocusedview.requestFocus();

                        }
                    });
                }
            }else mlastFocusPosition=newPosition;
        }
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {

        mlastFocusPosition= PlayerController.getInstance().getFileIndexOfFolder();
        scrollToPosition(mlastFocusPosition);
        View lastFocusedview = getLayoutManager().findViewByPosition(mlastFocusPosition);
        if (lastFocusedview != null) {
            lastFocusedview.requestFocus();
            return false;
        }

        return super.requestFocus(direction, previouslyFocusedRect);
    }


}