package com.usbtv.demo;

import android.view.View;

import com.usbtv.demo.view.adapter.FolderListAdapter;
import com.usbtv.demo.view.adapter.FolderNumListRecycleViewAdapter;

public class MyListener implements FolderNumListRecycleViewAdapter.OnItemClickListener, FolderNumListRecycleViewAdapter.OnItemFocusChangeListener{

    private FolderListAdapter gameListAdapter;
    public MyListener(FolderListAdapter gameListAdapter) {
        this.gameListAdapter = gameListAdapter;
    }

    @Override
    public void onItemClick(View view, int position) {

        System.out.println("click");



    }

    @Override
    public void onItemFocusChange(View view, String mString, int position, boolean hasFocus) {
        System.out.println("onItemFocusChange");

    }
}
