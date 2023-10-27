package com.wegielek.signalychinese.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import com.wegielek.signalychinese.R;

public class HamburgerActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hamburger);

        Toolbar definitionToolbar = findViewById(R.id.definitionToolbar);
        setSupportActionBar(definitionToolbar);
        definitionToolbar.setTitleTextColor(getColor(R.color.dark_mode_white));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Settings");
        } else {
            Log.e(LOG_TAG, "Support action bar is null in onCreate");
        }

        Button button = findViewById(R.id.flashCardsBtn);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(getBaseContext(), FlashCardsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        Button button1 = findViewById(R.id.historyBtn);
        button1.setOnClickListener(v -> {
            Intent intent = new Intent(getBaseContext(), HistoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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