package com.usbtv.demo;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.view.MyImageView;
import com.usbtv.demo.view.WBottomTitleView;

import app.com.tvrecyclerview.Presenter;
import app.com.tvrecyclerview.RowItem;


public class RegularVerticalPresenter extends Presenter {

    public RegularVerticalPresenter(Context context) {
        super(context);
    }

    @Override
    public View onCreateView() {
        WBottomTitleView view = new WBottomTitleView(getContext());
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
        WBottomTitleView imageView = (WBottomTitleView) viewHolder.view;
        if(folder.getCoverUrl()!=null){
            //ImageUtil.displayImg(imageView,folder.getCoverUrl());
            Glide.with(App.getInstance().getApplicationContext()).load(folder.getCoverUrl()).into(imageView);
           // ImageUtil.displayImg(imageView,App.getProxyUrl(folder.getCoverUrl()));
           // if(imageView.getUrl()==null)
            //imageView.setUrl(App.getProxyUrl(folder.getCoverUrl()));
        }else{
            imageView.setImageURI(null);
            imageView.setTextString(folder.getName());
        }


        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerController.getInstance().hideMenu();

                PlayerController.getInstance().setTypeId(folder.getTypeId());
                PlayerController.getInstance().play(folder.getFiles().iterator().next());

            }
        });
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
