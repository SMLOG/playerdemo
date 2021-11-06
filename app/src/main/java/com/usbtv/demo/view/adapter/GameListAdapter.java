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
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.usbtv.demo.R;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.view.util.GlideUtils;

import java.lang.reflect.Method;
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
                    focusStatus(v,holder.getAdapterPosition());
                }else {
                    normalStatus(v);
                }

                if(hasFocus){
                    int[] amount = getScrollAmount(moviesRecyclerView, v);//计算需要滑动的距离
                    //滑动到指定距离
                    scrollToAmount(moviesRecyclerView, amount[0], amount[1]);

                    holder.itemView.setTranslationZ(20);//阴影
                    ofFloatAnimator(holder.itemView,1f,1.3f);//放大
                }else {
                    holder.itemView.setTranslationZ(0);
                    ofFloatAnimator(holder.itemView,1.3f,1f);
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

        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int action = event.getAction();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        int positionUp  =  (int) v.getTag();
                        if (action == KeyEvent.ACTION_DOWN) {
                            if (positionUp <=  0) {
                               // moviesRecyclerView.smoothScrollToPosition(getItemCount() - 1);
                                holder.itemView.setTranslationZ(20);//阴影
                                ofFloatAnimator(holder.itemView,1f,1.3f);//放大
                                return true;
                            }
                        }
                        break;

                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        int positionDown = (int) v.getTag();
                        if (action == KeyEvent.ACTION_DOWN) {
                            if (positionDown >= getItemCount() - 1) {
                               // moviesRecyclerView.smoothScrollToPosition(0);
                                holder.itemView.setTranslationZ(20);//阴影
                                ofFloatAnimator(holder.itemView,1f,1.3f);//放大
                                return true;
                            }
                        }
                        break;
                }
                return false;
            };
        }
        );


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
            itemView.setFocusable(true);
            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(b){
                        int[] amount = getScrollAmount(moviesRecyclerView, view);//计算需要滑动的距离
                        //滑动到指定距离
                        scrollToAmount(moviesRecyclerView, amount[0], amount[1]);

                        //itemView.setTranslationZ(20);//阴影
                        //ofFloatAnimator(itemView,1f,1.3f);//放大
                    }else {
                        //itemView.setTranslationZ(0);
                        //ofFloatAnimator(itemView,1.3f,1f);
                    }
                }

            });
        }
        //根据坐标滑动到指定距离
        private void scrollToAmount(RecyclerView recyclerView, int dx, int dy) {
            //如果没有滑动速度等需求，可以直接调用这个方法，使用默认的速度
//                recyclerView.smoothScrollBy(dx,dy);

            //以下对滑动速度提出定制
            try {
                Class recClass = recyclerView.getClass();
                Method smoothMethod = recClass.getDeclaredMethod("smoothScrollBy", int.class, int.class, Interpolator.class, int.class);
                smoothMethod.invoke(recyclerView, dx, dy, new AccelerateDecelerateInterpolator(), 700);//时间设置为700毫秒，
            } catch (Exception e) {
                e.printStackTrace();
            }

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
