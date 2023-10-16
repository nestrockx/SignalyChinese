package com.wegielek.signalychinese.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wegielek.signalychinese.interfaces.ResultsRecyclerViewListener;
import com.wegielek.signalychinese.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchResultListAdapter extends RecyclerView.Adapter<SearchResultListAdapter.ViewHolder> {

    private final Context context;
    private final List<String> dataList;
    private final ResultsRecyclerViewListener resultsRecyclerViewListener;

    public SearchResultListAdapter(ResultsRecyclerViewListener resultsRecyclerViewListener, Context context) {
        this.context = context;
        this.resultsRecyclerViewListener = resultsRecyclerViewListener;
        this.dataList = new ArrayList<>();
    }

    public void setData(List<String> dataList) {
        this.dataList.clear();
        this.dataList.addAll(dataList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String inputString = dataList.get(position);

        List<String> list = Arrays.asList(inputString.split("/"));
        StringBuilder translation = new StringBuilder();
        for (int i = 4; i < list.size(); i++) {
            if (i != list.size() - 1) {
                translation.append((i - 3)).append(".").append(list.get(i)).append(" ");
            } else {
                translation.append((i - 3)).append(".").append(list.get(i)).append("");
            }
        }

        if (!list.get(0).equals(list.get(1))) {
            holder.charactersTv.setText(context.getString(R.string.result_text_placeholder_1, list.get(0), list.get(1)));
        } else {
            holder.charactersTv.setText(list.get(0));
        }
        holder.pronunciationTv.setText(list.get(2));
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
            itemView.setOnClickListener(view -> resultsRecyclerViewListener.onResultClicked(getAdapterPosition()));
        }
    }
}

