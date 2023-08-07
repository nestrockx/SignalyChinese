package com.wegielek.signalychinese.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.wegielek.signalychinese.Interfaces.RecyclerViewListener;
import com.wegielek.signalychinese.R;

import java.util.List;

public class CharacterListAdapter extends RecyclerView.Adapter<CharacterListAdapter.ViewHolder> {

    private final RecyclerViewListener recyclerViewListener;
    private final List<String> dataList;

    public CharacterListAdapter(List<String> dataList, RecyclerViewListener recyclerViewListener) {
        this.dataList = dataList;
        this.recyclerViewListener = recyclerViewListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewItem);

            itemView.setOnTouchListener((View.OnTouchListener) (view, motionEvent) -> {
                if (recyclerViewListener != null) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        recyclerViewListener.onItemPressed(itemView);
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            recyclerViewListener.onItemReleased(pos);
                        }
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                        recyclerViewListener.onItemCanceled(itemView);
                    }
                }
                return true;
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(dataList.get(position));
        if (position == 0) {
            //holder.itemView.setBackgroundColor(Color.parseColor("#0055CC"));
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
