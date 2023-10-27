package com.wegielek.signalychinese.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.interfaces.DefinitionListRecyclerViewListener;
import com.wegielek.signalychinese.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DefinitionListAdapter extends RecyclerView.Adapter<DefinitionListAdapter.ViewHolder> {

    private final Context context;
    private final List<String> dataList;
    private final DefinitionListRecyclerViewListener listener;

    public DefinitionListAdapter(Context context, DefinitionListRecyclerViewListener listener) {
        this.context = context;
        this.dataList = new ArrayList<>();
        this.listener = listener;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_definition_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.definitionRowTv.setText(context.getString(R.string.definition_list_item_placeholder, position + 1, dataList.get(position)));
        holder.moreBtn.setOnClickListener(v -> {
            listener.speak(v, dataList.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView definitionRowTv;

        private final AppCompatImageButton moreBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            definitionRowTv = itemView.findViewById(R.id.definitionRowTv);

        }
    }

}
