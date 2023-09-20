package com.wegielek.signalychinese.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.interfaces.RadicalsRecyclerViewListener;

public class RadicalsChildAdapter extends RecyclerView.Adapter<RadicalsChildAdapter.ViewHolder> {

    private String[] dataList;
    private RadicalsRecyclerViewListener radicalsRecyclerViewListener;

    public RadicalsChildAdapter(String[] dataList, RadicalsRecyclerViewListener radicalsRecyclerViewListener) {
        this.dataList = dataList;
        this.radicalsRecyclerViewListener = radicalsRecyclerViewListener;
    }

    @NonNull
    @Override
    public RadicalsChildAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.radicals_child_rv, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RadicalsChildAdapter.ViewHolder holder, int position) {
        holder.textView.setText(dataList[position]);

    }

    @Override
    public int getItemCount() {
        return dataList.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.characterTv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    radicalsRecyclerViewListener.onRadicalClicked(dataList[getAdapterPosition()]);
                }
            });
        }
    }
}

