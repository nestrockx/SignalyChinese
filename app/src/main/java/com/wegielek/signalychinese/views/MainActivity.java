package com.wegielek.signalychinese.views;

import static com.wegielek.signalychinese.utils.Utils.containsChinese;

import androidx.activity.OnBackPressedCallback;
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
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.mlkit.vision.digitalink.RecognitionCandidate;

import com.wegielek.signalychinese.BR;
import com.wegielek.signalychinese.adapters.RadicalsAdapter;
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
import com.wegielek.signalychinese.viewmodels.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CanvasViewListener,
        CharactersRecyclerViewListener, ResultsRecyclerViewListener, RadicalsRecyclerViewListener,
        SearchTextBoxListener {
    private static final String LOG_TAG = "MainActivity";

    private final Handler mSearchHandler = new Handler();

    private SuggestedCharacterListAdapter mSuggestedCharacterListAdapter;
    private SearchResultListAdapter mSearchResultsListAdapter;
    private RadicalsAdapter mRadicalsAdapter;
    private StateUI mStateUI = StateUI.DRAW;
    private ActivityMainBinding mBinding;
    private MainViewModel mMainViewModel;

    private boolean mRadicalChosen = false;

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

        mMainViewModel.mRadicalsList.observe(this, radicalsList ->
                mRadicalsAdapter.setData(radicalsList)
        );

        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mStateUI = savedInstanceState.getSerializable("state", StateUI.class);
            } else {
                mStateUI = (StateUI) savedInstanceState.getSerializable("state");
            }
        }
        setStateUI(mStateUI);

        mBinding.radicalsBackBtn.setOnClickListener(v -> mMainViewModel.getSection("0").observe(this, radicals -> {
            List<String[]> radicalsList = new ArrayList<>();
            for (int i = 0; i < radicals.size(); i++) {
                radicalsList.add(radicals.get(i).radicals.split(" "));
            }
            mMainViewModel.setRadicalsList(radicalsList);
            mRadicalChosen = false;
            setStateUI(StateUI.PUZZLE);
            getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                }
            });
        }));

        mBinding.searchTextBox.requestFocus();
        mBinding.searchTextBox.setOnSelectionChangedListener(this);

        mBinding.resultsRv.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultsListAdapter = new SearchResultListAdapter(this, this);
        mBinding.resultsRv.setAdapter(mSearchResultsListAdapter);

        mBinding.radicalsRv.setLayoutManager(new LinearLayoutManager(this));
        mRadicalsAdapter = new RadicalsAdapter(this, this);
        mBinding.radicalsRv.setAdapter(mRadicalsAdapter);

        mBinding.charactersRv.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL,
                false)
        );
        mSuggestedCharacterListAdapter = new SuggestedCharacterListAdapter(this);
        mBinding.charactersRv.setAdapter(mSuggestedCharacterListAdapter);

        mBinding.characterDrawCanvas.setOnRecognizeListener(this);
        mBinding.characterDrawCanvas.post(() -> mBinding.characterDrawCanvas.initialize(
                mBinding.characterDrawCanvas.getWidth(),
                mBinding.characterDrawCanvas.getHeight(),
                mMainViewModel)
        );

        mBinding.undoBtn.setOnClickListener(view -> mBinding.characterDrawCanvas.undoStroke());
        mBinding.searchBtn.setOnClickListener(v -> performSearch(true));

        mBinding.puzzleBtn.setOnClickListener(view -> {
            setStateUI(StateUI.PUZZLE);
        });

        mBinding.drawBtn.setOnClickListener(view -> {
            setStateUI(StateUI.DRAW);
        });
        
        mBinding.backspaceBtn.setOnClickListener(view -> {
            if (mBinding.searchTextBox.getText() != null) {
                mBinding.searchTextBox.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                mMainViewModel.setCursorPosition(mBinding.searchTextBox.getSelectionEnd());

                if (mBinding.searchTextBox.getText().length() <= 0) {
                    setStateUI(StateUI.DRAW);
                }

                mBinding.doneBtn.setVisibility(View.INVISIBLE);
                mBinding.characterDrawCanvas.clear();
                mBinding.undoBtn.setVisibility(View.INVISIBLE);
            } else {
                Log.e(LOG_TAG, "Search text box text is null in backspaceBtn.onClick");
            }
        });

        mBinding.doneBtn.setOnClickListener(view -> {
            mMainViewModel.clearCharacterList();
            mBinding.doneBtn.setVisibility(View.INVISIBLE);
            if (mBinding.searchTextBox.getText() != null) {
                mBinding.searchTextBox.setSelection(mBinding.searchTextBox.getText().length());
                mMainViewModel.setCursorPosition(mBinding.searchTextBox.getText().length());
            } else {
                Log.e(LOG_TAG, "Search text box text is null in doneBtn.onClick");
            }
            mBinding.characterDrawCanvas.clear();
            mBinding.undoBtn.setVisibility(View.INVISIBLE);
            mSuggestedCharacterListAdapter.notifyDataSetChanged();
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
                performSearch(true);
                return true;
            }
            return false;
        });

        loadRadicals();
    }

    private void setStateUI(StateUI stateUI) {
        if (stateUI == StateUI.DRAW) {
            setDrawUI();
        } else if (stateUI == StateUI.RESULTS) {
            setResultsUI();
            mBinding.searchTextBox.postDelayed(() ->
                    performSearch(true),
                    1000);
        } else if (stateUI == StateUI.PUZZLE){
            setPuzzleUI();
        }
    }

    private void setDrawUI() {
        mStateUI = StateUI.DRAW;
        mBinding.labelTv.setText(getString(R.string.drawing_mode));
        mBinding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_draw_active, getTheme()));
        mBinding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_puzzle_default, getTheme()));
        mBinding.undoBtn.setVisibility(View.VISIBLE);
        mBinding.doneBtn.setVisibility(View.INVISIBLE);
        mBinding.resultsRv.setVisibility(View.INVISIBLE);
        mBinding.radicalsRv.setVisibility(View.INVISIBLE);
        mBinding.charactersRv.setVisibility(View.VISIBLE);
        mBinding.radicalsBackBtn.setVisibility(View.INVISIBLE);
        mBinding.characterDrawCanvas.setVisibility(View.VISIBLE);
    }

    private void setResultsUI() {
        mStateUI = StateUI.RESULTS;
        mBinding.labelTv.setText(getString(R.string.searching_mode));
        mBinding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_draw_default, getTheme()));
        mBinding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_puzzle_default, getTheme()));
        mBinding.resultsRv.setVisibility(View.VISIBLE);
        mBinding.doneBtn.setVisibility(View.INVISIBLE);
        mBinding.undoBtn.setVisibility(View.INVISIBLE);
        mBinding.radicalsRv.setVisibility(View.INVISIBLE);
        mBinding.charactersRv.setVisibility(View.INVISIBLE);
        mBinding.radicalsBackBtn.setVisibility(View.INVISIBLE);
        mBinding.characterDrawCanvas.setVisibility(View.INVISIBLE);
    }

    private void setPuzzleUI() {
        mStateUI = StateUI.PUZZLE;
        mBinding.puzzleBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_puzzle_active, getTheme()));
        mBinding.drawBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_draw_default, getTheme()));
        mBinding.labelTv.setText(getString(R.string.puzzle_mode));
        mBinding.radicalsRv.setVisibility(View.VISIBLE);
        mBinding.undoBtn.setVisibility(View.INVISIBLE);
        mBinding.doneBtn.setVisibility(View.INVISIBLE);
        mBinding.resultsRv.setVisibility(View.INVISIBLE);
        mBinding.charactersRv.setVisibility(View.INVISIBLE);
        mBinding.radicalsBackBtn.setVisibility(View.INVISIBLE);
        mBinding.characterDrawCanvas.setVisibility(View.INVISIBLE);
        if (mRadicalChosen) {
            mBinding.radicalsBackBtn.setVisibility(View.VISIBLE);
        } else {
            mBinding.radicalsBackBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void loadRadicals() {
        mMainViewModel.getSection("0").observe(this, radicals -> {
            List<String[]> radicalsList = new ArrayList<>();
            for (int i = 0; i < radicals.size(); i++) {
                radicalsList.add(radicals.get(i).radicals.split(" "));
            }
            mMainViewModel.setRadicalsList(radicalsList);
        });
    }

    private void performSearch(boolean hideKeyboard) {

        String inputTextString;
        if (mBinding.searchTextBox.getText() != null) {
            inputTextString = mBinding.searchTextBox.getText().toString();
        } else {
            Log.e(LOG_TAG, "Search text box text is null in performSearch");
            return;
        }

        if (inputTextString.length() > 0) {
            if (!containsChinese(inputTextString)) {
                if (inputTextString.charAt(inputTextString.length() - 1) == ' ') {
                    inputTextString = inputTextString.substring(0, inputTextString.length() - 1);
                }
                mMainViewModel.searchByWordPL(inputTextString).observe(this, dictionaries -> {
                    List<String> searchResults = new ArrayList<>();
                    for (Dictionary dictionary : dictionaries) {
                        searchResults.add(dictionary.traditionalSign + "/" + dictionary.simplifiedSign
                                + "/" + dictionary.pronunciation + "/" + dictionary.pronunciationPhonetic
                                + "/" + dictionary.translation);
                    }
                    searchSwitch(searchResults, hideKeyboard);
                });
            } else {
                if (inputTextString.length() == 1) {
                    mMainViewModel.searchSingleCH(inputTextString).observe(this, dictionaries -> {
                        List<String> searchResults = new ArrayList<>();
                        for (Dictionary dictionary : dictionaries) {
                            searchResults.add(dictionary.traditionalSign + "/" + dictionary.simplifiedSign
                                    + "/" + dictionary.pronunciation + "/" + dictionary.pronunciationPhonetic
                                    + "/" + dictionary.translation);
                        }
                        searchSwitch(searchResults, hideKeyboard);
                    });
                } else {
                    mMainViewModel.searchByWordCH(inputTextString).observe(this, dictionaries -> {
                        List<String> searchResults = new ArrayList<>();
                        for (Dictionary dictionary : dictionaries) {
                            searchResults.add(dictionary.traditionalSign + "/" + dictionary.simplifiedSign
                                    + "/" + dictionary.pronunciation + "/" + dictionary.pronunciationPhonetic
                                    + "/" + dictionary.translation);
                        }
                        searchSwitch(searchResults, hideKeyboard);
                    });
                }
            }
        }
    }

    private void searchSwitch(List<String> searchResults, boolean hideKeyboard) {
        if (searchResults.size() > 0) {
            mMainViewModel.updateResults(searchResults);

            setResultsUI();
            if (hideKeyboard) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(mBinding.searchTextBox.getWindowToken(), 0);
                }
            }
            mBinding.searchTextBox.setSelection(mBinding.searchTextBox.length());

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
        if (mBinding.searchTextBox.getText() != null) {
            mBinding.searchTextBox.setText(getString(R.string.search_text_box_append_placeholder,
                    mBinding.searchTextBox.getText().subSequence(0, mMainViewModel.getCursorPosition()),
                    recognitionCandidatesList.get(0).getText()));
        } else {
            Log.e(LOG_TAG, "Search text box is null in onResults");
        }
        mBinding.searchTextBox.setSelection(mBinding.searchTextBox.getText().length());
        mBinding.doneBtn.setVisibility(View.VISIBLE);
        mBinding.charactersRv.setVisibility(View.VISIBLE);
        mBinding.undoBtn.setVisibility(View.VISIBLE);
        mSuggestedCharacterListAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemReleased(int position) {
        if (mBinding.searchTextBox.getText() != null && mMainViewModel.mCharactersList.getValue() != null) {
            mBinding.searchTextBox.setText(getString(R.string.search_text_box_append_placeholder,
                    mBinding.searchTextBox.getText().subSequence(0, mMainViewModel.getCursorPosition()),
                    mMainViewModel.mCharactersList.getValue().get(position)));
            mBinding.searchTextBox.setSelection(mBinding.searchTextBox.getText().length());
        } else {
            Log.e(LOG_TAG, "Search text box or characters list is null in onItemReleased");
        }
        mMainViewModel.setCursorPosition(mBinding.searchTextBox.getText().length());
        mMainViewModel.clearCharacterList();
        mBinding.doneBtn.setVisibility(View.INVISIBLE);
        mBinding.undoBtn.setVisibility(View.INVISIBLE);
        mBinding.characterDrawCanvas.clear();
        mSuggestedCharacterListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemPressed(View itemView) {
        itemView.setBackgroundColor(getColor(R.color.selection_blue));
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
        mMainViewModel.getSection(radical).observe(this, radicals -> {
            if(radicals.size() > 0) {
               List<String[]> radicalsList = new ArrayList<>();
               for (int i = 0; i < radicals.size(); i++) {
                    radicalsList.add(radicals.get(i).radicals.split(" "));
               }
               mMainViewModel.setRadicalsList(radicalsList);

               mRadicalChosen = true;
               setStateUI(StateUI.PUZZLE);

                getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        mMainViewModel.getSection("0").observe(MainActivity.this, radicals -> {
                            if(radicals.size() > 0) {
                                List<String[]> radicalsList = new ArrayList<>();
                                for (int i = 0; i < radicals.size(); i++) {
                                    radicalsList.add(radicals.get(i).radicals.split(" "));
                                }
                                mMainViewModel.setRadicalsList(radicalsList);
                                mRadicalChosen = false;
                                setStateUI(StateUI.PUZZLE);
                            }
                        });
                        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                finish();
                            }
                        });
                    }
                });
            } else {
                mBinding.searchTextBox.append(radical);
            }
        });
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
        if (mBinding.searchTextBox.getText() != null) {
            if (mStateUI == StateUI.RESULTS && mBinding.searchTextBox.getText().length() > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (!mSearchHandler.hasCallbacks(delayedSearch())) {
                        mSearchHandler.postDelayed(delayedSearch(), 200);
                    }
                } else {
                    if (!mSearchHandler.hasMessages(0)) {
                        mSearchHandler.postDelayed(delayedSearch(), 200);
                    }
                }
            }
        } else {
            Log.e(LOG_TAG, "Search text box text is null in onSelectionChanged");
        }
    }

    private Runnable delayedSearch() {
        return () -> performSearch(false);
    }
}