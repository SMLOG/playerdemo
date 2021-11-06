package com.usbtv.demo.view.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.usbtv.demo.PlayerController;
import com.usbtv.demo.R;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.view.widget.MyNumRecyclerView;


/**
 * Created by AD on 2017/9/6.
 *
 * @desc: 标题适配器
 */

public class MyRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final MyNumRecyclerView recyclerView;
    private Context mContext;
    private Folder folder;
    private OnItemFocusChangeListener mOnFocusChangeListener;
    private OnItemClickListener mOnItemClickListener;
    private final LayoutInflater mLayoutInflater;

    private int defaultFocus = 0;
    private boolean needFocus = true;

    public MyRecycleViewAdapter(Context context, MyNumRecyclerView numTabRecyclerView) {
        this.mContext = context;
        this.recyclerView = numTabRecyclerView;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void refresh(Folder f) {
        if (f!=null&&folder!=null&&f.getId() == folder.getId()) return;
        this.folder = f;
        this.notifyDataSetChanged();
    }



    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemFocusChangeListener {
        void onItemFocusChange(View view, String mString, int position, boolean hasFocus);
    }


    public final void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnFocusChangeListener(@Nullable OnItemFocusChangeListener mOnFocusChangeListener) {
        this.mOnFocusChangeListener = mOnFocusChangeListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.layout_recycleview_item, parent, false);
        return new RecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final RecyclerViewHolder viewHolder = (RecyclerViewHolder) holder;

        viewHolder.tv.setText(position + 1 + "");


        viewHolder.tv.setTextColor(position == PlayerController.getInstance().getCurIndex() ? Color.RED : Color.WHITE);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerController.getInstance().play(folder.getFiles().toArray(new VFile[]{})[position]).hideMenu();

                viewHolder.tv.setTextColor(Color.RED);
                notifyDataSetChanged();
            }
        });

        if (mOnFocusChangeListener != null) {
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (mOnFocusChangeListener != null) {
                        // mOnFocusChangeListener.onItemFocusChange(v, mStrings[position],position, hasFocus);
                    }
                }
            });
        }


        holder.itemView.setFocusable(true);

        if (needFocus) {
            if (defaultFocus < 0) {
                defaultFocus = 0;
            }
            if (defaultFocus >= getItemCount()) {
                defaultFocus = getItemCount() - 1;
            }
            if (defaultFocus == position) {
                if (!holder.itemView.isFocusable()) {
                    defaultFocus++;
                } else {
                    holder.itemView.requestFocus();
                }
            }
        } else {
            //setNeedFocus(position == (getItemCount() - 1));
        }

        holder.itemView.setFocusable(true);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {


                if(hasFocus){
                    int[] amount = getScrollAmount(recyclerView, v);//计算需要滑动的距离
                    //滑动到指定距离
                    scrollToAmount(recyclerView, amount[0], amount[1]);

                }else {
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
    public int getItemCount() {
        if (folder != null)
            return folder.getFiles().size();
        return 0;
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView tv;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv);
        }
    }
}
