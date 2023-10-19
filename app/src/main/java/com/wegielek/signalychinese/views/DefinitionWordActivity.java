package com.wegielek.signalychinese.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.databinding.ActivityDefinitionWordBinding;
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel;

import java.util.Locale;


public class DefinitionWordActivity extends AppCompatActivity {

    private static final String LOG_TAG = "DefinitionWordActivity";

    private ActivityDefinitionWordBinding mBinding;
    private TextToSpeech mTts;
    public DefinitionViewModel mDefinitionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_definition_word);

        mDefinitionViewModel = new ViewModelProvider(this).get(DefinitionViewModel.class);
        mBinding.executePendingBindings();

        String word = getIntent().getStringExtra("word");

        Toolbar myToolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (word != null) {
                getSupportActionBar().setTitle(word.split("/")[1]);
                mDefinitionViewModel.setWord(word);
            } else {
                Log.e(LOG_TAG, "Word definition is null in onCreate");
            }
        } else {
            Log.e(LOG_TAG, "Support action bar is null in onCreate");
        }

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
            if (word != null) {
                mTts.speak(word.split("/")[0], TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                Log.e(LOG_TAG, "Word definition is null in speakBtn.onClick");
            }
        });

        NavHostFragment dictionaryWordNavHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.dictionaryWordNavHostFragment);
        if (dictionaryWordNavHostFragment != null) {
            mBinding.bottomNavigationView.setOnItemSelectedListener(item -> {
                NavOptions.Builder navOptions = new NavOptions.Builder().setLaunchSingleTop(true);
                if (item.getItemId() == R.id.strokesFragment) {
                    navOptions.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left);
                } else {
                    navOptions.setEnterAnim(R.anim.slide_in_left).setExitAnim(R.anim.slide_out_right);
                }
                dictionaryWordNavHostFragment.getNavController().navigate(item.getItemId(), null, navOptions.build());
                return true;
            });
        }

        if (word != null) {
            mBinding.definitionCharactersTv.setText(getString(R.string.result_text_placeholder_1, word.split("/")[0], word.split("/")[1]));
            mBinding.definitionPronunciationTv.setText(word.split("/")[2]);
        } else {
            Log.e(LOG_TAG, "Word definition is null in onCreate");
        }
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