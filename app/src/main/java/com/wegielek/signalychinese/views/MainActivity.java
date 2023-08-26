package com.wegielek.signalychinese.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.digitalink.RecognitionCandidate;
import com.wegielek.signalychinese.Interfaces.CanvasViewListener;
import com.wegielek.signalychinese.Interfaces.CharactersRecyclerViewListener;
import com.wegielek.signalychinese.Interfaces.ResultsRecyclerViewListener;
import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.State;
import com.wegielek.signalychinese.adapters.CharacterListAdapter;
import com.wegielek.signalychinese.adapters.ResultsListAdapter;

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


public class MainActivity extends AppCompatActivity implements CanvasViewListener, CharactersRecyclerViewListener, ResultsRecyclerViewListener {
    private CanvasView mCanvasView;
    private RecyclerView mCharactersRecyclerView;
    private CharacterListAdapter mCharacterListAdapter;
    private List<String> mCharactersList;
    private RecyclerView mResultsRecyclerView;
    private ResultsListAdapter mResultsListAdapter;
    private List<String> dictionaryResultsList;
    private List<String> mSearchResults;
    private Map<String, String> jsonTraditionalMap;
    private Map<String, String> jsonSimplifiedMap;

    private TextInputEditText mTextInputEditText;
    private TextView mLabel;
    private Button mDoneButton;
    private Button mSearchButton;
    private Button mLearnButton;
    private Button mPuzzleButton;
    private Button mDrawButton;
    private Button mBackspaceButton;
    private Button mUndoButton;
    private State mState = State.DRAW;
    private int mCursorPosition = 0;


    private void backspace(int n, boolean force) {
        int length = Objects.requireNonNull(mTextInputEditText.getText()).length();
        if (n - (length - mCursorPosition) == 1 && length > 0 && !force) {
            mTextInputEditText.getText().delete(length - n + 1, length);
        }
        else if (length - mCursorPosition >= n && !force) {
            mTextInputEditText.getText().delete(length - n, length);
        }
        else if (force && length >= 1) {
            mTextInputEditText.getText().delete(length - 1, length);
        }

        if (mState == State.RESULTS && length > 1) {
            performSearch();
        } else if (mState == State.RESULTS) {
            mDrawButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_active, getTheme()));
            mResultsRecyclerView.setVisibility(View.INVISIBLE);
            mCanvasView.setVisibility(View.VISIBLE);
            mLabel.setText(getString(R.string.drawing_mode));
            mState = State.DRAW;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mState = (State) savedInstanceState.getSerializable("state");
        }

        mLearnButton = findViewById(R.id.learn_btn);
        mPuzzleButton = findViewById(R.id.puzzle_btn);
        mDrawButton = findViewById(R.id.draw_btn);
        mDoneButton = findViewById(R.id.done_btn);
        mUndoButton = findViewById(R.id.undo_btn);
        mSearchButton = findViewById(R.id.search_btn);
        mBackspaceButton = findViewById(R.id.backspaceButton);
        mTextInputEditText = findViewById(R.id.textInputEditText);
        mCanvasView = findViewById(R.id.CharacterDrawCanvasView);
        mResultsRecyclerView = findViewById(R.id.results_rv);
        mCharactersRecyclerView = findViewById(R.id.recyclerView);
        mLabel = findViewById(R.id.label);

        mCharactersList = new ArrayList<>();
        dictionaryResultsList = new ArrayList<>();
        mSearchResults = new ArrayList<>();


        if (mState == State.DRAW) {
            mLabel.setText(getString(R.string.drawing_mode));
            mDrawButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_active, getTheme()));
            mPuzzleButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
        } else if (mState == State.RESULTS) {
            mLabel.setText(getString(R.string.searching_mode));
            mDrawButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
            mPuzzleButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
        } else {
            mPuzzleButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_active, getTheme()));
            mDrawButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
            mLabel.setText(getString(R.string.puzzle_mode));
        }


        mTextInputEditText.requestFocus();

        mResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mResultsListAdapter = new ResultsListAdapter(dictionaryResultsList, this, this);
        mResultsRecyclerView.setAdapter(mResultsListAdapter);

        mCharactersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mCharacterListAdapter = new CharacterListAdapter(mCharactersList, this);
        mCharactersRecyclerView.setAdapter(mCharacterListAdapter);

        mCanvasView.setOnRecognizeListener(this);
        mCanvasView.post(() -> {
            mCanvasView.init(mCanvasView.getWidth(), mCanvasView.getHeight());
        });

        mUndoButton.setOnClickListener(view -> mCanvasView.undo());
        mSearchButton.setOnClickListener(v -> performSearch());

        mPuzzleButton.setOnClickListener(view -> {
            mState = State.PUZZLE;
            mLabel.setText(getString(R.string.puzzle_mode));
            mPuzzleButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_active, getTheme()));
            mDrawButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
            mCanvasView.setVisibility(View.INVISIBLE);
            mResultsRecyclerView.setVisibility(View.INVISIBLE);
        });

        mDrawButton.setOnClickListener(view -> {
            mState = State.DRAW;
            mLabel.setText(getString(R.string.drawing_mode));
            mDrawButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_active, getTheme()));
            mPuzzleButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
            mResultsRecyclerView.setVisibility(View.INVISIBLE);
            mCanvasView.setVisibility(View.VISIBLE);
        });

        mBackspaceButton.setOnClickListener(view -> {
            backspace(1, true);
            mDoneButton.setVisibility(View.INVISIBLE);
            mCursorPosition = mTextInputEditText.length();
            mCanvasView.clear();
            mUndoButton.setVisibility(View.INVISIBLE);
        });

        mDoneButton.setOnClickListener(view -> {
            mCharactersList.clear();
            mCharacterListAdapter.notifyDataSetChanged();
            mDoneButton.setVisibility(View.INVISIBLE);
            mCursorPosition += Objects.requireNonNull(mTextInputEditText.getText()).length() - mCursorPosition;
            mCanvasView.clear();
            mUndoButton.setVisibility(View.INVISIBLE);
        });

        mTextInputEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });


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
    }

    @SuppressLint("NotifyDataSetChanged")
    private void performSearch() {
        String inputTextString = Objects.requireNonNull(mTextInputEditText.getText()).toString();

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

        dictionaryResultsList.clear();
        dictionaryResultsList.addAll(mSearchResults);
        mResultsListAdapter.notifyDataSetChanged();

        mState = State.RESULTS;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(mTextInputEditText.getWindowToken(), 0);
        }
        mDrawButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
        mPuzzleButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));

        mCursorPosition = mTextInputEditText.length();
        mCanvasView.clear();
        mUndoButton.setVisibility(View.INVISIBLE);
        mCanvasView.setVisibility(View.INVISIBLE);
        mCharactersRecyclerView.setVisibility(View.INVISIBLE);
        mResultsRecyclerView.setVisibility(View.VISIBLE);
        mLabel.setText(getString(R.string.searching_mode));
        mDoneButton.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResults(List<RecognitionCandidate> recognitionCandidatesList) {
        mCharactersList.clear();
        for (RecognitionCandidate rc: recognitionCandidatesList) {
            mCharactersList.add(rc.getText());
        }
        mCharacterListAdapter.notifyDataSetChanged();

        backspace(recognitionCandidatesList.get(0).getText().length(), false);
        mTextInputEditText.append(recognitionCandidatesList.get(0).getText());

        mDoneButton.setVisibility(View.VISIBLE);
        mCharactersRecyclerView.setVisibility(View.VISIBLE);
        mUndoButton.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemReleased(int position) {
        //Toast.makeText(this, charactersList.get(position), Toast.LENGTH_SHORT).show();
        backspace(mCharactersList.get(position).length(), false);
        mTextInputEditText.append(mCharactersList.get(position));
        mCursorPosition += mCharactersList.get(position).length();
        mCharactersList.clear();
        mCharacterListAdapter.notifyDataSetChanged();
        mDoneButton.setVisibility(View.INVISIBLE);
        mCanvasView.clear();
        mUndoButton.setVisibility(View.INVISIBLE);

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
        Intent intent = new Intent();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mTextInputEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(mTextInputEditText.getWindowToken(), 0);
                } else {
                    mTextInputEditText.postDelayed(this, 100);
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