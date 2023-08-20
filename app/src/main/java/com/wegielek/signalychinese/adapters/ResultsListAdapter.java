package com.wegielek.signalychinese.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.wegielek.signalychinese.Interfaces.CharactersRecyclerViewListener;
import com.wegielek.signalychinese.Interfaces.ResultsRecyclerViewListener;
import com.wegielek.signalychinese.R;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsListAdapter extends RecyclerView.Adapter<ResultsListAdapter.ViewHolder> {

    private List<String> dataList;
    private final ResultsRecyclerViewListener resultsRecyclerViewListener;

    public ResultsListAdapter(List<String> dataList, ResultsRecyclerViewListener resultsRecyclerViewListener) {
        this.dataList = dataList;
        this.resultsRecyclerViewListener = resultsRecyclerViewListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String inputString = dataList.get(position);
        Pattern pattern = Pattern.compile("(.+) (.+) \\[(.+)\\] /(.+)/");
        Matcher matcher = pattern.matcher(inputString);
        if(matcher.find()) {
            String x = matcher.group(1);
            String y = matcher.group(2);
            String z = matcher.group(3);
            String a = matcher.group(4);

            holder.textView.setText(x.concat(" (").concat(y).concat(")"));
            holder.textView3.setText(z);
            holder.textView2.setText(a);
        }
        else {
            holder.textView.setText("Data in wrong format");
        }
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
            textView = itemView.findViewById(R.id.label);
            textView2 = itemView.findViewById(R.id.textView2);
            textView3 = itemView.findViewById(R.id.textView3);

            itemView.setOnClickListener(view -> resultsRecyclerViewListener.onResultClicked(getAdapterPosition()));
        }
    }
}

