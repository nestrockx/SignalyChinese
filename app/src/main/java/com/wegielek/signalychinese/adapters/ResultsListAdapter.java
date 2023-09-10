package com.wegielek.signalychinese.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wegielek.signalychinese.Interfaces.ResultsRecyclerViewListener;
import com.wegielek.signalychinese.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResultsListAdapter extends RecyclerView.Adapter<ResultsListAdapter.ViewHolder> {

    private final Context context;
    private final List<String> dataList;
    private final ResultsRecyclerViewListener resultsRecyclerViewListener;

    public ResultsListAdapter(ResultsRecyclerViewListener resultsRecyclerViewListener, Context context) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String inputString = dataList.get(position);


        List<String> list = Arrays.asList(inputString.split("/"));

        StringBuilder translation = new StringBuilder();
        for (int i = 3; i < list.size(); i++) {
            if (i != list.size() - 1) {
                translation.append(list.get(i)).append("; ");
            } else {
                translation.append(list.get(i)).append(";");
            }
        }

        holder.textView.setText(context.getString(R.string.result_text_placeholder_1, list.get(0), list.get(1)));
        holder.textView2.setText(list.get(2));
        holder.textView3.setText(translation.toString());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView textView2;
        TextView textView3;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.labelTv);
            textView3 = itemView.findViewById(R.id.translationTv);
            textView2 = itemView.findViewById(R.id.pronunciationTv);

            itemView.setOnClickListener(view -> resultsRecyclerViewListener.onResultClicked(getAdapterPosition()));
        }
    }
}

