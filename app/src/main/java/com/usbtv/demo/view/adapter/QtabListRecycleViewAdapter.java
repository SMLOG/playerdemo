package com.usbtv.demo.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.usbtv.demo.R;
import com.usbtv.demo.PlayerController;


public class QtabListRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final RecyclerView recyclerView;
    private Context mContext;
    private final LayoutInflater mLayoutInflater;


    public QtabListRecycleViewAdapter(Context context, RecyclerView numTabRecyclerView) {
        this.mContext = context;
        this.recyclerView = numTabRecyclerView;
        mLayoutInflater = LayoutInflater.from(mContext);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.layout_recycleview_q_item, parent, false);
        return new RecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final RecyclerViewHolder viewHolder = (RecyclerViewHolder) holder;

        viewHolder.tv.setText(PlayerController.getInstance().getRates()[position]);
        viewHolder.numPos.setText(""+(position+1));
        holder.itemView.setFocusable(true);
        holder.itemView.setClickable(true);
        boolean isSelect = PlayerController.getInstance().getRate(position);
        viewHolder.numPos.setTextColor(isSelect? Color.RED : Color.GRAY);
        viewHolder.tv.setTextColor(isSelect? Color.RED : Color.WHITE);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerController.getInstance().setCurRateIndex(position).hideMenu();
            }
        });

    }

    @Override
    public int getItemCount() {
        return PlayerController.getInstance().getRates().length;
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        TextView numPos;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv);
            numPos = (TextView) itemView.findViewById(R.id.numPos);
        }
    }
}
