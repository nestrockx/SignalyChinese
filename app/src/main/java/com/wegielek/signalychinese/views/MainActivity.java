package com.wegielek.signalychinese.views;

import androidx.appcompat.app.AppCompatActivity;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.vision.digitalink.RecognitionCandidate;
import com.wegielek.signalychinese.Interfaces.CanvasViewListener;
import com.wegielek.signalychinese.Interfaces.RecyclerViewListener;
import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.adapters.CharacterListAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements CanvasViewListener, RecyclerViewListener {

    private TextInputEditText textInputEditText;
    private CanvasView canvasView;
    private RecyclerView charactersRecyclerView;
    private CharacterListAdapter characterListAdapter;
    private List<String> charactersList;
    private Button doneButton;
    private List<String> fileDictionaryContents;

    private int cursorPosition = 0;

    private Handler mHandler;

    private void backspace(int n, boolean force) {
        int length = Objects.requireNonNull(textInputEditText.getText()).length();
        if (n - (length - cursorPosition) == 1 && length > 0 && !force) {
            textInputEditText.getText().delete(length - n + 1, length);
        }
        else if (length - cursorPosition >= n && !force) {
            textInputEditText.getText().delete(length - n, length);
        }
        else if (force && length >= 1) {
            textInputEditText.getText().delete(length - 1, length);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textInputEditText = findViewById(R.id.textInputEditText);
        textInputEditText.requestFocus();
        textInputEditText.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textInputEditText.getWindowToken(), 0);
        }, 1000);

        textInputEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //performSearch();
                return true;
            }
            return false;
        });

        Button backspaceButton = findViewById(R.id.backspaceButton);
        backspaceButton.setOnClickListener(view -> {
            backspace(1, true);
            canvasView.clear();
            doneButton.setVisibility(View.INVISIBLE);
            cursorPosition = textInputEditText.length();
            canvasView.clearHistory();
        });

        canvasView = findViewById(R.id.CanvasView);
        canvasView.setOnRecognizeListener(this);
        canvasView.post(() -> {
            canvasView.init(canvasView.getWidth(), canvasView.getHeight());
        });

        charactersRecyclerView = findViewById(R.id.recyclerView);
        charactersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        charactersList = new ArrayList<>();
        characterListAdapter = new CharacterListAdapter(charactersList, this);
        charactersRecyclerView.setAdapter(characterListAdapter);

        doneButton = findViewById(R.id.done_btn);
        doneButton.setOnClickListener(view -> {
            canvasView.clear();
            charactersList.clear();
            characterListAdapter.notifyDataSetChanged();
            doneButton.setVisibility(View.INVISIBLE);
            cursorPosition += Objects.requireNonNull(textInputEditText.getText()).length() - cursorPosition;
            canvasView.clearHistory();
        });

        fileDictionaryContents = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open("resultTest.txt"), StandardCharsets.UTF_8))) {

            String mLine;
            while ((mLine = reader.readLine()) != null) {
                fileDictionaryContents.add(mLine);
            }
            //Toast.makeText(this, fileDictionaryContents.get(0), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Button undoButton = findViewById(R.id.undo_btn);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canvasView.undo();
            }
        });


        //  HanziWritingView hanziWritingView = findViewById(R.id.HanjaWritingView);
      //  hanziWritingView.setHanziCharacter('读');
      //  hanziWritingView.setDimensions(1000);

      //  Button clearButton = findViewById(R.id.clear_button);
      //  clearButton.setOnClickListener(view -> hanziWritingView.clear());


        /*
        mHandler = new Handler();
        Runnable wakelock = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Hooy", Toast.LENGTH_SHORT).show();
                mHandler.postDelayed(this, 5000);
            }
        };
        mHandler.post(wakelock);
         */

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResults(List<RecognitionCandidate> recognitionCandidatesList) {
        charactersList.clear();
        for (RecognitionCandidate rc: recognitionCandidatesList) {
            charactersList.add(rc.getText());
        }
        characterListAdapter.notifyDataSetChanged();

        backspace(recognitionCandidatesList.get(0).getText().length(), false);
        textInputEditText.append(recognitionCandidatesList.get(0).getText());

        doneButton.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemReleased(int position) {
        //Toast.makeText(this, charactersList.get(position), Toast.LENGTH_SHORT).show();
        backspace(charactersList.get(position).length(), false);
        textInputEditText.append(charactersList.get(position));
        cursorPosition += charactersList.get(position).length();
        canvasView.clear();
        charactersList.clear();
        characterListAdapter.notifyDataSetChanged();
        doneButton.setVisibility(View.INVISIBLE);
        canvasView.clearHistory();
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
}