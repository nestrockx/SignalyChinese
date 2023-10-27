package com.wegielek.signalychinese.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.interfaces.HistoryRecyclerViewListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<String> dataList;
    private final HistoryRecyclerViewListener historyRecyclerViewListener;

    public HistoryAdapter(HistoryRecyclerViewListener historyRecyclerViewListener, Context context) {
        this.context = context;
        this.historyRecyclerViewListener = historyRecyclerViewListener;
        this.dataList = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<String> dataList) {
        this.dataList.clear();
        this.dataList.addAll(dataList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_search_result_list, parent, false);
        return new HistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        String inputString = dataList.get(position);

        List<String> list = Arrays.asList(inputString.split("/"));
        StringBuilder translation = new StringBuilder();
        for (int i = 5; i < list.size(); i++) {
            if (i == 5) {
                translation.append((i - 4)).append(". ").append(list.get(i)).append(" ");
            } else if (i != list.size() - 1) {
                translation.append((i - 4)).append(".").append(list.get(i)).append(" ");
            } else {
                translation.append((i - 4)).append(".").append(list.get(i)).append("");
            }
        }

        if (!list.get(1).equals(list.get(2))) {
            holder.charactersTv.setText(context.getString(R.string.result_text_placeholder_1, list.get(1), list.get(2)));
        } else {
            holder.charactersTv.setText(list.get(1));
        }
        holder.pronunciationTv.setText(list.get(3));
        holder.translationTv.setText(translation.toString());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView charactersTv;
        private final TextView pronunciationTv;
        private final TextView translationTv;

        public ViewHolder(View itemView) {
            super(itemView);
            charactersTv = itemView.findViewById(R.id.labelTv);
            pronunciationTv = itemView.findViewById(R.id.pronunciationTv);
            translationTv = itemView.findViewById(R.id.translationTv);
            itemView.setOnClickListener(view ->
                    historyRecyclerViewListener.onHistoryClicked(dataList.get(getAdapterPosition()).substring(20))
            );
            itemView.setOnLongClickListener(view -> {
                historyRecyclerViewListener.onLongHistoryClicked(dataList.get(getAdapterPosition()).substring(0, 19));
                return true;
            });
        }
    }

}
