package com.wegielek.signalychinese.adapters;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wegielek.signalychinese.interfaces.CharactersRecyclerViewListener;
import com.wegielek.signalychinese.R;

import java.util.ArrayList;
import java.util.List;

public class SuggestedCharacterListAdapter extends RecyclerView.Adapter<SuggestedCharacterListAdapter.ViewHolder> {

    private final CharactersRecyclerViewListener charactersRecyclerViewListener;
    private final List<String> dataList;

    public SuggestedCharacterListAdapter(CharactersRecyclerViewListener charactersRecyclerViewListener) {
        this.charactersRecyclerViewListener = charactersRecyclerViewListener;
        this.dataList = new ArrayList<>();
    }

    public void setData(List<String> dataList) {
        this.dataList.clear();
        this.dataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewItem);

            itemView.setOnTouchListener((view, motionEvent) -> {
                if (charactersRecyclerViewListener != null) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        charactersRecyclerViewListener.onItemPressed(itemView);
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        view.performClick();
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            charactersRecyclerViewListener.onItemReleased(pos);
                        }
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                        charactersRecyclerViewListener.onItemCanceled(itemView);
                    }
                }
                return true;
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggested_character_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
