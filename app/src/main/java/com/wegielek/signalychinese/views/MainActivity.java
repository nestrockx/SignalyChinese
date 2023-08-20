package com.wegielek.signalychinese.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
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
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements CanvasViewListener, CharactersRecyclerViewListener, ResultsRecyclerViewListener {

    private List<String> traditional;
    private List<String> simplified;
    private List<String> transcription;
    private List<String> translation;
    private TextInputEditText mTextInputEditText;
    private CanvasView mCanvasView;
    private RecyclerView mCharactersRecyclerView;
    private CharacterListAdapter mCharacterListAdapter;
    private ResultsListAdapter mResultsListAdapter;
    private List<String> mCharactersList;
    private Button mDoneButton;
    private Button mSearchButton;
    private List<String> mFileDictionaryContents;
    private int mCursorPosition = 0;
    private TextView mLabel;
    private RecyclerView mResultsRecyclerView;
    private Button mUndoButton;
    private List<String> dictionaryResultsList;
    private Handler mHandler;
    private Button mLearnButton;
    private Button mPuzzleButton;
    private Button mDrawButton;
    private Button mBackspaceButton;
    private State mState = State.DRAW;

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
            mLabel.setText("Narysuj znak");
            mState = State.DRAW;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        mPuzzleButton.setOnClickListener(view -> {
            mPuzzleButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_active, getTheme()));
            mDrawButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
        });


        mDrawButton.setOnClickListener(view -> {
            mDrawButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_active, getTheme()));
            mPuzzleButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));
            mResultsRecyclerView.setVisibility(View.INVISIBLE);
            mCanvasView.setVisibility(View.VISIBLE);
            mLabel.setText("Narysuj znak");
            mState = State.DRAW;
        });


        mTextInputEditText.requestFocus();
        mTextInputEditText.postDelayed(new Runnable() {
              @Override
              public void run() {
                  InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                  if (imm.isActive()) {
                      imm.hideSoftInputFromWindow(mTextInputEditText.getWindowToken(), 0);
                  } else {
                      mTextInputEditText.postDelayed(this, 10);
                  }
              }
        }, 10);

        mTextInputEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        mBackspaceButton.setOnClickListener(view -> {
            backspace(1, true);
            mDoneButton.setVisibility(View.INVISIBLE);
            mCursorPosition = mTextInputEditText.length();
            mCanvasView.clear();
        });


        mCanvasView.setOnRecognizeListener(this);
        mCanvasView.post(() -> {
            mCanvasView.init(mCanvasView.getWidth(), mCanvasView.getHeight());
        });


        mCharactersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mCharactersList = new ArrayList<>();
        mCharacterListAdapter = new CharacterListAdapter(mCharactersList, this);
        mCharactersRecyclerView.setAdapter(mCharacterListAdapter);


        mDoneButton.setOnClickListener(view -> {
            mCharactersList.clear();
            mCharacterListAdapter.notifyDataSetChanged();
            mDoneButton.setVisibility(View.INVISIBLE);
            mCursorPosition += Objects.requireNonNull(mTextInputEditText.getText()).length() - mCursorPosition;
            mCanvasView.clear();
        });

        mFileDictionaryContents = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open("resultTest.txt"), StandardCharsets.UTF_8))) {

            String mLine;
            while ((mLine = reader.readLine()) != null) {
                mFileDictionaryContents.add(mLine);
            }
            //Toast.makeText(this, fileDictionaryContents.get(0), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        traditional = new ArrayList<>();
        simplified = new ArrayList<>();
        transcription = new ArrayList<>();
        translation = new ArrayList<>();
        for (String line : mFileDictionaryContents) {
            Pattern pattern = Pattern.compile("(.+) (.+) \\[(.+)\\] /(.+)/");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String x = matcher.group(1);
                String y = matcher.group(2);
                String z = matcher.group(3).replaceAll("\\d", "");
                String a = matcher.group(4);

                traditional.add(x);
                simplified.add(y);
                transcription.add(z);
                translation.add(a);
            } else {

            }
        }

        mUndoButton.setOnClickListener(view -> mCanvasView.undo());
        mSearchButton.setOnClickListener(v -> performSearch());

        mResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        dictionaryResultsList = new ArrayList<>();
        dictionaryResultsList.add("凈身 净身 [jing4 shen1] /oczyścić swoje ciało (tj. poddać się kastracji)/");
        dictionaryResultsList.add("凈身 净身 [jing4 shen1] /oczyścić swoje ciało (tj. poddać się kastracji)/");
        dictionaryResultsList.add("凈身 净身 [jing4 shen1] /oczyścić swoje ciało (tj. poddać się kastracji)/");
        dictionaryResultsList.add("凈身 净身 [jing4 shen1] /oczyścić swoje ciało (tj. poddać się kastracji)/");
        dictionaryResultsList.add("凈身 净身 [jing4 shen1] /oczyścić swoje ciało (tj. poddać się kastracji)/");
        dictionaryResultsList.add("凈身 净身 [jing4 shen1] /oczyścić swoje ciało (tj. poddać się kastracji)/");
        dictionaryResultsList.add("凈身 净身 [jing4 shen1] /oczyścić swoje ciało (tj. poddać się kastracji)/");
        dictionaryResultsList.add("凈身 净身 [jing4 shen1] /oczyścić swoje ciało (tj. poddać się kastracji)/");
        dictionaryResultsList.add("凈身 净身 [jing4 shen1] /oczyścić swoje ciało (tj. poddać się kastracji)/");
        dictionaryResultsList.add("凈身 净身 [jing4 shen1] /oczyścić swoje ciało (tj. poddać się kastracji)/");

        mResultsListAdapter = new ResultsListAdapter(dictionaryResultsList, this);

        mResultsRecyclerView.setAdapter(mResultsListAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void performSearch() {

        mState = State.RESULTS;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(mTextInputEditText.getWindowToken(), 0);
        }
        mDrawButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_draw_default, getTheme()));
        mPuzzleButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_puzzle_default, getTheme()));


        mCursorPosition = mTextInputEditText.length();
        mCanvasView.clear();
        mCanvasView.setVisibility(View.INVISIBLE);
        mCharactersRecyclerView.setVisibility(View.INVISIBLE);
        mUndoButton.setVisibility(View.INVISIBLE);
        mResultsRecyclerView.setVisibility(View.VISIBLE);
        mLabel.setText("Wyszukiwanie");
        mDoneButton.setVisibility(View.INVISIBLE);

        ArrayList<String> searchResults = new ArrayList<>();
        String inputTextString = mTextInputEditText.getText().toString();
        for (int i = 0; i < mFileDictionaryContents.size(); i++) {
            Pattern pattern = Pattern.compile(inputTextString, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(simplified.get(i));
            Matcher matcher1 = pattern.matcher(transcription.get(i));
            Matcher matcher2 = pattern.matcher(traditional.get(i));

            if (matcher.matches() || matcher1.matches() || matcher2.matches()) {
                searchResults.add(mFileDictionaryContents.get(i));
            }

        }
        dictionaryResultsList.clear();
        dictionaryResultsList.addAll(searchResults);
        mResultsListAdapter.notifyDataSetChanged();
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

    }
}