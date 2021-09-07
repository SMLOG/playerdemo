package com.usbtv.demo;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.usbtv.demo.data.Folder;
import com.usbtv.demo.view.MyImageView;

import app.com.tvrecyclerview.Presenter;
import app.com.tvrecyclerview.RowItem;


public class RegularVerticalPresenter extends Presenter {

    public RegularVerticalPresenter(Context context) {
        super(context);
    }

    @Override
    public View onCreateView() {
        MyImageView view = new MyImageView(getContext());
        view.setSelected(true);
        view.setFocusable(true);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(413, 238);
        view.setLayoutParams(params);
        return view;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        viewHolder.view.setBackgroundColor(Utils.getRandColor());
        Folder folder = (Folder) item;
        MyImageView imageView = (MyImageView) viewHolder.view;
        if(folder.getCoverUrl()!=null){
            imageView.setUrl(App.getProxyUrl(folder.getCoverUrl()));
        }


        viewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PlayerController.getInstance().play(folder.getFiles().iterator().next());
                return true;
            }
        });
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
