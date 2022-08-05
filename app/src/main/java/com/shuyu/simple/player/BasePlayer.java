package com.shuyu.simple.player;

import com.shuyu.simple.model.GSYModel;
import com.shuyu.simple.model.GSYVideoModel;
import com.usbtv.demo.exo.GSYExoModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放器差异管理接口
 Created by guoshuyu on 2018/1/11.
 */

public abstract class BasePlayer implements IPlayer {

    protected  int curMediaIndex=0;
    protected List<GSYVideoModel> mediaItems = new ArrayList<>();
    protected IPlayerInitSuccessListener mPlayerInitSuccessListener;

    public IPlayerInitSuccessListener getPlayerPreparedSuccessListener() {
        return mPlayerInitSuccessListener;
    }

    public void setPlayerInitSuccessListener(IPlayerInitSuccessListener listener) {
        this.mPlayerInitSuccessListener = listener;
    }


}
