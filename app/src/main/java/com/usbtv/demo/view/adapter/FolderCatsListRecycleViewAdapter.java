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

import androidx.recyclerview.widget.RecyclerView;

import com.usbtv.demo.R;
import com.usbtv.demo.PlayerController;

import java.util.List;


public class FolderCatsListRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final RecyclerView recyclerView;
    private final FolderListAdapter moviesRecyclerViewAdapter;
    private Context mContext;
    private final LayoutInflater mLayoutInflater;

    private int curPos=0;

    public FolderCatsListRecycleViewAdapter(Context context, RecyclerView rv, FolderListAdapter moviesRecyclerViewAdapter) {
        this.mContext = context;
        this.recyclerView = rv;
        mLayoutInflater = LayoutInflater.from(mContext);
        this.moviesRecyclerViewAdapter=moviesRecyclerViewAdapter;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       // View v = mLayoutInflater.inflate(R.layout.layout_recycleview_cats_item, parent, false);
       // view.setOnFocusChangeListener(mOnFocusChangeListener);
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recycleview_cats_item, parent, false);
        return new RecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final RecyclerViewHolder viewHolder = (RecyclerViewHolder) holder;

        String cat = PlayerController.getInstance().getCats().get(position);
        viewHolder.catText.setText(cat);


        viewHolder.catText.setTextColor(cat.equals( PlayerController.getInstance().getCurCat()) ? Color.RED : Color.WHITE);

        viewHolder.itemView.setFocusable(true);
        viewHolder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && !recyclerView.isComputingLayout()) {
                    PlayerController.getInstance().setCurCat(viewHolder.catText.getText().toString());
                    viewHolder.catText.setTextColor(Color.RED);
                    int p = curPos;
                    curPos = position;
                    notifyItemChanged(curPos);
                    notifyItemChanged(p);

                } else {
                }
            }});

        holder.itemView.setFocusable(true);

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

        List<String> datas = PlayerController.getInstance().getCats();
        if (datas != null)
            return datas.size();
        return 0;
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView catText;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            catText = (TextView) itemView.findViewById(R.id.catText);
        }
    }
}
