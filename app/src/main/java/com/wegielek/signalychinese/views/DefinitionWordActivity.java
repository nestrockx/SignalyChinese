package com.wegielek.signalychinese.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.databinding.ActivityDefinitionWordBinding;
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel;

import java.util.Locale;


public class DefinitionWordActivity extends AppCompatActivity {

    private ActivityDefinitionWordBinding mBinding;
    private TextToSpeech mTts;
    public DefinitionViewModel mDefinitionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_definition_word);

        mDefinitionViewModel = new ViewModelProvider(this).get(DefinitionViewModel.class);
        mBinding.executePendingBindings();

        Toolbar myToolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String word = getIntent().getStringExtra("word");
        mDefinitionViewModel.setWord(word);

        getSupportActionBar().setTitle(word.split("/")[1]);


        mTts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int langResult = mTts.isLanguageAvailable(new Locale("zh_CN"));

                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language not supported");
                } else {
                    mTts.setLanguage(new Locale("zh_CN"));
                }
            } else {
                Log.e("TextToSpeech", "Initialization failed");
            }
        });


        mBinding.speakBtn.setOnClickListener(v -> {
            mTts.speak(word.split("/")[0], TextToSpeech.QUEUE_FLUSH, null, null);
        });


        NavHostFragment dictionaryWordNavHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.dictionaryWordNavHostFragment);
        if (dictionaryWordNavHostFragment != null) {
            mBinding.bottomNavigationView.setOnItemSelectedListener(item -> {
                NavigationUI.onNavDestinationSelected(item, dictionaryWordNavHostFragment.getNavController());
                return true;
            });
        }

        mBinding.definitionCharactersTv.setText(getString(R.string.result_text_placeholder_1, word.split("/")[0], word.split("/")[1]));
        mBinding.definitionPronunciationTv.setText(word.split("/")[2]);





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