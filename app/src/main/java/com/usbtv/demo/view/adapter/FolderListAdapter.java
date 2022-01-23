package com.usbtv.demo.view.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Interpolator;
import android.os.Build;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.usbtv.demo.R;
import com.usbtv.demo.data.Folder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class FolderListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Folder> mList;
    private List<Folder> allList;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private final LayoutInflater mLayoutInflater;
    private String typeId="";

    private  RecyclerView moviesRecyclerView;
    public FolderListAdapter(RecyclerView moviesRecyclerView, List<Folder> mList, Context context, OnItemClickListener onItemClickListener) {
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



    public void update(List<Folder> folderList) {
        this.allList = folderList;
        this.update(this.typeId);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, List<Folder> mList, int position);
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
    //根据坐标滑动到指定距离
    private void scrollToAmount(RecyclerView recyclerView, int dx, int dy) {
        //如果没有滑动速度等需求，可以直接调用这个方法，使用默认的速度
//                recyclerView.smoothScrollBy(dx,dy);

        //以下对滑动速度提出定制
        try {
           // Class recClass = recyclerView.getClass();
            recyclerView.smoothScrollBy(dx,dy);
            //Method smoothMethod = recClass.getDeclaredMethod("smoothScrollBy", int.class, int.class,  android.view.animation.Interpolator.class, int.class);
            //smoothMethod.invoke(recyclerView, dx, dy, new AccelerateDecelerateInterpolator(), 700);//时间设置为700毫秒，
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    /**
     * 计算需要滑动的距离,使焦点在滑动中始终居中
     * @param recyclerView
     * @param view
     */
    private int[] getScrollAmount(RecyclerView recyclerView, View view) {
        int[] out = new int[2];
        final int parentLeft = recyclerView.getPaddingLeft();
        final int parentTop = recyclerView.getPaddingTop();
        final int parentRight = recyclerView.getWidth() - recyclerView.getPaddingRight();
        final int childLeft = view.getLeft() + 0 - view.getScrollX();
        final int childTop = view.getTop() + 0 - view.getScrollY();

        final int dx =childLeft - parentLeft - ((parentRight - view.getWidth()) / 2);//item左边距减去Recyclerview不在屏幕内的部分，加当前Recyclerview一半的宽度就是居中

        final int dy = childTop - parentTop - (parentTop - view.getHeight()) / 2;//同上
        out[0] = dx;
        out[1] = dy;
        return out;

    }
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final RecyclerViewHolder viewHolder = (RecyclerViewHolder) holder;
        viewHolder.tv.setText(mList.get(position).getShortName());
        holder.itemView.setTag(position);
        //GlideUtils.loadImg(mContext,mList.get(position).getCoverUrl(),viewHolder.iv);
        Glide.with(mContext).load(mList.get(position).getCoverUrl()).into(viewHolder.iv);

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
                    holder.itemView.setTranslationZ(20);//阴影
                    ofFloatAnimator(holder.itemView,1f,1.3f);//放大
                }else {
                    holder.itemView.setTranslationZ(0);
                    ofFloatAnimator(holder.itemView,1.3f,1f);
                }
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




}
