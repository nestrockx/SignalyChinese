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
import android.view.MotionEvent
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
import com.wegielek.signalychinese.adapters.SearchResultsAdapter
import com.wegielek.signalychinese.adapters.SuggestedCharactersAdapter
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.database.FlashCards
import com.wegielek.signalychinese.database.Radicals
import com.wegielek.signalychinese.databinding.ActivityMainBinding
import com.wegielek.signalychinese.enums.StateUI
import com.wegielek.signalychinese.interfaces.CanvasViewListener
import com.wegielek.signalychinese.interfaces.CharactersRecyclerViewListener
import com.wegielek.signalychinese.interfaces.RadicalsRecyclerViewListener
import com.wegielek.signalychinese.interfaces.ResultsRecyclerViewListener
import com.wegielek.signalychinese.interfaces.SearchTextBoxListener
import com.wegielek.signalychinese.utils.Preferences
import com.wegielek.signalychinese.utils.Preferences.Companion.getMicLanguage
import com.wegielek.signalychinese.utils.Preferences.Companion.getSearchMode
import com.wegielek.signalychinese.utils.TextToSpeechManager
import com.wegielek.signalychinese.utils.Utils.Companion.containsChinese
import com.wegielek.signalychinese.utils.Utils.Companion.hideKeyboard
import com.wegielek.signalychinese.utils.Utils.Companion.showMicLanguagePopup
import com.wegielek.signalychinese.utils.Utils.Companion.showSearchModePopup
import com.wegielek.signalychinese.viewmodels.MainViewModel

class MainActivity : AppCompatActivity(), CanvasViewListener,
    CharactersRecyclerViewListener, ResultsRecyclerViewListener, RadicalsRecyclerViewListener,
    SearchTextBoxListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mSuggestedCharactersAdapter: SuggestedCharactersAdapter
    private lateinit var mSearchResultsListAdapter: SearchResultsAdapter
    private lateinit var mRadicalsAdapter: RadicalsAdapter
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
                        1500
                    )
                    return@setKeepOnScreenCondition mMainViewModel.isKeepSplashScreen()
                } else {
                    return@setKeepOnScreenCondition false
                }
            }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            mSuggestedCharactersAdapter.setData(
                stringList
            )
        }
        mMainViewModel.radicalsList.observe(
            this
        ) { radicalsList: List<Array<String>> ->
            mRadicalsAdapter.setData(
                radicalsList
            )
        }
        if (savedInstanceState != null) {
            mStateUI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getSerializable("state", StateUI::class.java)
            } else {
                @Suppress("DEPRECATION")
                savedInstanceState.getSerializable("state") as StateUI?
            }
        }
        if (searchWord != null) {
            binding.searchTextBox.setText(searchWord.trim { it <= ' ' })
            setStateUI(StateUI.RESULTS)
            performSearch(true)
        } else {
            setStateUI(mStateUI)
        }
        binding.radicalsBackBtn.setOnClickListener {
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
        binding.searchTextBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (binding.searchTextBox.text != null) {
                    if (mStateUI === StateUI.RESULTS && binding.searchTextBox.text!!.isNotEmpty()
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
        binding.searchTextBox.setOnSelectionChangedListener(this)
        binding.resultsRv.layoutManager = LinearLayoutManager(this)
        mSearchResultsListAdapter = SearchResultsAdapter(this, this)
        binding.resultsRv.adapter = mSearchResultsListAdapter
        binding.radicalsRv.layoutManager = LinearLayoutManager(this)
        mRadicalsAdapter = RadicalsAdapter(this, this)
        binding.radicalsRv.adapter = mRadicalsAdapter
        binding.charactersRv.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL,
            false
        )
        mSuggestedCharactersAdapter = SuggestedCharactersAdapter(this)
        binding.charactersRv.adapter = mSuggestedCharactersAdapter
        binding.characterDrawCanvas.setOnRecognizeListener(this)
        binding.characterDrawCanvas.post {
            binding.characterDrawCanvas.initialize(
                binding.characterDrawCanvas.width,
                binding.characterDrawCanvas.height,
                mMainViewModel
            )
        }
        binding.undoBtn.setOnClickListener { binding.characterDrawCanvas.undoStroke() }
        binding.searchBtn.setOnClickListener { performSearch(true) }
        binding.searchBtn.setOnLongClickListener { v ->
            showSearchModePopup(v)
            true
        }
        binding.puzzleBtn.setOnClickListener { setStateUI(StateUI.PUZZLE) }
        binding.drawBtn.setOnClickListener { setStateUI(StateUI.DRAW) }
        binding.learnBtn.setOnClickListener {
            val intent = Intent(baseContext, SchoolActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }
        binding.backspaceBtn.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.backspaceBtn.post(backspaceHold)
            } else if (event.action == MotionEvent.ACTION_UP) {
                binding.backspaceBtn.removeCallbacks(backspaceHold)
                v.callOnClick()
            }
            false
        }
        /*
        mBinding.backspaceBtn.setOnLongClickListener {
            mBinding.searchTextBox.setText("")
            mMainViewModel.cursorPosition = 0
            mBinding.characterDrawCanvas.clear()
            setStateUI(StateUI.DRAW)
            true
        }
        */

        binding.refreshBtn.visibility = View.INVISIBLE
        binding.refreshBtn.setOnClickListener {
            binding.characterDrawCanvas.post {
                binding.characterDrawCanvas.initialize(
                    binding.characterDrawCanvas.width,
                    binding.characterDrawCanvas.height,
                    mMainViewModel
                )
            }
            binding.refreshBtn.visibility = View.INVISIBLE
            binding.charactersLoadingPb.visibility = View.VISIBLE
            binding.downloadingTv.text = getString(R.string.downloading)
        }

        binding.micBtn.setOnClickListener {
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
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    getMicLanguage()
                )
                // Specify the prompt message
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.say_something))
                startActivityForResult(intent, RECOGNIZER_RESULT)
            } else {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            }
        }
        binding.micBtn.setOnLongClickListener { v ->
            showMicLanguagePopup(v)
            true
        }
        binding.doneBtn.setOnClickListener {
            mMainViewModel.clearCharacterList()
            binding.doneBtn.visibility = View.INVISIBLE
            if (binding.searchTextBox.text != null) {
                binding.searchTextBox.text?.let { binding.searchTextBox.setSelection(it.length) }
                mMainViewModel.cursorPosition = binding.searchTextBox.text!!.length
            } else {
                Log.e(
                    LOG_TAG,
                    "Search text box text is null in doneBtn.onClick"
                )
            }
            binding.characterDrawCanvas.clear()
            binding.undoBtn.visibility = View.INVISIBLE
            mSuggestedCharactersAdapter.notifyDataSetChanged()
        }
        binding.settingsBtn.setOnClickListener {
            val intent = Intent(baseContext, HamburgerActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            if (Build.VERSION.SDK_INT >= 34) {
                overrideActivityTransition(
                    OVERRIDE_TRANSITION_OPEN,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                @Suppress("DEPRECATION")
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right, R.color.dark_mode_black)
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
        binding.cameraBtn.setOnClickListener {
            val intent = Intent(baseContext, CameraActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            if (Build.VERSION.SDK_INT >= 34) {
                overrideActivityTransition(
                    OVERRIDE_TRANSITION_OPEN,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                @Suppress("DEPRECATION")
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left, R.color.dark_mode_black)
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
        binding.searchTextBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(true)
                return@setOnEditorActionListener true
            }
            false
        }
        binding.searchTextBox.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.hasFocus()
            } else {
                !v.hasFocus()
            }
        }
        loadRadicals()

        TextToSpeechManager.instanceCH
        TextToSpeechManager.instancePL

        if (!Preferences.isDefaultFlashCardGroupSetup()) {
            val flashCards = FlashCards()
            flashCards.group = getString(R.string.saved)
            flashCards.traditionalSign = "字"
            flashCards.simplifiedSign = "(字)"
            flashCards.pronunciation = "zì "
            flashCards.pronunciationPhonetic = "zi "
            flashCards.translation = "litera / symbol / znak / słowo / CL:個|个 / tytuł lub nazwa stylu tradycyjnie nadawana mężczyznom w wieku 20 lat w dynastycznych Chinach "
            mMainViewModel.addFlashCardToGroup(flashCards)
            Preferences.setDefaultFlashCardGroupSetup(true)
        }
    }

    private val backspaceHold: Runnable = object : Runnable {
        override fun run() {
            if (binding.searchTextBox.text != null) {
                binding.searchTextBox.dispatchKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DEL
                    )
                )
                mMainViewModel.cursorPosition = binding.searchTextBox.selectionEnd
                if (binding.searchTextBox.text!!.isEmpty() && mStateUI !== StateUI.PUZZLE) {
                    setStateUI(StateUI.DRAW)
                    mSearchHandler.removeCallbacks(delayedSearch())
                }
                binding.doneBtn.visibility = View.INVISIBLE
                binding.characterDrawCanvas.clear()
                binding.undoBtn.visibility = View.INVISIBLE
            }
            binding.backspaceBtn.postDelayed(this, 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RECOGNIZER_RESULT && resultCode == RESULT_OK) {
            val result = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            binding.searchTextBox.setText(result!![0])
            binding.searchTextBox.text?.let { binding.searchTextBox.setSelection(it.length) }
            mMainViewModel.cursorPosition = binding.searchTextBox.text!!.length
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setStateUI(stateUI: StateUI?) {
        if (stateUI === StateUI.DRAW) {
            setDrawUI()
        } else if (stateUI === StateUI.RESULTS) {
            setResultsUI()
            binding.searchTextBox.postDelayed(
                { performSearch(true) },
                1000
            )
        } else if (stateUI === StateUI.PUZZLE) {
            setPuzzleUI()
        }
    }

    private fun setDrawUI() {
        mStateUI = StateUI.DRAW
        binding.labelTv.text = getString(R.string.drawing_mode)
        binding.drawBtn.setImageResource(R.drawable.ic_draw_active)
        binding.puzzleBtn.setImageResource(R.drawable.ic_puzzle_default)
        binding.undoBtn.visibility = View.VISIBLE
        binding.doneBtn.visibility = View.INVISIBLE
        binding.resultsRv.visibility = View.INVISIBLE
        binding.radicalsRv.visibility = View.INVISIBLE
        binding.charactersRv.visibility = View.VISIBLE
        binding.radicalsBackBtn.visibility = View.INVISIBLE
        binding.characterDrawCanvas.visibility = View.VISIBLE
    }

    private fun setResultsUI() {
        mStateUI = StateUI.RESULTS
        binding.labelTv.text = getString(R.string.searching_mode)
        binding.drawBtn.setImageResource(R.drawable.ic_draw_default)
        binding.puzzleBtn.setImageResource(R.drawable.ic_puzzle_default)
        binding.resultsRv.visibility = View.VISIBLE
        binding.doneBtn.visibility = View.INVISIBLE
        binding.undoBtn.visibility = View.INVISIBLE
        binding.radicalsRv.visibility = View.INVISIBLE
        binding.charactersRv.visibility = View.INVISIBLE
        binding.radicalsBackBtn.visibility = View.INVISIBLE
        binding.characterDrawCanvas.visibility = View.INVISIBLE
        binding.drawCharacterText.visibility = View.INVISIBLE
    }

    private fun setPuzzleUI() {
        mStateUI = StateUI.PUZZLE
        binding.puzzleBtn.setImageResource(R.drawable.ic_puzzle_active)
        binding.drawBtn.setImageResource(R.drawable.ic_draw_default)
        binding.labelTv.text = getString(R.string.puzzle_mode)
        binding.radicalsRv.visibility = View.VISIBLE
        binding.undoBtn.visibility = View.INVISIBLE
        binding.doneBtn.visibility = View.INVISIBLE
        binding.resultsRv.visibility = View.INVISIBLE
        binding.charactersRv.visibility = View.INVISIBLE
        binding.radicalsBackBtn.visibility = View.INVISIBLE
        binding.characterDrawCanvas.visibility = View.INVISIBLE
        if (mMainViewModel.radicalChosen!!) {
            binding.radicalsBackBtn.visibility = View.VISIBLE
            binding.labelTv.setText(R.string.puzzle_chosen_mode)
        } else {
            binding.radicalsBackBtn.visibility = View.INVISIBLE
        }
        binding.drawCharacterText.visibility = View.INVISIBLE
    }

    private fun loadRadicals() {
        val future = mMainViewModel.radicalChosenCharacter?.let {
            mMainViewModel.getRadicalsSection(
                it
            )
        }
        if (future != null) {
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
    }

    private fun performSearch(hideKeyboard: Boolean) {
        val inputTextString: String = if (binding.searchTextBox.text != null) {
            binding.searchTextBox.text.toString().trim()
        } else {
            Log.e(LOG_TAG, "Search text box text is null in performSearch")
            return
        }
        if (getSearchMode() == "normal") {
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
                Toast.makeText(this, getString(R.string.provide_input), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, getString(R.string.provide_input), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchSwitch(searchResults: List<Dictionary>, hideKeyboard: Boolean) {
        if (searchResults.isNotEmpty()) {
            mMainViewModel.updateResults(searchResults)
            setResultsUI()
            if (hideKeyboard) {
                hideKeyboard(this, binding.searchTextBox)
            }
            binding.searchTextBox.setSelection(binding.searchTextBox.length())
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
        if (binding.searchTextBox.text != null) {
            binding.searchTextBox.setText(
                getString(
                    R.string.search_text_box_append_placeholder,
                    mMainViewModel.cursorPosition.let {
                        binding.searchTextBox.text!!
                            .subSequence(0, it)
                    },
                    recognitionCandidatesList[0].text
                )
            )
        } else {
            Log.e(LOG_TAG, "Search text box is null in onResults")
        }
        binding.searchTextBox.text?.let { binding.searchTextBox.setSelection(it.length) }
        binding.doneBtn.visibility = View.VISIBLE
        binding.charactersRv.visibility = View.VISIBLE
        binding.undoBtn.visibility = View.VISIBLE
        mSuggestedCharactersAdapter.notifyDataSetChanged()
    }

    override fun onModelDownloaded(noInternet: Boolean) {
        binding.charactersLoadingPb.visibility = View.INVISIBLE
        binding.downloadingTv.visibility = View.INVISIBLE
        if (noInternet) {
            binding.downloadingTv.visibility = View.VISIBLE
            binding.downloadingTv.text = getString(R.string.no_internet)
            binding.refreshBtn.visibility = View.VISIBLE
            //Toast.makeText(this, "no internet", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInput() {
        binding.drawCharacterText.visibility = View.INVISIBLE
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemReleased(position: Int) {
        if (binding.searchTextBox.text != null && mMainViewModel.charactersList.value != null) {
            binding.searchTextBox.setText(
                getString(
                    R.string.search_text_box_append_placeholder,
                    mMainViewModel.cursorPosition.let {
                        binding.searchTextBox.text!!
                            .subSequence(0, it)
                    },
                    mMainViewModel.charactersList.value!![position]
                )
            )
            binding.searchTextBox.setSelection(binding.searchTextBox.text!!.length)
        } else {
            Log.e(LOG_TAG, "Search text box or characters list is null in onItemReleased")
        }
        mMainViewModel.cursorPosition = binding.searchTextBox.text!!.length
        mMainViewModel.clearCharacterList()
        binding.doneBtn.visibility = View.INVISIBLE
        binding.undoBtn.visibility = View.INVISIBLE
        binding.characterDrawCanvas.clear()
        mSuggestedCharactersAdapter.notifyDataSetChanged()
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
            binding.searchTextBox.append(radical)
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
                                                val radicals1List: MutableList<Array<String>> =
                                                    ArrayList()
                                                for (i in radicals1.indices) {
                                                    radicals1List.add(
                                                        radicals1[i].radicals.split(" ".toRegex())
                                                            .dropLastWhile { it.isEmpty() }
                                                            .toTypedArray())
                                                }
                                                mMainViewModel.setRadicalsList(radicals1List)
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
                        binding.searchTextBox.append(radical)
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