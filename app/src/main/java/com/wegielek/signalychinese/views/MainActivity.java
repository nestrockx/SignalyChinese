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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.digitalink.RecognitionCandidate;
import com.wegielek.signalychinese.BR;
import com.wegielek.signalychinese.adapters.RadicalsParentAdapter;
import com.wegielek.signalychinese.interfaces.CanvasViewListener;
import com.wegielek.signalychinese.interfaces.CharactersRecyclerViewListener;
import com.wegielek.signalychinese.interfaces.RadicalsRecyclerViewListener;
import com.wegielek.signalychinese.interfaces.ResultsRecyclerViewListener;
import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.enums.State;
import com.wegielek.signalychinese.adapters.CharacterListAdapter;
import com.wegielek.signalychinese.adapters.ResultsListAdapter;
import com.wegielek.signalychinese.databinding.ActivityMainBinding;
import com.wegielek.signalychinese.models.RadicalsParentModel;
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

public class MainActivity extends AppCompatActivity implements CanvasViewListener, CharactersRecyclerViewListener, ResultsRecyclerViewListener, RadicalsRecyclerViewListener {
    private CharacterListAdapter mCharacterListAdapter;
    private ResultsListAdapter mResultsListAdapter;
    private RadicalsParentAdapter mRadicalsParentAdapter;
    private List<String> mSearchResults;
    private Map<String, String> jsonTraditionalMap;
    private Map<String, String> jsonSimplifiedMap;
    private Map<Integer, String[]> jsonRadicalsMap;
    private State mState = State.DRAW;
    private ActivityMainBinding binding;
    private MainViewModel mMainViewModel;
    private List<RadicalsParentModel> mRadicalsParentModels;

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

        mSearchResults = new ArrayList<>();

        if (savedInstanceState != null) {
            mState = (State) savedInstanceState.getSerializable("state");
        }


        binding.searchTextBox.requestFocus();

        binding.resultsRv.setLayoutManager(new LinearLayoutManager(this));
        mResultsListAdapter = new ResultsListAdapter(this, this);
        binding.resultsRv.setAdapter(mResultsListAdapter);

        //--------------------------------------------------------------------------------------------------------------------------
        String jsonRadicalsString;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open("radicalsJSON.json"), StandardCharsets.UTF_8))) {
            jsonRadicalsString = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        jsonRadicalsMap = new Gson().fromJson(
                jsonRadicalsString, new TypeToken<HashMap<Integer, String[]>>() {}.getType()
        );

        mRadicalsParentModels = new ArrayList<>();

        for(int i = 0; i < jsonRadicalsMap.get(0).length - 1; i++) {
            mRadicalsParentModels.add(new RadicalsParentModel(jsonRadicalsMap.get(0)[i].split(" ")));
        }

        mRadicalsParentAdapter = new RadicalsParentAdapter(mRadicalsParentModels, this);
        binding.radicalsRv.setLayoutManager(new LinearLayoutManager(this));
        binding.radicalsRv.setAdapter(mRadicalsParentAdapter);
        mRadicalsParentAdapter.notifyDataSetChanged();
        //----------------------------------------------------------------------------------------------------------------------------

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
            backspace(1, true);
            binding.doneBtn.setVisibility(View.INVISIBLE);
            mMainViewModel.setCursorPosition(binding.searchTextBox.length());
            binding.characterDrawCanvas.clear();
            binding.undoBtn.setVisibility(View.INVISIBLE);
        });

        binding.doneBtn.setOnClickListener(view -> {
            mMainViewModel.clearCharacterList();
            mCharacterListAdapter.notifyDataSetChanged();
            binding.doneBtn.setVisibility(View.INVISIBLE);
            mMainViewModel.setCursorPosition(binding.searchTextBox.getText().length());
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
            performSearch();
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
        mState = State.RESULTS;
        mSearchResults.clear();
        if (jsonTraditionalMap != null && jsonSimplifiedMap != null) {
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

            if (inputTextString.length() > 1) {
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

            mMainViewModel.updateResults(mSearchResults);

            mState = State.RESULTS;
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(binding.searchTextBox.getWindowToken(), 0);
            }
            binding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
            binding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));

            mMainViewModel.setCursorPosition(binding.searchTextBox.length());
            binding.characterDrawCanvas.clear();
            binding.undoBtn.setVisibility(View.INVISIBLE);
            binding.characterDrawCanvas.setVisibility(View.INVISIBLE);
            binding.charactersRv.setVisibility(View.INVISIBLE);
            binding.resultsRv.setVisibility(View.VISIBLE);
            binding.labelTv.setText(getString(R.string.searching_mode));
            binding.doneBtn.setVisibility(View.INVISIBLE);
            binding.radicalsRv.setVisibility(View.INVISIBLE);
        } else {
            Toast.makeText(this, getString(R.string.dictionary_files_not_loaded), Toast.LENGTH_SHORT).show();
        }
    }

    private void backspace(int n, boolean force) {
        int length = Objects.requireNonNull(binding.searchTextBox.getText()).length();
        if (n - (length - mMainViewModel.getCursorPosition()) == 1 && length > 0 && !force) {
            binding.searchTextBox.getText().delete(length - n + 1, length);
        }
        else if (length - mMainViewModel.getCursorPosition() >= n && !force) {
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
        mMainViewModel.clearCharacterList();
        for (RecognitionCandidate rc: recognitionCandidatesList) {
            mMainViewModel.addToCharacterList(rc.getText());
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
        backspace(mMainViewModel.charactersList.getValue().get(position).length(), false);
        binding.searchTextBox.append(mMainViewModel.charactersList.getValue().get(position));
        mMainViewModel.setCursorPosition(mMainViewModel.getCursorPosition() + mMainViewModel.charactersList.getValue().get(position).length());
        mMainViewModel.clearCharacterList();
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

    @Override
    protected void onStart() {
        super.onStart();
        setStateUI(mState);
    }
}