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

import java.util.ArrayList;
import java.util.List;

public class DefinitionListAdapter extends RecyclerView.Adapter<DefinitionListAdapter.ViewHolder> {

    private final Context context;
    private final List<String> dataList;

    public DefinitionListAdapter(Context context) {
        this.context = context;
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_definition_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.definitionRowTv.setText(context.getString(R.string.definition_list_item_placeholder, position + 1, dataList.get(position)));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView definitionRowTv;

        public ViewHolder(View itemView) {
            super(itemView);
            definitionRowTv = itemView.findViewById(R.id.definitionRowTv);

        }
    }

}
