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
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.digitalink.RecognitionCandidate;
import com.wegielek.signalychinese.BR;
import com.wegielek.signalychinese.Interfaces.CanvasViewListener;
import com.wegielek.signalychinese.Interfaces.CharactersRecyclerViewListener;
import com.wegielek.signalychinese.Interfaces.ResultsRecyclerViewListener;
import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.Enums.State;
import com.wegielek.signalychinese.adapters.CharacterListAdapter;
import com.wegielek.signalychinese.adapters.ResultsListAdapter;
import com.wegielek.signalychinese.databinding.ActivityMainBinding;
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

public class MainActivity extends AppCompatActivity implements CanvasViewListener, CharactersRecyclerViewListener, ResultsRecyclerViewListener {
    private CharacterListAdapter mCharacterListAdapter;
    private ResultsListAdapter mResultsListAdapter;
    private List<String> mSearchResults;
    private Map<String, String> jsonTraditionalMap;
    private Map<String, String> jsonSimplifiedMap;
    private State mState = State.DRAW;
    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding.setVariable(BR.vm, mainViewModel);
        binding.executePendingBindings();

        mainViewModel.dictionaryResultsList.observe(this, stringList ->
                mResultsListAdapter.setData(stringList)
        );

        mainViewModel.charactersList.observe(this, stringList ->
                mCharacterListAdapter.setData(stringList)
        );

        mSearchResults = new ArrayList<>();

        if (savedInstanceState != null) {
            mState = (State) savedInstanceState.getSerializable("state");
        }
        if (mState == State.DRAW) {
            binding.labelTv.setText(getString(R.string.drawing_mode));
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_active, getTheme()));
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
            binding.charactersRv.setVisibility(View.VISIBLE);
            binding.characterDrawCanvas.setVisibility(View.VISIBLE);
            binding.resultsRv.setVisibility(View.INVISIBLE);
        } else if (mState == State.RESULTS) {
            binding.labelTv.setText(getString(R.string.searching_mode));
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
            binding.characterDrawCanvas.setVisibility(View.INVISIBLE);
            binding.resultsRv.setVisibility(View.VISIBLE);
        } else {
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_active, getTheme()));
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
            binding.labelTv.setText(getString(R.string.puzzle_mode));
            binding.characterDrawCanvas.setVisibility(View.INVISIBLE);
            binding.resultsRv.setVisibility(View.INVISIBLE);
        }

        binding.searchTextBox.requestFocus();

        binding.resultsRv.setLayoutManager(new LinearLayoutManager(this));
        mResultsListAdapter = new ResultsListAdapter(this, this);
        binding.resultsRv.setAdapter(mResultsListAdapter);

        binding.charactersRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mCharacterListAdapter = new CharacterListAdapter(this);
        binding.charactersRv.setAdapter(mCharacterListAdapter);

        binding.characterDrawCanvas.setOnRecognizeListener(this);
        binding.characterDrawCanvas.post(() -> binding.characterDrawCanvas.initialize(binding.characterDrawCanvas.getWidth(), binding.characterDrawCanvas.getHeight(), mainViewModel));

        binding.undoBtn.setOnClickListener(view -> binding.characterDrawCanvas.undoStroke());
        binding.searchBtn.setOnClickListener(v -> performSearch());

        binding.puzzleBtn.setOnClickListener(view -> {
            mState = State.PUZZLE;
            binding.labelTv.setText(getString(R.string.puzzle_mode));
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_active, getTheme()));
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
            binding.characterDrawCanvas.setVisibility(View.INVISIBLE);
            binding.resultsRv.setVisibility(View.INVISIBLE);
        });

        binding.drawBtn.setOnClickListener(view -> {
            mState = State.DRAW;
            binding.labelTv.setText(getString(R.string.drawing_mode));
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_active, getTheme()));
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
            binding.resultsRv.setVisibility(View.INVISIBLE);
            binding.characterDrawCanvas.setVisibility(View.VISIBLE);
        });
        
        binding.backspaceBtn.setOnClickListener(view -> {
            backspace(1, true);
            binding.doneBtn.setVisibility(View.INVISIBLE);
            mainViewModel.setCursorPosition(binding.searchTextBox.length());
            binding.characterDrawCanvas.clear();
            binding.undoBtn.setVisibility(View.INVISIBLE);
        });

        binding.doneBtn.setOnClickListener(view -> {
            mainViewModel.clearCharacterList();
            mCharacterListAdapter.notifyDataSetChanged();
            binding.doneBtn.setVisibility(View.INVISIBLE);
            mainViewModel.setCursorPosition(binding.searchTextBox.getText().length());
            binding.characterDrawCanvas.clear();
            binding.undoBtn.setVisibility(View.INVISIBLE);
        });

        binding.searchTextBox.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
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

    @SuppressLint("NotifyDataSetChanged")
    private void performSearch() {
        String inputTextString = Objects.requireNonNull(binding.searchTextBox.getText()).toString();

        if (jsonTraditionalMap.containsKey(inputTextString)) {
            String x = jsonTraditionalMap.get(inputTextString);
            mSearchResults = new ArrayList<>(Arrays.asList(Objects.requireNonNull(x).split(" /X/ ")));
            mSearchResults.replaceAll(s -> inputTextString + "/" + s);

        } else if (jsonSimplifiedMap.containsKey(inputTextString)) {
            String x = jsonSimplifiedMap.get(inputTextString);
            mSearchResults = new ArrayList<>(Arrays.asList(Objects.requireNonNull(x).split(" /X/ ")));
            for (int i = 0; i < mSearchResults.size(); i++) {
                String tmp = mSearchResults.get(i).split("/")[0];
                mSearchResults.set(i, tmp + "/" + mSearchResults.get(i).replace(tmp, inputTextString));
            }
        }

        if(inputTextString.length() > 1) {
            for (int i = 0; i < inputTextString.length(); i++) {
                if (jsonTraditionalMap.containsKey(String.valueOf(inputTextString.charAt(i)))) {
                    int searchresultscount1 = mSearchResults.size();
                    String x = jsonTraditionalMap.get(String.valueOf(inputTextString.charAt(i)));
                    mSearchResults.addAll(new ArrayList<>(Arrays.asList(Objects.requireNonNull(x).split(" /X/ "))));
                    for (int j = searchresultscount1; j < mSearchResults.size(); j++) {
                        mSearchResults.set(j, inputTextString.charAt(i) + "/" + mSearchResults.get(j));
                    }
                } else if (jsonSimplifiedMap.containsKey(String.valueOf(inputTextString.charAt(i)))) {
                    int searchresultscount2 = mSearchResults.size();
                    String x = jsonSimplifiedMap.get(String.valueOf(inputTextString.charAt(i)));
                    mSearchResults.addAll(new ArrayList<>(Arrays.asList(Objects.requireNonNull(x).split(" /X/ "))));
                    for (int j = searchresultscount2; j < mSearchResults.size(); j++) {
                        String tmp = mSearchResults.get(j).split("/")[0];
                        mSearchResults.set(j, tmp + "/" + mSearchResults.get(j).replace(tmp, String.valueOf(inputTextString.charAt(i))));
                    }
                }
            }
        }

        mainViewModel.updateResults(mSearchResults);

        mState = State.RESULTS;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(binding.searchTextBox.getWindowToken(), 0);
        }
        binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
        binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));

        mainViewModel.setCursorPosition(binding.searchTextBox.length());
        binding.characterDrawCanvas.clear();
        binding.undoBtn.setVisibility(View.INVISIBLE);
        binding.characterDrawCanvas.setVisibility(View.INVISIBLE);
        binding.charactersRv.setVisibility(View.INVISIBLE);
        binding.resultsRv.setVisibility(View.VISIBLE);
        binding.labelTv.setText(getString(R.string.searching_mode));
        binding.doneBtn.setVisibility(View.INVISIBLE);
    }

    private void backspace(int n, boolean force) {
        int length = Objects.requireNonNull(binding.searchTextBox.getText()).length();
        if (n - (length - mainViewModel.getCursorPosition()) == 1 && length > 0 && !force) {
            binding.searchTextBox.getText().delete(length - n + 1, length);
        }
        else if (length - mainViewModel.getCursorPosition() >= n && !force) {
            binding.searchTextBox.getText().delete(length - n, length);
        }
        else if (force && length >= 1) {
            binding.searchTextBox.getText().delete(length - 1, length);
        }

        if (mState == State.RESULTS && length > 1) {
            performSearch();
        } else if (mState == State.RESULTS) {
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_active, getTheme()));
            binding.resultsRv.setVisibility(View.INVISIBLE);
            binding.characterDrawCanvas.setVisibility(View.VISIBLE);
            binding.labelTv.setText(getString(R.string.drawing_mode));
            mState = State.DRAW;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResults(List<RecognitionCandidate> recognitionCandidatesList) {
        mainViewModel.clearCharacterList();
        for (RecognitionCandidate rc: recognitionCandidatesList) {
            mainViewModel.addToCharacterList(rc.getText());
        }
        mCharacterListAdapter.notifyDataSetChanged();
        backspace(recognitionCandidatesList.get(0).getText().length(), false);
        binding.searchTextBox.append(recognitionCandidatesList.get(0).getText());
        binding.doneBtn.setVisibility(View.VISIBLE);
        binding.charactersRv.setVisibility(View.VISIBLE);
        binding.undoBtn.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemReleased(int position) {
        backspace(mainViewModel.charactersList.getValue().get(position).length(), false);
        binding.searchTextBox.append(mainViewModel.charactersList.getValue().get(position));
        mainViewModel.setCursorPosition(mainViewModel.getCursorPosition() + mainViewModel.charactersList.getValue().get(position).length());
        mainViewModel.clearCharacterList();
        mCharacterListAdapter.notifyDataSetChanged();
        binding.doneBtn.setVisibility(View.INVISIBLE);
        binding.characterDrawCanvas.clear();
        binding.undoBtn.setVisibility(View.INVISIBLE);
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
        Intent intent = new Intent(this, DictionaryActivity.class);
        intent.putExtra("word", mainViewModel.getResult(position));
        startActivity(intent);
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
                    binding.searchTextBox.postDelayed(this, 100);
                }
            }
        }, 100);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("state", mState);
    }
}