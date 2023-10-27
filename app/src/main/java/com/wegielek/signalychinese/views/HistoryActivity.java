package com.wegielek.signalychinese.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.adapters.HistoryAdapter;
import com.wegielek.signalychinese.database.SearchHistory;
import com.wegielek.signalychinese.databinding.ActivityHistoryBinding;
import com.wegielek.signalychinese.interfaces.HistoryRecyclerViewListener;
import com.wegielek.signalychinese.viewmodels.HistoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements HistoryRecyclerViewListener {

    private ActivityHistoryBinding mBinding;
    private HistoryAdapter mHistoryAdapter;
    private HistoryViewModel mHistoryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_history);

        mHistoryViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        Toolbar historyToolbar = mBinding.historyToolbar;
        setSupportActionBar(historyToolbar);
        historyToolbar.setTitleTextColor(getColor(R.color.dark_mode_white));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("History");
        }

        mBinding.historyRv.setLayoutManager(new LinearLayoutManager(this));
        mHistoryAdapter = new HistoryAdapter(this, this);
        mBinding.historyRv.setAdapter(mHistoryAdapter);


        mHistoryViewModel.getWholeHistory().observe(this, searchHistories -> {
            List<String> searchHistoryList = new ArrayList<>();
            for (SearchHistory searchHistory : searchHistories) {
                searchHistoryList.add(searchHistory.time + "/" + searchHistory.traditionalSign + "/" + searchHistory.simplifiedSign
                        + "/" + searchHistory.pronunciation + "/" + searchHistory.pronunciationPhonetic
                        + "/" + searchHistory.translation);
            }
            mHistoryAdapter.setData(searchHistoryList);
        });

    }

    @Override
    public void onHistoryClicked(String word) {
        Intent intent = new Intent(getBaseContext(), DefinitionWordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("word", word);
        startActivity(intent);
    }

    @Override
    public void onLongHistoryClicked(String timestamp) {
        Toast.makeText(this, timestamp, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}