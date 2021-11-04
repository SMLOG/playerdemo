package com.usbtv.demo.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.usbtv.demo.PlayerController;
import com.usbtv.demo.R;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;


/**
 * Created by AD on 2017/9/6.
 *
 * @desc: 标题适配器
 */

public class MyRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private Folder folder;
    private OnItemFocusChangeListener mOnFocusChangeListener;
    private OnItemClickListener mOnItemClickListener;
    private final LayoutInflater mLayoutInflater;

    private int defaultFocus = 0;
    private boolean needFocus = true;

    private int curIndex = 0;

    public MyRecycleViewAdapter(Context context) {
        this.mContext = context;

        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void refresh(Folder f) {
        if (f!=null&&folder!=null&&f.getId() == folder.getId()) return;
        this.folder = f;
        curIndex = 0;
        this.notifyDataSetChanged();
    }

    public int getCurIndex() {
        return curIndex;
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


        viewHolder.tv.setTextColor(position == curIndex ? Color.RED : Color.WHITE);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerController.getInstance().play(folder.getFiles().toArray(new VFile[]{})[position]);
                PlayerController.getInstance().hideMenu();
                curIndex = position;
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
