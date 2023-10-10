package com.wegielek.signalychinese.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.digitalink.RecognitionCandidate;
import com.wegielek.signalychinese.BR;
import com.wegielek.signalychinese.adapters.RadicalsParentAdapter;
import com.wegielek.signalychinese.database.Dictionary;
import com.wegielek.signalychinese.interfaces.CanvasViewListener;
import com.wegielek.signalychinese.interfaces.CharactersRecyclerViewListener;
import com.wegielek.signalychinese.interfaces.SearchTextBoxListener;
import com.wegielek.signalychinese.interfaces.RadicalsRecyclerViewListener;
import com.wegielek.signalychinese.interfaces.ResultsRecyclerViewListener;
import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.enums.StateUI;
import com.wegielek.signalychinese.adapters.SuggestedCharacterListAdapter;
import com.wegielek.signalychinese.adapters.SearchResultListAdapter;
import com.wegielek.signalychinese.databinding.ActivityMainBinding;
import com.wegielek.signalychinese.models.RadicalsParentModel;
import com.wegielek.signalychinese.utils.Utils;
import com.wegielek.signalychinese.viewmodels.MainViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements CanvasViewListener, CharactersRecyclerViewListener, ResultsRecyclerViewListener, RadicalsRecyclerViewListener, SearchTextBoxListener {
    private static final String LOG_TAG = "MainActivity";

    private SuggestedCharacterListAdapter mSuggestedCharacterListAdapter;
    private SearchResultListAdapter mSearchResultsListAdapter;
    private RadicalsParentAdapter mRadicalsParentAdapter;
    private Map<Integer, String[]> jsonRadicalsMap;
    private StateUI mStateUI = StateUI.DRAW;
    private ActivityMainBinding mBinding;
    private MainViewModel mMainViewModel;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mMainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mBinding.setVariable(BR.vm, mMainViewModel);
        mBinding.executePendingBindings();

        mMainViewModel.mDictionaryResultsList.observe(this, stringList ->
                mSearchResultsListAdapter.setData(stringList)
        );

        mMainViewModel.mCharactersList.observe(this, stringList ->
                mSuggestedCharacterListAdapter.setData(stringList)
        );

        mMainViewModel.mRadicalsList.observe(this, radicalsParentModels ->
                mRadicalsParentAdapter.setData(radicalsParentModels)
        );

        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mStateUI = (StateUI) savedInstanceState.getSerializable("state", StateUI.class);
            } else {
                mStateUI = (StateUI) savedInstanceState.getSerializable("state");
            }
        }
        setStateUI(mStateUI);


        mBinding.searchTextBox.requestFocus();
        mBinding.searchTextBox.setOnSelectionChangedListener(this);

        mBinding.resultsRv.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultsListAdapter = new SearchResultListAdapter(this, this);
        mBinding.resultsRv.setAdapter(mSearchResultsListAdapter);

        mBinding.radicalsRv.setLayoutManager(new LinearLayoutManager(this));
        mRadicalsParentAdapter = new RadicalsParentAdapter(this);
        mBinding.radicalsRv.setAdapter(mRadicalsParentAdapter);

        mBinding.charactersRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mSuggestedCharacterListAdapter = new SuggestedCharacterListAdapter(this);
        mBinding.charactersRv.setAdapter(mSuggestedCharacterListAdapter);

        mBinding.characterDrawCanvas.setOnRecognizeListener(this);
        mBinding.characterDrawCanvas.post(() -> mBinding.characterDrawCanvas.initialize(mBinding.characterDrawCanvas.getWidth(), mBinding.characterDrawCanvas.getHeight(), mMainViewModel));

        mBinding.undoBtn.setOnClickListener(view -> mBinding.characterDrawCanvas.undoStroke());
        mBinding.searchBtn.setOnClickListener(v -> performSearch());

        mBinding.puzzleBtn.setOnClickListener(view -> {
            mStateUI = StateUI.PUZZLE;
            setPuzzleUI();
        });

        mBinding.drawBtn.setOnClickListener(view -> {
            mStateUI = StateUI.DRAW;
            setDrawUI();
        });
        
        mBinding.backspaceBtn.setOnClickListener(view -> {
            mBinding.searchTextBox.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            mMainViewModel.setCursorPosition(mBinding.searchTextBox.getSelectionEnd());

            if (mStateUI == StateUI.RESULTS && mBinding.searchTextBox.getText().length() > 0) {
                performSearch();
            } else if (mStateUI == StateUI.RESULTS) {
                mBinding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_active, getTheme()));
                mBinding.resultsRv.setVisibility(View.INVISIBLE);
                mBinding.characterDrawCanvas.setVisibility(View.VISIBLE);
                mBinding.labelTv.setText(getString(R.string.drawing_mode));
                mStateUI = StateUI.DRAW;
            }

            mBinding.doneBtn.setVisibility(View.INVISIBLE);
            mBinding.characterDrawCanvas.clear();
            mBinding.undoBtn.setVisibility(View.INVISIBLE);
        });

        mBinding.doneBtn.setOnClickListener(view -> {
            mMainViewModel.clearCharacterList();
            mSuggestedCharacterListAdapter.notifyDataSetChanged();
            mBinding.doneBtn.setVisibility(View.INVISIBLE);
            mBinding.searchTextBox.setSelection(mBinding.searchTextBox.getText().length());
            mMainViewModel.setCursorPosition(mBinding.searchTextBox.getText().length());
            mBinding.characterDrawCanvas.clear();
            mBinding.undoBtn.setVisibility(View.INVISIBLE);
        });

        mBinding.settingsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        mBinding.cameraBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), CameraActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        mBinding.searchTextBox.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        //loadRadicals();
    }

    private void setStateUI(StateUI stateUI) {
        if (stateUI == StateUI.DRAW) {
            setDrawUI();
        } else if (stateUI == StateUI.RESULTS) {
            setResultsUI();
            mBinding.searchTextBox.postDelayed(this::performSearch, 1000);
        } else {
            setPuzzleUI();
        }
    }

    private void setDrawUI() {
        mBinding.labelTv.setText(getString(R.string.drawing_mode));
        mBinding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_active, getTheme()));
        mBinding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
        mBinding.charactersRv.setVisibility(View.VISIBLE);
        mBinding.characterDrawCanvas.setVisibility(View.VISIBLE);
        mBinding.resultsRv.setVisibility(View.INVISIBLE);
        mBinding.radicalsRv.setVisibility(View.INVISIBLE);
        mBinding.undoBtn.setVisibility(View.VISIBLE);
        mBinding.doneBtn.setVisibility(View.INVISIBLE);
    }

    private void setResultsUI() {
        mBinding.labelTv.setText(getString(R.string.searching_mode));
        mBinding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
        mBinding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
        mBinding.characterDrawCanvas.setVisibility(View.INVISIBLE);
        mBinding.charactersRv.setVisibility(View.INVISIBLE);
        mBinding.doneBtn.setVisibility(View.INVISIBLE);
        mBinding.resultsRv.setVisibility(View.VISIBLE);
        mBinding.undoBtn.setVisibility(View.INVISIBLE);
        mBinding.radicalsRv.setVisibility(View.INVISIBLE);
    }

    private void setPuzzleUI() {
        mBinding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_active, getTheme()));
        mBinding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
        mBinding.labelTv.setText(getString(R.string.puzzle_mode));
        mBinding.characterDrawCanvas.setVisibility(View.INVISIBLE);
        mBinding.resultsRv.setVisibility(View.INVISIBLE);
        mBinding.undoBtn.setVisibility(View.INVISIBLE);
        mBinding.radicalsRv.setVisibility(View.VISIBLE);
        mBinding.doneBtn.setVisibility(View.INVISIBLE);
    }

    private void loadRadicals() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String jsonRadicalsString;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("radicalsJSON.json"), StandardCharsets.UTF_8))) {
                jsonRadicalsString = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            jsonRadicalsMap = new Gson().fromJson(
                    jsonRadicalsString, new TypeToken<HashMap<Integer, String[]>>() {
                    }.getType()
            );

            List<RadicalsParentModel> radicalsParentModels = new ArrayList<>();
            for (int i = 0; i < jsonRadicalsMap.get(0).length - 1; i++) {
                radicalsParentModels.add(new RadicalsParentModel(jsonRadicalsMap.get(0)[i].split(" ")));
            }

            mMainViewModel.setRadicalsList(radicalsParentModels);

        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void performSearch() {
        String inputTextString;
        if (mBinding.searchTextBox.getText() != null) {
            inputTextString = mBinding.searchTextBox.getText().toString();
        } else {
            Log.e(LOG_TAG, "Search text box text is null");
            return;
        }

        if (inputTextString.length() > 0) {
            if (!Utils.containsChinese(inputTextString)) {
                if (inputTextString.charAt(inputTextString.length() - 1) == ' ') {
                    inputTextString = inputTextString.substring(0, inputTextString.length() - 1);
                }
                mMainViewModel.searchByWordPL(inputTextString).observe(this, dictionaries -> {
                    List<String> searchResults = new ArrayList<>();
                    for (Dictionary dictionary : dictionaries) {
                        searchResults.add(dictionary.traditionalSign + "/" + dictionary.simplifiedSign + "/" + dictionary.pronunciation + "/" + dictionary.translation);
                    }
                    searchSwitch(searchResults);
                });
            } else {
                if (inputTextString.length() == 1) {
                    mMainViewModel.searchSingleCH(inputTextString).observe(this, dictionaries -> {
                        List<String> searchResults = new ArrayList<>();
                        for (Dictionary dictionary : dictionaries) {
                            searchResults.add(dictionary.traditionalSign + "/" + dictionary.simplifiedSign + "/" + dictionary.pronunciation + "/" + dictionary.translation);
                        }
                        searchSwitch(searchResults);
                    });
                } else {
                    mMainViewModel.searchByWordCH(inputTextString).observe(this, dictionaries -> {
                        List<String> searchResults = new ArrayList<>();
                        for (Dictionary dictionary : dictionaries) {
                            searchResults.add(dictionary.traditionalSign + "/" + dictionary.simplifiedSign + "/" + dictionary.pronunciation + "/" + dictionary.translation);
                        }
                        searchSwitch(searchResults);
                    });
                }
            }
        }
    }

    private void searchSwitch(List<String> searchResults) {
        if (searchResults.size() > 0) {
            mMainViewModel.updateResults(searchResults);

            mStateUI = StateUI.RESULTS;
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(mBinding.searchTextBox.getWindowToken(), 0);
            }
            /*
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
            binding.characterDrawCanvas.clear();
            binding.undoBtn.setVisibility(View.INVISIBLE);
            binding.characterDrawCanvas.setVisibility(View.INVISIBLE);
            binding.charactersRv.setVisibility(View.INVISIBLE);
            binding.resultsRv.setVisibility(View.VISIBLE);
            binding.labelTv.setText(getString(R.string.searching_mode));
            binding.doneBtn.setVisibility(View.INVISIBLE);
            binding.radicalsRv.setVisibility(View.INVISIBLE);
             */
            mBinding.searchTextBox.setSelection(mBinding.searchTextBox.length());
            setResultsUI();

        } else if (mStateUI != StateUI.RESULTS) {
            Toast.makeText(this, getString(R.string.no_results), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResults(List<RecognitionCandidate> recognitionCandidatesList) {
        mMainViewModel.clearCharacterList();
        for (RecognitionCandidate rc: recognitionCandidatesList) {
            mMainViewModel.addToCharacterList(rc.getText());
        }
        mSuggestedCharacterListAdapter.notifyDataSetChanged();

        mBinding.searchTextBox.setText(mBinding.searchTextBox.getText()
                .subSequence(0, mMainViewModel.getCursorPosition())
                + recognitionCandidatesList.get(0).getText());
        mBinding.searchTextBox.setSelection(mBinding.searchTextBox.getText().length());

        mBinding.doneBtn.setVisibility(View.VISIBLE);
        mBinding.charactersRv.setVisibility(View.VISIBLE);
        mBinding.undoBtn.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemReleased(int position) {
        mBinding.searchTextBox.setText(mBinding.searchTextBox.getText()
                .subSequence(0, mMainViewModel.getCursorPosition())
                + mMainViewModel.mCharactersList.getValue().get(position));
        mBinding.searchTextBox.setSelection(mBinding.searchTextBox.getText().length());
        mMainViewModel.setCursorPosition(mBinding.searchTextBox.getText().length());
        mMainViewModel.clearCharacterList();
        mSuggestedCharacterListAdapter.notifyDataSetChanged();
        mBinding.doneBtn.setVisibility(View.INVISIBLE);
        mBinding.undoBtn.setVisibility(View.INVISIBLE);
        mBinding.characterDrawCanvas.clear();
    }

    @Override
    public void onItemPressed(View itemView) {
        itemView.setBackgroundColor(Color.parseColor("#0055CC"));
        itemView.postDelayed(() -> itemView.setBackgroundColor(Color.TRANSPARENT), 500);
    }

    @Override
    public void onItemCanceled(View itemView) {
        itemView.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onResultClicked(int position) {
        Intent intent = new Intent(getBaseContext(), DefinitionWordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("word", mMainViewModel.getResult(position));
        startActivity(intent);
    }

    @Override
    public void onRadicalClicked(String radical) {

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mBinding.searchTextBox.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(mBinding.searchTextBox.getWindowToken(), 0);
                } else {
                    mBinding.searchTextBox.postDelayed(this, 10);
                }
            }
        }, 50);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable("state", mStateUI);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSelectionChanged(int selStart, int selEnd) {

    }
}