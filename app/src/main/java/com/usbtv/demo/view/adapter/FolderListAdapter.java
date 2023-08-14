package com.usbtv.demo.view.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.usbtv.demo.R;
import com.usbtv.demo.PlayerController;
import com.usbtv.demo.data.Folder;

import java.util.List;

public abstract class FolderListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private final LayoutInflater mLayoutInflater;

    private  RecyclerView moviesRecyclerView;
    public FolderListAdapter(RecyclerView moviesRecyclerView, Context context, OnItemClickListener onItemClickListener) {
        this.mContext = context;

        this.moviesRecyclerView = moviesRecyclerView;

        mLayoutInflater = LayoutInflater.from(mContext);
        this.mOnItemClickListener = onItemClickListener;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }




    public interface OnItemClickListener {
        void onItemClick(View view, Folder folder, int position);
    }

    public interface OnItemFocusChangeListener {
        void onItemFocusChange(View view, int position, boolean hasFocus);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.listview_item,parent, false);
        return new RecyclerViewHolder(v);
    }

    //放大动画
    private void ofFloatAnimator(View view,float start,float end){
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(700);//动画时间
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", start, end);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", start, end);
        animatorSet.setInterpolator(new DecelerateInterpolator());//插值器
        animatorSet.play(scaleX).with(scaleY);//组合动画,同时基于x和y轴放大
        animatorSet.start();
    }



    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final RecyclerViewHolder viewHolder = (RecyclerViewHolder) holder;
        Folder folder = PlayerController.getInstance().getCurCatList().get(position);
        viewHolder.tv.setText((folder.getIsFav()>0?"*":"")+folder.getShortName());

        viewHolder.tv.setBackgroundColor(PlayerController.getInstance().isFolderPositionSelected(position)?Color.RED:Color.BLACK);

        holder.itemView.setTag(position);
        viewHolder.indexNum.setText(""+(position+1));
        //GlideUtils.loadImg(mContext,mList.get(position).getCoverUrl(),viewHolder.iv);
        Glide.with(mContext).load(folder.getCoverUrl()).into(viewHolder.iv);

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, folder,position);

                }
            });
        }
        holder.itemView.setFocusable(true);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(hasFocus && !moviesRecyclerView.isComputingLayout()){
                    holder.itemView.setTranslationZ(20);//阴影
                    ofFloatAnimator(holder.itemView,1f,1.3f);//放大

                    PlayerController.getInstance().focusFolder(position);

                }else {
                    holder.itemView.setTranslationZ(0);
                    ofFloatAnimator(holder.itemView,1.3f,1f);
                }
            }
        });

        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(KeyEvent.ACTION_DOWN ==keyEvent.getAction()){
                    switch (i){
                        case KeyEvent.KEYCODE_HOME:
                        case KeyEvent.KEYCODE_MENU: //设置键

                            PlayerController.getInstance().toggleFav();
                            return true;
                        default:
                    }
                }
                return false;


            }
        });


    }

    @Override
    public int getItemCount() {
        List<Folder> curCatList = PlayerController.getInstance().getCurCatList();
        return curCatList!=null?curCatList.size():0;
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView indexNum;
        TextView tv;
        ImageView iv;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv_name);
            iv = (ImageView) itemView.findViewById(R.id.iv_bg);
            indexNum=(TextView) itemView.findViewById(R.id.indexNum);
        }

    }



    /**
     * 当item获得焦点时处理
     *
     * @param itemView itemView
     */
    protected abstract void onItemFocus(View itemView);


    /**
     * 当条目失去焦点时调用
     *
     * @param itemView 条目对应的View
     */
    protected abstract void onItemGetNormal(View itemView);




}
