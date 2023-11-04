package com.wegielek.signalychinese.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.mlkit.vision.digitalink.RecognitionCandidate
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.adapters.RadicalsAdapter
import com.wegielek.signalychinese.adapters.SearchResultListAdapter
import com.wegielek.signalychinese.adapters.SuggestedCharacterListAdapter
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.database.Radicals
import com.wegielek.signalychinese.databinding.ActivityMainBinding
import com.wegielek.signalychinese.enums.StateUI
import com.wegielek.signalychinese.interfaces.CanvasViewListener
import com.wegielek.signalychinese.interfaces.CharactersRecyclerViewListener
import com.wegielek.signalychinese.interfaces.RadicalsRecyclerViewListener
import com.wegielek.signalychinese.interfaces.ResultsRecyclerViewListener
import com.wegielek.signalychinese.interfaces.SearchTextBoxListener
import com.wegielek.signalychinese.utils.Preferences.Companion.getMicLanguage
import com.wegielek.signalychinese.utils.Preferences.Companion.getSearchMode
import com.wegielek.signalychinese.utils.Utils.Companion.containsChinese
import com.wegielek.signalychinese.utils.Utils.Companion.hideKeyboard
import com.wegielek.signalychinese.utils.Utils.Companion.showMicLanguagePopup
import com.wegielek.signalychinese.utils.Utils.Companion.showSearchModePopup
import com.wegielek.signalychinese.viewmodels.MainViewModel

class MainActivity : AppCompatActivity(), CanvasViewListener,
    CharactersRecyclerViewListener, ResultsRecyclerViewListener, RadicalsRecyclerViewListener,
    SearchTextBoxListener {
    private lateinit var mSuggestedCharacterListAdapter: SuggestedCharacterListAdapter
    private lateinit var mSearchResultsListAdapter: SearchResultListAdapter
    private lateinit var mRadicalsAdapter: RadicalsAdapter
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mMainViewModel: MainViewModel
    private val mSearchHandler = Handler(Looper.getMainLooper())
    private var mStateUI: StateUI? = StateUI.DRAW

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val searchWord = intent.getStringExtra("searchWord")
        mMainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        installSplashScreen()
            .setKeepOnScreenCondition {
                if (searchWord == null) {
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed(
                        { mMainViewModel.setKeepSplashScreen(false) },
                        2000
                    )
                    return@setKeepOnScreenCondition mMainViewModel.isKeepSplashScreen()
                } else {
                    return@setKeepOnScreenCondition false
                }
            }
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mMainViewModel.dictionaryResultsList.observe(
            this
        ) { dictionaries: List<Dictionary> ->
            mSearchResultsListAdapter.setData(
                dictionaries
            )
        }
        mMainViewModel.charactersList.observe(
            this
        ) { stringList: List<String> ->
            mSuggestedCharacterListAdapter.setData(
                stringList
            )
        }
        mMainViewModel.radicalsList.observe(this
        ) { radicalsList: List<Array<String>> ->
            mRadicalsAdapter.setData(
                radicalsList
            )
        }
        if (savedInstanceState != null) {
            mStateUI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getSerializable("state", StateUI::class.java)
            } else {
                savedInstanceState.getSerializable("state") as StateUI?
            }
        }
        if (searchWord != null) {
            mBinding.searchTextBox.setText(searchWord.trim { it <= ' ' })
            setStateUI(StateUI.RESULTS)
            performSearch(true)
        } else {
            setStateUI(mStateUI)
        }
        mBinding.radicalsBackBtn.setOnClickListener {
            val future =
                mMainViewModel.getRadicalsSection("0")
            Futures.addCallback<List<Radicals>>(
                future,
                object : FutureCallback<List<Radicals>> {
                    override fun onSuccess(radicals: List<Radicals>) {
                        val radicalsList: MutableList<Array<String>> =
                            ArrayList()
                        for (i in radicals.indices) {
                            radicalsList.add(
                                radicals[i].radicals.split(" ".toRegex())
                                    .dropLastWhile { it.isEmpty() }
                                    .toTypedArray())
                        }
                        mMainViewModel.setRadicalsList(radicalsList)
                        mMainViewModel.setRadicalChosen(false)
                        setStateUI(StateUI.PUZZLE)
                        onBackPressedDispatcher.addCallback(object :
                            OnBackPressedCallback(true) {
                            override fun handleOnBackPressed() {
                                finish()
                            }
                        })
                    }

                    override fun onFailure(t: Throwable) {
                        t.printStackTrace()
                    }
                },
                ContextCompat.getMainExecutor(this)
            )
        }
        mBinding.searchTextBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (mBinding.searchTextBox.text != null) {
                    if (mStateUI === StateUI.RESULTS && mBinding.searchTextBox.text!!.isNotEmpty()
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (!mSearchHandler.hasCallbacks(delayedSearch())) {
                                mSearchHandler.postDelayed(delayedSearch(), 200)
                            }
                        } else {
                            if (!mSearchHandler.hasMessages(0)) {
                                mSearchHandler.postDelayed(delayedSearch(), 200)
                            }
                        }
                    }
                } else {
                    Log.e(LOG_TAG, "Search text box text is null in onSelectionChanged")
                }
            }
        })
        mBinding.searchTextBox.setOnSelectionChangedListener(this)
        mBinding.resultsRv.layoutManager = LinearLayoutManager(this)
        mSearchResultsListAdapter = SearchResultListAdapter(this, this)
        mBinding.resultsRv.adapter = mSearchResultsListAdapter
        mBinding.radicalsRv.layoutManager = LinearLayoutManager(this)
        mRadicalsAdapter = RadicalsAdapter(this, this)
        mBinding.radicalsRv.adapter = mRadicalsAdapter
        mBinding.charactersRv.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL,
            false
        )
        mSuggestedCharacterListAdapter = SuggestedCharacterListAdapter(this)
        mBinding.charactersRv.adapter = mSuggestedCharacterListAdapter
        mBinding.characterDrawCanvas.setOnRecognizeListener(this)
        mBinding.characterDrawCanvas.post {
            mBinding.characterDrawCanvas.initialize(
                mBinding.characterDrawCanvas.width,
                mBinding.characterDrawCanvas.height,
                mMainViewModel
            )
        }
        mBinding.undoBtn.setOnClickListener { mBinding.characterDrawCanvas.undoStroke() }
        mBinding.searchBtn.setOnClickListener { performSearch(true) }
        mBinding.searchBtn.setOnLongClickListener { v ->
            showSearchModePopup(v)
            true
        }
        mBinding.puzzleBtn.setOnClickListener { setStateUI(StateUI.PUZZLE) }
        mBinding.drawBtn.setOnClickListener { setStateUI(StateUI.DRAW) }
        mBinding.learnBtn.setOnClickListener {
            val intent = Intent(baseContext, FlashCardsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }
        mBinding.backspaceBtn.setOnClickListener {
            if (mBinding.searchTextBox.text != null) {
                mBinding.searchTextBox.dispatchKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DEL
                    )
                )
                mMainViewModel.cursorPosition = mBinding.searchTextBox.selectionEnd
                if (mBinding.searchTextBox.text!!.isEmpty() && mStateUI !== StateUI.PUZZLE) {
                    setStateUI(StateUI.DRAW)
                    mSearchHandler.removeCallbacks(delayedSearch())
                }
                mBinding.doneBtn.visibility = View.INVISIBLE
                mBinding.characterDrawCanvas.clear()
                mBinding.undoBtn.visibility = View.INVISIBLE
            } else {
                Log.e(
                    LOG_TAG,
                    "Search text box text is null in backspaceBtn.onClick"
                )
            }
        }
        mBinding.backspaceBtn.setOnLongClickListener {
            mBinding.searchTextBox.setText("")
            mMainViewModel.cursorPosition = 0
            setStateUI(StateUI.DRAW)
            true
        }
        mBinding.micBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                // Set the language to Chinese (Mandarin)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    getMicLanguage(
                        applicationContext
                    )
                )
                // Specify the prompt message
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...")
                startActivityForResult(intent, RECOGNIZER_RESULT)
            }
        }
        mBinding.micBtn.setOnLongClickListener { v ->
            showMicLanguagePopup(v)
            true
        }
        mBinding.doneBtn.setOnClickListener {
            mMainViewModel.clearCharacterList()
            mBinding.doneBtn.visibility = View.INVISIBLE
            if (mBinding.searchTextBox.text != null) {
                mBinding.searchTextBox.text?.let { mBinding.searchTextBox.setSelection(it.length) }
                mMainViewModel.cursorPosition = mBinding.searchTextBox.text!!.length
            } else {
                Log.e(
                    LOG_TAG,
                    "Search text box text is null in doneBtn.onClick"
                )
            }
            mBinding.characterDrawCanvas.clear()
            mBinding.undoBtn.visibility = View.INVISIBLE
            mSuggestedCharacterListAdapter.notifyDataSetChanged()
        }
        mBinding.settingsBtn.setOnClickListener {
            val intent = Intent(baseContext, HamburgerActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            if (Build.VERSION.SDK_INT >= 34) {
                overrideActivityTransition(
                    OVERRIDE_TRANSITION_OPEN,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            } else {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
        mBinding.cameraBtn.setOnClickListener {
            val intent = Intent(baseContext, CameraActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            if (Build.VERSION.SDK_INT >= 34) {
                overrideActivityTransition(
                    OVERRIDE_TRANSITION_OPEN,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            } else {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
        mBinding.searchTextBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId === EditorInfo.IME_ACTION_SEARCH) {
                performSearch(true)
                return@setOnEditorActionListener true
            }
            false
        }
        mBinding.searchTextBox.setOnFocusChangeListener { v, hasFocus -> }
        loadRadicals()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RECOGNIZER_RESULT && resultCode == RESULT_OK) {
            val result = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            mBinding.searchTextBox.setText(result!![0])
            mBinding.searchTextBox.text?.let { mBinding.searchTextBox.setSelection(it.length) }
            mMainViewModel.cursorPosition = mBinding.searchTextBox.text!!.length
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setStateUI(stateUI: StateUI?) {
        if (stateUI === StateUI.DRAW) {
            setDrawUI()
        } else if (stateUI === StateUI.RESULTS) {
            setResultsUI()
            mBinding.searchTextBox.postDelayed(
                { performSearch(true) },
                1000
            )
        } else if (stateUI === StateUI.PUZZLE) {
            setPuzzleUI()
        }
    }

    private fun setDrawUI() {
        mStateUI = StateUI.DRAW
        mBinding.labelTv.text = getString(R.string.drawing_mode)
        mBinding.drawBtn.setImageResource(R.drawable.ic_draw_active)
        mBinding.puzzleBtn.setImageResource(R.drawable.ic_puzzle_default)
        mBinding.undoBtn.visibility = View.VISIBLE
        mBinding.doneBtn.visibility = View.INVISIBLE
        mBinding.resultsRv.visibility = View.INVISIBLE
        mBinding.radicalsRv.visibility = View.INVISIBLE
        mBinding.charactersRv.visibility = View.VISIBLE
        mBinding.radicalsBackBtn.visibility = View.INVISIBLE
        mBinding.characterDrawCanvas.visibility = View.VISIBLE
    }

    private fun setResultsUI() {
        mStateUI = StateUI.RESULTS
        mBinding.labelTv.text = getString(R.string.searching_mode)
        mBinding.drawBtn.setImageResource(R.drawable.ic_draw_default)
        mBinding.puzzleBtn.setImageResource(R.drawable.ic_puzzle_default)
        mBinding.resultsRv.visibility = View.VISIBLE
        mBinding.doneBtn.visibility = View.INVISIBLE
        mBinding.undoBtn.visibility = View.INVISIBLE
        mBinding.radicalsRv.visibility = View.INVISIBLE
        mBinding.charactersRv.visibility = View.INVISIBLE
        mBinding.radicalsBackBtn.visibility = View.INVISIBLE
        mBinding.characterDrawCanvas.visibility = View.INVISIBLE
    }

    private fun setPuzzleUI() {
        mStateUI = StateUI.PUZZLE
        mBinding.puzzleBtn.setImageResource(R.drawable.ic_puzzle_active)
        mBinding.drawBtn.setImageResource(R.drawable.ic_draw_default)
        mBinding.labelTv.text = getString(R.string.puzzle_mode)
        mBinding.radicalsRv.visibility = View.VISIBLE
        mBinding.undoBtn.visibility = View.INVISIBLE
        mBinding.doneBtn.visibility = View.INVISIBLE
        mBinding.resultsRv.visibility = View.INVISIBLE
        mBinding.charactersRv.visibility = View.INVISIBLE
        mBinding.radicalsBackBtn.visibility = View.INVISIBLE
        mBinding.characterDrawCanvas.visibility = View.INVISIBLE
        if (mMainViewModel.radicalChosen!!) {
            mBinding.radicalsBackBtn.visibility = View.VISIBLE
            mBinding.labelTv.setText(R.string.puzzle_chosen_mode)
        } else {
            mBinding.radicalsBackBtn.visibility = View.INVISIBLE
        }
    }

    private fun loadRadicals() {
        val future = mMainViewModel.radicalChosenCharacter?.let {
            mMainViewModel.getRadicalsSection(
                it
            )
        }
        Futures.addCallback(future, object : FutureCallback<List<Radicals>> {
            override fun onSuccess(radicals: List<Radicals>) {
                val radicalsList: MutableList<Array<String>> = ArrayList()
                for (i in radicals.indices) {
                    radicalsList.add(
                        radicals[i].radicals.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray())
                }
                mMainViewModel.setRadicalsList(radicalsList)
            }

            override fun onFailure(t: Throwable) {
                t.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun performSearch(hideKeyboard: Boolean) {
        val inputTextString: String = if (mBinding.searchTextBox.text != null) {
            mBinding.searchTextBox.text.toString().trim()
        } else {
            Log.e(LOG_TAG, "Search text box text is null in performSearch")
            return
        }
        if (getSearchMode(this) == "normal") {
            if (inputTextString.isNotEmpty()) {
                if (!containsChinese(inputTextString) && inputTextString.length > 1) {
                    val future = mMainViewModel.searchByWordPL(inputTextString)
                    Futures.addCallback(future, object : FutureCallback<List<Dictionary>> {
                        override fun onSuccess(dictionaries: List<Dictionary>) {
                            searchSwitch(dictionaries, hideKeyboard)
                        }

                        override fun onFailure(t: Throwable) {
                            t.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(this))
                } else {
                    if (inputTextString.length == 1) {
                        val future = mMainViewModel.searchSingleCH(inputTextString)
                        Futures.addCallback(future, object : FutureCallback<List<Dictionary>> {
                            override fun onSuccess(dictionaries: List<Dictionary>) {
                                searchSwitch(dictionaries, hideKeyboard)
                            }

                            override fun onFailure(t: Throwable) {
                                t.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(this))
                    } else {
                        val future = mMainViewModel.searchByWordCH(inputTextString)
                        Futures.addCallback(future, object : FutureCallback<List<Dictionary>> {
                            override fun onSuccess(dictionaries: List<Dictionary>) {
                                searchSwitch(dictionaries, hideKeyboard)
                            }

                            override fun onFailure(t: Throwable) {
                                t.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(this))
                    }
                }
            } else {
                Toast.makeText(this, "Please provide input", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (inputTextString.isNotEmpty()) {
                if (!containsChinese(inputTextString) && inputTextString.length > 1) {
                    val future = mMainViewModel.searchByWordPLAll(inputTextString)
                    Futures.addCallback(future, object : FutureCallback<List<Dictionary>> {
                        override fun onSuccess(dictionaries: List<Dictionary>) {
                            searchSwitch(dictionaries, hideKeyboard)
                        }

                        override fun onFailure(t: Throwable) {
                            t.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(this))
                } else {
                    val future = mMainViewModel.searchByWordCHAll(inputTextString)
                    Futures.addCallback(future, object : FutureCallback<List<Dictionary>> {
                        override fun onSuccess(dictionaries: List<Dictionary>) {
                            searchSwitch(dictionaries, hideKeyboard)
                        }

                        override fun onFailure(t: Throwable) {
                            t.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(this))
                }
            } else {
                Toast.makeText(this, "Please provide input", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchSwitch(searchResults: List<Dictionary>, hideKeyboard: Boolean) {
        if (searchResults.isNotEmpty()) {
            mMainViewModel.updateResults(searchResults)
            setResultsUI()
            if (hideKeyboard) {
                hideKeyboard(this, mBinding.searchTextBox)
            }
            mBinding.searchTextBox.setSelection(mBinding.searchTextBox.length())
        } else if (mStateUI !== StateUI.RESULTS) {
            Toast.makeText(this, getString(R.string.no_results), Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResults(recognitionCandidatesList: List<RecognitionCandidate>) {
        mMainViewModel.clearCharacterList()
        for (rc in recognitionCandidatesList) {
            mMainViewModel.addToCharacterList(rc.text)
        }
        if (mBinding.searchTextBox.text != null) {
            mBinding.searchTextBox.setText(
                getString(
                    R.string.search_text_box_append_placeholder,
                    mMainViewModel.cursorPosition.let {
                        mBinding.searchTextBox.text!!
                            .subSequence(0, it)
                    },
                    recognitionCandidatesList[0].text
                )
            )
        } else {
            Log.e(LOG_TAG, "Search text box is null in onResults")
        }
        mBinding.searchTextBox.text?.let { mBinding.searchTextBox.setSelection(it.length) }
        mBinding.doneBtn.visibility = View.VISIBLE
        mBinding.charactersRv.visibility = View.VISIBLE
        mBinding.undoBtn.visibility = View.VISIBLE
        mSuggestedCharacterListAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemReleased(position: Int) {
        if (mBinding.searchTextBox.text != null && mMainViewModel.charactersList.value != null) {
            mBinding.searchTextBox.setText(
                getString(
                    R.string.search_text_box_append_placeholder,
                    mMainViewModel.cursorPosition.let {
                        mBinding.searchTextBox.text!!
                            .subSequence(0, it)
                    },
                    mMainViewModel.charactersList.value!![position]
                )
            )
            mBinding.searchTextBox.setSelection(mBinding.searchTextBox.text!!.length)
        } else {
            Log.e(LOG_TAG, "Search text box or characters list is null in onItemReleased")
        }
        mMainViewModel.cursorPosition = mBinding.searchTextBox.text!!.length
        mMainViewModel.clearCharacterList()
        mBinding.doneBtn.visibility = View.INVISIBLE
        mBinding.undoBtn.visibility = View.INVISIBLE
        mBinding.characterDrawCanvas.clear()
        mSuggestedCharacterListAdapter.notifyDataSetChanged()
    }

    override fun onItemPressed(itemView: View) {
        itemView.setBackgroundColor(getColor(R.color.selection_color))
        itemView.postDelayed({ itemView.setBackgroundColor(Color.TRANSPARENT) }, 500)
    }

    override fun onItemCanceled(itemView: View) {
        itemView.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onResultClicked(position: Int) {
        val intent = Intent(baseContext, DefinitionWordActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra("word", mMainViewModel.getResult(position))
        startActivity(intent)
    }

    override fun onRadicalClicked(radical: String) {
        if (mMainViewModel.radicalChosen!!) {
            mBinding.searchTextBox.append(radical)
        } else {
            val future = mMainViewModel.getRadicalsSection(radical)
            Futures.addCallback<List<Radicals>>(future, object : FutureCallback<List<Radicals>> {
                override fun onSuccess(radicals: List<Radicals>) {
                    if (radicals.isNotEmpty()) {
                        val radicalsList: MutableList<Array<String>> = ArrayList()
                        for (i in radicals.indices) {
                            radicalsList.add(
                                radicals[i].radicals.split(" ".toRegex())
                                    .dropLastWhile { it.isEmpty() }
                                    .toTypedArray())
                        }
                        mMainViewModel.setRadicalsList(radicalsList)
                        mMainViewModel.setRadicalChosen(true)
                        setStateUI(StateUI.PUZZLE)
                        onBackPressedDispatcher.addCallback(object :
                            OnBackPressedCallback(true) {
                            override fun handleOnBackPressed() {
                                val future1 = mMainViewModel.getRadicalsSection("0")
                                Futures.addCallback(
                                    future1,
                                    object : FutureCallback<List<Radicals>> {
                                        override fun onSuccess(radicals1: List<Radicals>) {
                                            if (radicals1.isNotEmpty()) {
                                                val radicalsList: MutableList<Array<String>> =
                                                    ArrayList()
                                                for (i in radicals1.indices) {
                                                    radicalsList.add(
                                                        radicals1[i].radicals.split(" ".toRegex())
                                                            .dropLastWhile { it.isEmpty() }
                                                            .toTypedArray())
                                                }
                                                mMainViewModel.setRadicalsList(radicalsList)
                                                mMainViewModel.setRadicalChosen(false)
                                                setStateUI(StateUI.PUZZLE)
                                            }
                                        }

                                        override fun onFailure(t: Throwable) {
                                            t.printStackTrace()
                                        }
                                    },
                                    ContextCompat.getMainExecutor(applicationContext)
                                )
                                onBackPressedDispatcher.addCallback(object :
                                    OnBackPressedCallback(true) {
                                    override fun handleOnBackPressed() {
                                        finish()
                                    }
                                })
                            }
                        })
                    } else {
                        mBinding.searchTextBox.append(radical)
                    }
                }

                override fun onFailure(t: Throwable) {
                    t.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(this))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("state", mStateUI)
        super.onSaveInstanceState(outState)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {}
    private fun delayedSearch(): Runnable {
        return Runnable { performSearch(false) }
    }

    companion object {
        private const val LOG_TAG = "MainActivity"
        private const val RECOGNIZER_RESULT = 1
    }
}