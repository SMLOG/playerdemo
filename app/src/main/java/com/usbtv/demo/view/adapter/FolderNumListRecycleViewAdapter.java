package com.usbtv.demo.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.usbtv.demo.R;
import com.usbtv.demo.comm.PlayerController;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;


public class FolderNumListRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final RecyclerView recyclerView;
    private Context mContext;
    private final LayoutInflater mLayoutInflater;


    public FolderNumListRecycleViewAdapter(Context context, RecyclerView numTabRecyclerView) {
        this.mContext = context;
        this.recyclerView = numTabRecyclerView;
        mLayoutInflater = LayoutInflater.from(mContext);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.layout_recycleview_item, parent, false);
        return new RecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final RecyclerViewHolder viewHolder = (RecyclerViewHolder) holder;

        if(PlayerController.getInstance().getNumFiles()[position].getName()==null){
            viewHolder.tv.setText( ""+(position+1));
        }else{
            viewHolder.tv.setText( (position+1)+","+PlayerController.getInstance().getNumFiles()[position].getName());
        }

        holder.itemView.setFocusable(true);
        holder.itemView.setClickable(true);
        viewHolder.tv.setTextColor(PlayerController.getInstance().isNumberSelect(position) ? Color.RED : Color.WHITE);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerController.getInstance().play(position).hideMenu();
            }
        });

    }

    @Override
    public int getItemCount() {
        VFile[] files = PlayerController.getInstance().getNumFiles();
        if (files != null)
            return files.length;
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
