package com.usbtv.demo;

import android.view.View;

import com.usbtv.demo.view.adapter.GameListAdapter;
import com.usbtv.demo.view.adapter.MyRecycleViewAdapter;

public class MyListener implements MyRecycleViewAdapter.OnItemClickListener,MyRecycleViewAdapter.OnItemFocusChangeListener{

    private GameListAdapter gameListAdapter;
    public MyListener(GameListAdapter gameListAdapter) {
        this.gameListAdapter = gameListAdapter;
    }

    @Override
    public void onItemClick(View view, int position) {

        System.out.println("click");



    }

    @Override
    public void onItemFocusChange(View view, String mString, int position, boolean hasFocus) {
        System.out.println("onItemFocusChange");
        gameListAdapter.update(App.typesMap.get(mString));

    }
}
