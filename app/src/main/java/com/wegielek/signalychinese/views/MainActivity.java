package com.wegielek.signalychinese.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.wegielek.signalychinese.enums.State;
import com.wegielek.signalychinese.adapters.CharacterListAdapter;
import com.wegielek.signalychinese.adapters.ResultsListAdapter;
import com.wegielek.signalychinese.databinding.ActivityMainBinding;
import com.wegielek.signalychinese.models.RadicalsParentModel;
import com.wegielek.signalychinese.repository.DictionaryRepository;
import com.wegielek.signalychinese.viewmodels.MainViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements CanvasViewListener, CharactersRecyclerViewListener, ResultsRecyclerViewListener, RadicalsRecyclerViewListener, SearchTextBoxListener {
    private CharacterListAdapter mCharacterListAdapter;
    private ResultsListAdapter mResultsListAdapter;
    private RadicalsParentAdapter mRadicalsParentAdapter;
    private Map<Integer, String[]> jsonRadicalsMap;
    private State mState = State.DRAW;
    private ActivityMainBinding binding;
    private MainViewModel mMainViewModel;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mMainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding.setVariable(BR.vm, mMainViewModel);
        binding.executePendingBindings();

        mMainViewModel.dictionaryResultsList.observe(this, stringList ->
                mResultsListAdapter.setData(stringList)
        );

        mMainViewModel.charactersList.observe(this, stringList ->
                mCharacterListAdapter.setData(stringList)
        );

        mMainViewModel.radicalsList.observe(this, radicalsParentModels ->
                mRadicalsParentAdapter.setData(radicalsParentModels)
        );

        if (savedInstanceState != null) {
            mState = (State) savedInstanceState.getSerializable("state");
        }
        setStateUI(mState);


        binding.searchTextBox.requestFocus();
        binding.searchTextBox.setOnSelectionChangedListener(this);

        binding.resultsRv.setLayoutManager(new LinearLayoutManager(this));
        mResultsListAdapter = new ResultsListAdapter(this, this);
        binding.resultsRv.setAdapter(mResultsListAdapter);

        binding.radicalsRv.setLayoutManager(new LinearLayoutManager(this));
        mRadicalsParentAdapter = new RadicalsParentAdapter(this);
        binding.radicalsRv.setAdapter(mRadicalsParentAdapter);

        binding.charactersRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mCharacterListAdapter = new CharacterListAdapter(this);
        binding.charactersRv.setAdapter(mCharacterListAdapter);

        binding.characterDrawCanvas.setOnRecognizeListener(this);
        binding.characterDrawCanvas.post(() -> binding.characterDrawCanvas.initialize(binding.characterDrawCanvas.getWidth(), binding.characterDrawCanvas.getHeight(), mMainViewModel));

        binding.undoBtn.setOnClickListener(view -> binding.characterDrawCanvas.undoStroke());
        binding.searchBtn.setOnClickListener(v -> performSearch());

        binding.puzzleBtn.setOnClickListener(view -> {
            mState = State.PUZZLE;
            binding.labelTv.setText(getString(R.string.puzzle_mode));
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_active, getTheme()));
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
            binding.characterDrawCanvas.setVisibility(View.INVISIBLE);
            binding.resultsRv.setVisibility(View.INVISIBLE);
            binding.radicalsRv.setVisibility(View.VISIBLE);
            binding.charactersRv.setVisibility(View.INVISIBLE);
            binding.doneBtn.setVisibility(View.INVISIBLE);
            binding.undoBtn.setVisibility(View.INVISIBLE);
        });

        binding.drawBtn.setOnClickListener(view -> {
            mState = State.DRAW;
            binding.labelTv.setText(getString(R.string.drawing_mode));
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_active, getTheme()));
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
            binding.resultsRv.setVisibility(View.INVISIBLE);
            binding.characterDrawCanvas.setVisibility(View.VISIBLE);
            binding.undoBtn.setVisibility(View.VISIBLE);
            binding.radicalsRv.setVisibility(View.INVISIBLE);
            binding.charactersRv.setVisibility(View.VISIBLE);
        });
        
        binding.backspaceBtn.setOnClickListener(view -> {
            binding.searchTextBox.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            mMainViewModel.setCursorPosition(binding.searchTextBox.getSelectionEnd());

            if (mState == State.RESULTS && binding.searchTextBox.getText().length() > 0) {
                performSearch();
            } else if (mState == State.RESULTS) {
                binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_active, getTheme()));
                binding.resultsRv.setVisibility(View.INVISIBLE);
                binding.characterDrawCanvas.setVisibility(View.VISIBLE);
                binding.labelTv.setText(getString(R.string.drawing_mode));
                mState = State.DRAW;
            }

            binding.doneBtn.setVisibility(View.INVISIBLE);
            binding.characterDrawCanvas.clear();
            binding.undoBtn.setVisibility(View.INVISIBLE);
        });

        binding.doneBtn.setOnClickListener(view -> {
            mMainViewModel.clearCharacterList();
            mCharacterListAdapter.notifyDataSetChanged();
            binding.doneBtn.setVisibility(View.INVISIBLE);
            binding.searchTextBox.setSelection(binding.searchTextBox.getText().length());
            mMainViewModel.setCursorPosition(binding.searchTextBox.getText().length());
            binding.characterDrawCanvas.clear();
            binding.undoBtn.setVisibility(View.INVISIBLE);
        });

        binding.settingsBtn.setOnClickListener(view -> {
            //TODO
        });

        binding.searchTextBox.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        //loadRadicals();
    }

    private void setStateUI(State state) {
        if (state == State.DRAW) {
            binding.labelTv.setText(getString(R.string.drawing_mode));
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_active, getTheme()));
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
            binding.charactersRv.setVisibility(View.VISIBLE);
            binding.characterDrawCanvas.setVisibility(View.VISIBLE);
            binding.resultsRv.setVisibility(View.INVISIBLE);
            binding.radicalsRv.setVisibility(View.INVISIBLE);
            binding.undoBtn.setVisibility(View.VISIBLE);
            binding.doneBtn.setVisibility(View.INVISIBLE);
        } else if (state == State.RESULTS) {
            binding.labelTv.setText(getString(R.string.searching_mode));
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
            binding.characterDrawCanvas.setVisibility(View.INVISIBLE);
            binding.charactersRv.setVisibility(View.INVISIBLE);
            binding.doneBtn.setVisibility(View.INVISIBLE);
            binding.resultsRv.setVisibility(View.VISIBLE);
            binding.undoBtn.setVisibility(View.INVISIBLE);
            binding.radicalsRv.setVisibility(View.INVISIBLE);
            binding.searchTextBox.postDelayed(() -> {
                   performSearch();
            }, 1000);
        } else {
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_active, getTheme()));
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
            binding.labelTv.setText(getString(R.string.puzzle_mode));
            binding.characterDrawCanvas.setVisibility(View.INVISIBLE);
            binding.resultsRv.setVisibility(View.INVISIBLE);
            binding.undoBtn.setVisibility(View.INVISIBLE);
            binding.radicalsRv.setVisibility(View.VISIBLE);
            binding.doneBtn.setVisibility(View.INVISIBLE);
        }


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
        String inputTextString0 = Objects.requireNonNull(binding.searchTextBox.getText()).toString();
        mMainViewModel.searchByWord(inputTextString0).observe(this, new Observer<List<Dictionary>>() {
            @Override
            public void onChanged(List<Dictionary> dictionaries) {
                List<String> searchResults = new ArrayList<>();
                for (Dictionary dictionary: dictionaries) {
                    searchResults.add(dictionary.traditionalSign + "/" + dictionary.simplifiedSign + "/" + dictionary.pronunciation + "/" + dictionary.translation);
                }
                searchSwitch(searchResults);
                //Toast.makeText(getApplicationContext(), dictionaries.get(0).translation, Toast.LENGTH_LONG).show();
            }
        });




    }

    private void searchSwitch(List<String> searchResults) {
        if (searchResults.size() > 0) {
            mMainViewModel.updateResults(searchResults);

            mState = State.RESULTS;
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(binding.searchTextBox.getWindowToken(), 0);
            }
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));

            binding.searchTextBox.setSelection(binding.searchTextBox.length());
            binding.characterDrawCanvas.clear();
            binding.undoBtn.setVisibility(View.INVISIBLE);
            binding.characterDrawCanvas.setVisibility(View.INVISIBLE);
            binding.charactersRv.setVisibility(View.INVISIBLE);
            binding.resultsRv.setVisibility(View.VISIBLE);
            binding.labelTv.setText(getString(R.string.searching_mode));
            binding.doneBtn.setVisibility(View.INVISIBLE);
            binding.radicalsRv.setVisibility(View.INVISIBLE);
        } else {
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
        mCharacterListAdapter.notifyDataSetChanged();

        binding.searchTextBox.setText(binding.searchTextBox.getText().subSequence(0, mMainViewModel.getCursorPosition()) + recognitionCandidatesList.get(0).getText());
        binding.searchTextBox.setSelection(binding.searchTextBox.getText().length());

        binding.doneBtn.setVisibility(View.VISIBLE);
        binding.charactersRv.setVisibility(View.VISIBLE);
        binding.undoBtn.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemReleased(int position) {
        binding.searchTextBox.setText(binding.searchTextBox.getText().subSequence(0, mMainViewModel.getCursorPosition()) + mMainViewModel.charactersList.getValue().get(position));
        binding.searchTextBox.setSelection(binding.searchTextBox.getText().length());
        mMainViewModel.setCursorPosition(binding.searchTextBox.getText().length());
        mMainViewModel.clearCharacterList();
        mCharacterListAdapter.notifyDataSetChanged();
        binding.doneBtn.setVisibility(View.INVISIBLE);
        binding.undoBtn.setVisibility(View.INVISIBLE);
        binding.characterDrawCanvas.clear();
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
        Intent intent = new Intent(getBaseContext(), DictionaryActivity.class);
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
        binding.searchTextBox.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(binding.searchTextBox.getWindowToken(), 0);
                } else {
                    binding.searchTextBox.postDelayed(this, 50);
                }
            }
        }, 50);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable("state", mState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSelectionChanged(int selStart, int selEnd) {

    }
}