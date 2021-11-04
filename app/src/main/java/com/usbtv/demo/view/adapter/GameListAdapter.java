package com.usbtv.demo.view.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.usbtv.demo.R;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.view.util.GlideUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author: njb
 * @date: 2020/6/22 0022 0:54
 * @desc:游戏列表适配器
 */
public abstract class GameListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Folder> mList;
    private List<Folder> allList;
    private Context mContext;
    private OnItemFocusChangeListener mOnFocusChangeListener;
    private OnItemClickListener mOnItemClickListener;
    private final LayoutInflater mLayoutInflater;
    private String typeId="";

    private  RecyclerView moviesRecyclerView;
    public GameListAdapter(RecyclerView moviesRecyclerView, List<Folder> mList, Context context, OnItemClickListener onItemClickListener) {
        this.allList = mList;
        this.mContext = context;
        List<Folder> newList = new ArrayList<>();
        newList.addAll(allList);

        this.mList = newList;

        this.moviesRecyclerView = moviesRecyclerView;

        mLayoutInflater = LayoutInflater.from(mContext);
        this.mOnItemClickListener = onItemClickListener;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public void update(String mString) {
        List<Folder> newList = new ArrayList<>();

        if("".equals(mString)){
            newList.addAll(allList);
        }else
        for(Folder f:allList){
           if( mString.equals(""+f.getTypeId())){
               newList.add(f);
           }
        }
        mList = newList;
        this.typeId = mString;
        notifyDataSetChanged();
    }



    public void update(List<Folder> gameListBeans) {
        this.allList = gameListBeans;
        this.update(this.typeId);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, List<Folder> mList, int position);
    }

    public interface OnItemFocusChangeListener {
        void onItemFocusChange(View view, int position, boolean hasFocus);
    }


    public final void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnFocusChangeListener(@Nullable OnItemFocusChangeListener mOnFocusChangeListener) {
        this.mOnFocusChangeListener = mOnFocusChangeListener;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.listview_item,parent, false);
        return new RecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final RecyclerViewHolder viewHolder = (RecyclerViewHolder) holder;
        viewHolder.tv.setText(mList.get(position).getShortName());
        GlideUtils.loadImg(mContext,mList.get(position).getCoverUrl(),viewHolder.iv);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, mList,position);

                }
            });
        }
        holder.itemView.setFocusable(true);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    focusStatus(v,holder.getAdapterPosition());
                }else {
                    normalStatus(v);
                }
            }
        });
        holder.itemView.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                int what =event.getAction();
                switch (what) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        RecyclerView recyclerView = (RecyclerView) holder.itemView.getParent();
                        int[] location = new int[2];
                        recyclerView.getLocationOnScreen(location);
                        int x = location[0];
//                            LogUtil.i("swj","GalleryAdapter.onHover.x="+x +",width = "+(recyclerView.getWidth()+x));
                        //为了防止滚动冲突，在滚动时候，获取焦点为了显示全，会回滚，这样会导致滚动停止
                        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                            //当超出RecyclerView的边缘时不去响应滚动
                            if (event.getRawX() > recyclerView.getWidth() + x || event.getRawX() < x) {
                                return true;
                            }
                            //鼠标进入view，争取到焦点
                            v.requestFocusFromTouch();
                            v.requestFocus();
//                                LogUtil.i(this,"HomeTvAdapter.onHover.position:"+position);
                            focusStatus(v, holder.getAdapterPosition());
                        }
                        break;
                    case MotionEvent.ACTION_HOVER_MOVE:  //鼠标在view上移动
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:  //鼠标离开view
                        normalStatus(v);
                        break;
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        ImageView iv;
        RecyclerViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv_name);
            iv = (ImageView) itemView.findViewById(R.id.iv_bg);
        }
    }

    /**
     * item获得焦点时调用
     *
     * @param itemView view
     */
    private void focusStatus(View itemView,int position) {
        if (itemView == null) {
            return;
        }

        float scal;
        scal = 1.8f;

        if (Build.VERSION.SDK_INT >= 21) {
            //抬高Z轴
            ViewCompat.animate(itemView).scaleX(scal).scaleY(scal).translationZ(1.1f).start();
        } else {
            ViewCompat.animate(itemView).scaleX(scal).scaleY(scal).start();
            ViewGroup parent = (ViewGroup) itemView.getParent();
            parent.requestLayout();
            parent.invalidate();
        }
        onItemFocus(itemView);
    }

    /**
     * 当item获得焦点时处理
     *
     * @param itemView itemView
     */
    protected abstract void onItemFocus(View itemView);

    /**
     * item失去焦点时
     *
     * @param itemView item对应的View
     */
    private void normalStatus(View itemView) {
        if (itemView == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            ViewCompat.animate(itemView).scaleX(1.0f).scaleY(1.0f).translationZ(0).start();
        } else {
            ViewCompat.animate(itemView).scaleX(1.0f).scaleY(1.0f).start();
            ViewGroup parent = (ViewGroup) itemView.getParent();
            parent.requestLayout();
            parent.invalidate();
        }
        onItemGetNormal(itemView);
    }

    /**
     * 当条目失去焦点时调用
     *
     * @param itemView 条目对应的View
     */
    protected abstract void onItemGetNormal(View itemView);


    static class OrderNum{
        public int id;
        public int typeId;
        public int seq;
    }

}
