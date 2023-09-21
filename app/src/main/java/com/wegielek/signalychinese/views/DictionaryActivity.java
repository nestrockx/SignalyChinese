package com.wegielek.signalychinese.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wegielek.signalychinese.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DictionaryActivity extends AppCompatActivity {

    private Map<String, String> jsonTraditionalMap;
    private Map<String, String> jsonSimplifiedMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitleTextColor(getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String word = getIntent().getStringExtra("word");

        getSupportActionBar().setTitle(word.split("/")[0] + " (" + word.split("/")[1]+ ")");

        loadDictionaryFiles();
    }

    private void loadDictionaryFiles() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String jsonTraditionalString;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("resultsJSON1.json"), StandardCharsets.UTF_8))) {
                jsonTraditionalString = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            jsonTraditionalMap = new Gson().fromJson(
                    jsonTraditionalString, new TypeToken<HashMap<String, String>>() {}.getType()
            );

            String jsonSimplifiedString;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("resultsJSON2.json"), StandardCharsets.UTF_8))) {
                jsonSimplifiedString = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            jsonSimplifiedMap = new Gson().fromJson(
                    jsonSimplifiedString, new TypeToken<HashMap<String, String>>() {}.getType()
            );
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}