package com.wegielek.signalychinese.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.interfaces.RadicalsRecyclerViewListener;
import com.wegielek.signalychinese.models.RadicalsParentModel;

import java.util.List;

public class RadicalsParentAdapter extends RecyclerView.Adapter<RadicalsParentAdapter.ViewHolder> {

    private String number;
    private List<RadicalsParentModel> parentList;
    private RadicalsRecyclerViewListener radicalsRecyclerViewListener;

    public RadicalsParentAdapter(List<RadicalsParentModel> parentList, RadicalsRecyclerViewListener radicalsRecyclerViewListener) {
        this.parentList = parentList;
        this.radicalsRecyclerViewListener = radicalsRecyclerViewListener;
    }

    @NonNull
    @Override
    public RadicalsParentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.radicals_parent_rv, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RadicalsParentAdapter.ViewHolder holder, int position) {

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(holder.itemView.getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);

        RadicalsChildAdapter radicalsChildAdapter;
        radicalsChildAdapter = new RadicalsChildAdapter(parentList.get(position).radicalsList, radicalsRecyclerViewListener);
        holder.rv_child.setLayoutManager(layoutManager);
        holder.rv_child.setAdapter(radicalsChildAdapter);
        holder.radicalNumber.setText(Integer.toString(position + 1));
        radicalsChildAdapter.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return parentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView radicalNumber;
        RecyclerView rv_child;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            radicalNumber = itemView.findViewById(R.id.radicalNumber);
            rv_child = itemView.findViewById(R.id.radicalsChildRv);
        }
    }
}

