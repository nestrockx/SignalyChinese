package com.wegielek.signalychinese.views;

import static com.wegielek.signalychinese.utils.Utils.copyToClipboard;
import static com.wegielek.signalychinese.utils.Utils.showPopup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.navigation.ui.NavigationUI;

import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.databinding.ActivityDefinitionWordBinding;
import com.wegielek.signalychinese.utils.Preferences;
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel;

import java.util.Locale;


public class DefinitionWordActivity extends AppCompatActivity {

    private static final String LOG_TAG = "DefinitionWordActivity";

    private boolean added = false;

    private ActivityDefinitionWordBinding mBinding;
    public TextToSpeech mTtsChinese;
    public TextToSpeech mTtsPolish;
    public DefinitionViewModel mDefinitionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_definition_word);

        mDefinitionViewModel = new ViewModelProvider(this).get(DefinitionViewModel.class);
        mBinding.executePendingBindings();

        String word = getIntent().getStringExtra("word");

        Toolbar definitionToolbar = mBinding.definitionToolbar;
        setSupportActionBar(definitionToolbar);
        definitionToolbar.setTitleTextColor(getColor(R.color.dark_mode_white));
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


        mTtsChinese = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int langResult = mTtsChinese.isLanguageAvailable(new Locale("zh_CN"));
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language not supported");
                } else {
                    mTtsChinese.setLanguage(new Locale("zh_CN"));
                }
            } else {
                Log.e("TextToSpeech", "Initialization failed");
            }
        });

        mTtsPolish = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int langResult = mTtsPolish.isLanguageAvailable(new Locale("pl_PL"));
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language not supported");
                } else {
                    mTtsPolish.setLanguage(new Locale("pl_PL"));
                }
            } else {
                Log.e("TextToSpeech", "Initialization failed");
            }
        });


        mBinding.speakBtn.setOnClickListener(v -> {
            /*
            Intent intent = new Intent();
            intent.setAction("com.android.settings.TTS_SETTINGS");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
             */

            if (!Preferences.getTtsAlertShow(getApplicationContext(), "tts_dialog_dont_show_again")) {
                View dialogTtsView = LayoutInflater.from(DefinitionWordActivity.this).inflate(R.layout.alert_dialog_tts, null);
                AlertDialog.Builder dialogTtsBuilder = new AlertDialog.Builder(DefinitionWordActivity.this);
                dialogTtsBuilder.setView(dialogTtsView);
                final AlertDialog dialogTts = dialogTtsBuilder.create();
                if (dialogTts.getWindow() != null) {
                    dialogTts.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                } else {
                    Log.e(LOG_TAG, "Dialog getWindow() is null");
                }
                dialogTts.show();

                AppCompatImageButton dialogPronunciationImageButton = dialogTtsView.findViewById(R.id.dialogPronunciationBtn);
                dialogPronunciationImageButton.setOnClickListener(v1 -> {
                    if (word != null) {
                        speak(word.split("/")[1]);
                    } else {
                        Log.e(LOG_TAG, "Word definition is null in speakBtn.onClick");
                    }
                });

                AppCompatButton dialogSettingsBtn = dialogTtsView.findViewById(R.id.dialogSettingsBtn);
                dialogSettingsBtn.setOnClickListener(v12 -> {
                    Intent installIntent = new Intent();
                    installIntent.setAction(
                            TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                });

                AppCompatImageButton dialogCloseBtn = dialogTtsView.findViewById(R.id.dialogCloseBtn);
                dialogCloseBtn.setOnClickListener(v13 -> dialogTts.hide());

                AppCompatButton dialogDontShowAgainBtn = dialogTtsView.findViewById(R.id.dialogDontShowAgainBtn);
                dialogDontShowAgainBtn.setOnClickListener(v14 -> {
                    Preferences.setTtsAlertShow(getApplicationContext(), "tts_dialog_dont_show_again", true);
                    dialogTts.hide();
                });
            }

            if (word != null) {
                speak(word.split("/")[1]);
            } else {
                Log.e(LOG_TAG, "Word definition is null in speakBtn.onClick");
            }
        });

        NavHostFragment dictionaryWordNavHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.dictionaryWordNavHostFragment);
        if (dictionaryWordNavHostFragment != null) {
            mBinding.bottomNavigationView.setOnItemSelectedListener(item -> {
                NavigationUI.onNavDestinationSelected(item, dictionaryWordNavHostFragment.getNavController());
                return true;
            });
        }

        if (word != null) {
            mBinding.definitionCharactersTv.setText(word.split("/")[0]);
            mBinding.definitionCharactersSimplifiedTv.setText("(" + word.split("/")[1] + ")");
            mBinding.definitionPronunciationTv.setText(word.split("/")[2]);
        } else {
            Log.e(LOG_TAG, "Word definition is null in onCreate");
        }


        mBinding.definitionCharactersTv.setOnClickListener(v
                -> showPopup(v, mBinding.definitionCharactersTv.getText().toString(), "zh-TW", "en", mTtsChinese)
        );

        mBinding.definitionCharactersSimplifiedTv.setOnClickListener(v
                -> showPopup(v, mBinding.definitionCharactersSimplifiedTv.getText().toString().substring(1, mBinding.definitionCharactersSimplifiedTv.getText().toString().length() - 1), "zh-CN", "en", mTtsChinese)
        );

        mBinding.definitionPronunciationTv.setOnClickListener(v ->
                showPopup(v, mBinding.definitionPronunciationTv.getText().toString() + "/" + word.split("/")[3].substring(0, word.split("/")[3].length() - 1), "zh-CN", "en", mTtsChinese)
        );

    }

    private void speak(String text) {
        mTtsChinese.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.definition_word_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.copy) {
            if (copyToClipboard(this, mBinding.definitionCharactersSimplifiedTv.getText().toString().substring(1, mBinding.definitionCharactersSimplifiedTv.getText().toString().length() - 1))) {
                Toast.makeText(this, "Successfully copied", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == R.id.add) {
            if (!added) {
                item.setIcon(R.drawable.ic_note_add_active);
                added = true;
            } else {
                item.setIcon(R.drawable.ic_note_add_default);
                added = false;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}