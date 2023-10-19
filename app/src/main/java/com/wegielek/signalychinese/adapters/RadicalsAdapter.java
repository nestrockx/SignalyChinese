package com.wegielek.signalychinese.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.interfaces.RadicalsRecyclerViewListener;

import java.util.ArrayList;
import java.util.List;

public class RadicalsAdapter extends RecyclerView.Adapter<RadicalsAdapter.ViewHolder> {

    private final Context context;
    private final List<String[]> dataList;
    private final RadicalsRecyclerViewListener radicalsRecyclerViewListener;

    public RadicalsAdapter (Context context, RadicalsRecyclerViewListener radicalsRecyclerViewListener) {
        this.context = context;
        this.dataList = new ArrayList<>();
        this.radicalsRecyclerViewListener = radicalsRecyclerViewListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<String[]> dataList) {
        this.dataList.clear();
        this.dataList.addAll(dataList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_radicals_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.flexboxLayout.removeAllViews();
        for (int i = 0; i < dataList.get(position).length; i++) {
            TextView textView = new TextView(context);
            textView.setTextSize(32.0f);
            textView.setTextColor(context.getColor(R.color.white));
            textView.setText(dataList.get(position)[i]);
            textView.setPadding(16, 8, 16, 8);
            int finalI = i;
            textView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundColor(context.getColor(R.color.selection_blue));
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        v.setBackgroundColor(Color.TRANSPARENT);
                        radicalsRecyclerViewListener.onRadicalClicked(dataList.get(position)[finalI]);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        v.setBackgroundColor(Color.TRANSPARENT);
                        break;
                }
                return true;
            });


            holder.flexboxLayout.addView(textView);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final FlexboxLayout flexboxLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            flexboxLayout = itemView.findViewById(R.id.radicalsLayoutItem);
            flexboxLayout.removeAllViews();
        }
    }
}
