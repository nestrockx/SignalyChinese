package com.wegielek.signalychinese.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Path
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.digitalink.Ink
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.database.FlashCards
import com.wegielek.signalychinese.database.History
import com.wegielek.signalychinese.database.Radicals
import com.wegielek.signalychinese.repository.DictionaryRepository
import java.sql.Timestamp
import java.text.SimpleDateFormat

class MainViewModel(application: Application) : AndroidViewModel(application) {
    var charactersList = MutableLiveData<List<String>>()
    var dictionaryResultsList = MutableLiveData<List<Dictionary>>()
    var radicalsList = MutableLiveData<List<Array<String>>>()
    private val mStrokeBuilder = MutableLiveData<Ink.Stroke.Builder>()
    private val mStrokesHistory = MutableLiveData<ArrayList<Ink.Stroke.Builder>>()
    private val mInkBuilder = MutableLiveData<Ink.Builder>()
    private val mCurrentVisibleStroke = MutableLiveData<Path>()
    private val mVisibleStrokesHistory = MutableLiveData<ArrayList<Path>>()
    private val mCursorPosition = MutableLiveData<Int>()
    private val isRadicalChosen = MutableLiveData<Boolean>()
    private val mRadicalChosenCharacter = MutableLiveData<String>()
    private val mKeepSplashScreen = MutableLiveData<Boolean>()
    private val mDictionaryRepository: DictionaryRepository

    init {
        mDictionaryRepository = DictionaryRepository(application)
        mStrokeBuilder.value = Ink.Stroke.builder()
        mInkBuilder.value = Ink.builder()
        mStrokesHistory.value = ArrayList()
        dictionaryResultsList.value = ArrayList()
        charactersList.value = ArrayList()
        mVisibleStrokesHistory.value = ArrayList()
        mCurrentVisibleStroke.value = Path()
        mCursorPosition.value = 0
        mRadicalChosenCharacter.value = "0"
        radicalsList.value = ArrayList()
        isRadicalChosen.value = false
        mKeepSplashScreen.value = true
    }

    fun addFlashCardToGroup(flashCards: FlashCards) {
        mDictionaryRepository.addFlashCardToGroup(flashCards)
    }

    fun setKeepSplashScreen(keepSplashScreen: Boolean) {
        this.mKeepSplashScreen.value = keepSplashScreen
    }

    fun isKeepSplashScreen(): Boolean {
        return mKeepSplashScreen.value!!
    }

    fun getRadicalsSection(section: String): ListenableFuture<List<Radicals>> {
        setRadicalChosenCharacter(section)
        return mDictionaryRepository.getRadicalsSection(section)
    }

    fun searchSingleCH(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return mDictionaryRepository.searchSingleCH(searchQuery)
    }

    fun searchByWordCH(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return mDictionaryRepository.searchByWordCH(searchQuery)
    }

    fun searchByWordCHAll(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return mDictionaryRepository.searchByWordCHAll(searchQuery)
    }

    fun searchByWordPL(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return mDictionaryRepository.searchByWordPL(searchQuery)
    }

    fun searchByWordPLAll(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return mDictionaryRepository.searchByWordPLAll(searchQuery)
    }

    val radicalChosenCharacter: String?
        get() = mRadicalChosenCharacter.value

    private fun setRadicalChosenCharacter(radicalChosenCharacter: String) {
        mRadicalChosenCharacter.value = radicalChosenCharacter
    }

    fun getResult(index: Int): Dictionary {
        val dictionary = dictionaryResultsList.value!![index]
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val timestamp = Timestamp(System.currentTimeMillis())
        val ts = sdf.format(timestamp)
        Log.i("TIME", ts)
        val history = History()
        history.time = ts
        history.traditionalSign = dictionary.traditionalSign
        history.simplifiedSign = dictionary.simplifiedSign
        history.pronunciation = dictionary.pronunciation
        history.pronunciationPhonetic = dictionary.pronunciationPhonetic
        history.translation = dictionary.translation
        mDictionaryRepository.addHistoryRecord(history).addListener(
            {
                Log.d(
                    "MainViewModel",
                    "History record added successfully"
                )
            },
            ContextCompat.getMainExecutor(getApplication<Application>().applicationContext)
        )
        return dictionaryResultsList.value!![index]
    }

    fun setRadicalChosen(isRadicalChosen: Boolean) {
        this.isRadicalChosen.value = isRadicalChosen
    }

    val radicalChosen: Boolean?
        get() = isRadicalChosen.value

    fun updateResults(searchResults: List<Dictionary>) {
        dictionaryResultsList.value = searchResults
    }

    fun addToCharacterList(text: String) {
        if (charactersList.value != null) {
            val characterListCopy = ArrayList(charactersList.value!!)
            characterListCopy.add(text)
            charactersList.value = characterListCopy
        }
    }

    fun clearCharacterList() {
        charactersList.value = emptyList()
    }

    var cursorPosition: Int
        get() = if (mCursorPosition.value != null) {
            mCursorPosition.value!!
        } else {
            -1
        }
        set(x) {
            mCursorPosition.value = x
        }

    fun addVisibleStroke(path: Path) {
        if (mVisibleStrokesHistory.value != null) {
            val visibleStrokesHistoryCopy = ArrayList(mVisibleStrokesHistory.value!!)
            visibleStrokesHistoryCopy.add(path)
            mVisibleStrokesHistory.value = visibleStrokesHistoryCopy
        }
    }

    fun removeVisibleStroke(index: Int) {
        if (mVisibleStrokesHistory.value != null) {
            val visibleStrokesHistoryCopy = ArrayList(mVisibleStrokesHistory.value!!)
            visibleStrokesHistoryCopy.removeAt(index)
            mVisibleStrokesHistory.value = visibleStrokesHistoryCopy
        }
    }

    val visibleStrokeSize: Int
        get() {
            return if (mVisibleStrokesHistory.value != null) {
                mVisibleStrokesHistory.value!!.size
            } else {
                -1
            }
        }

    fun getVisibleStroke(index: Int): Path? {
        return if (mVisibleStrokesHistory.value != null) {
            mVisibleStrokesHistory.value!![index]
        } else {
            null
        }
    }

    fun clearVisibleStrokes() {
        mVisibleStrokesHistory.value = ArrayList()
    }

    val currentVisibleStroke: Path?
        get() = mCurrentVisibleStroke.value

    fun setCurrentVisibleStroke(path: Path) {
        mCurrentVisibleStroke.value = path
    }

    fun resetCurrentVisibleStroke() {
        mCurrentVisibleStroke.value = Path()
    }

    fun lineToCurrentVisibleStroke(x: Float, y: Float) {
        val currentVisibleStrokeCopy = Path(mCurrentVisibleStroke.value)
        currentVisibleStrokeCopy.lineTo(x, y)
        mCurrentVisibleStroke.value = currentVisibleStrokeCopy
    }

    fun moveToCurrentVisibleStroke(x: Float, y: Float) {
        val currentVisibleStrokeCopy = Path(mCurrentVisibleStroke.value)
        currentVisibleStrokeCopy.moveTo(x, y)
        mCurrentVisibleStroke.value = currentVisibleStrokeCopy
    }

    fun setInkBuilder(mInkBuilder: Ink.Builder?) {
        this.mInkBuilder.value = mInkBuilder
    }

    fun inkBuilderBuild(): Ink? {
        return if (mInkBuilder.value != null) {
            mInkBuilder.value!!.build()
        } else {
            null
        }
    }

    fun addInkBuilderStroke(stroke: Ink.Stroke?) {
        val builder = Ink.Builder()
        val inkBuilderCopy = inkBuilderBuild()
        for (iStroke in inkBuilderCopy!!.strokes) {
            builder.addStroke(iStroke!!)
        }
        builder.addStroke(stroke!!)
        mInkBuilder.value = builder
    }

    fun removeFromStrokesHistory(index: Int) {
        if (mStrokesHistory.value != null) {
            val strokesHistoryCopy = ArrayList(mStrokesHistory.value!!)
            strokesHistoryCopy.removeAt(index)
            mStrokesHistory.value = strokesHistoryCopy
        }
    }

    val strokesHistorySize: Int
        get() {
            return if (mStrokesHistory.value != null) {
                mStrokesHistory.value!!.size
            } else {
                -1
            }
        }

    fun clearStrokesHistory() {
        mStrokesHistory.value = ArrayList()
    }

    fun addToStrokesHistory(strokeBuilder: Ink.Stroke.Builder) {
        if (mStrokesHistory.value != null) {
            val strokesHistoryCopy = ArrayList(mStrokesHistory.value!!)
            strokesHistoryCopy.add(strokeBuilder)
            mStrokesHistory.value = strokesHistoryCopy
        }
    }

    fun strokeHistoryBuild(index: Int): Ink.Stroke? {
        return if (mStrokesHistory.value != null) {
            mStrokesHistory.value!![index].build()
        } else {
            null
        }
    }

    fun getStrokeBuilder(): Ink.Stroke.Builder? {
        return mStrokeBuilder.value
    }

    fun strokeBuilderBuild(): Ink.Stroke? {
        return if (mStrokeBuilder.value != null) {
            mStrokeBuilder.value!!.build()
        } else {
            null
        }
    }

    fun addStrokeBuilderPoint(point: Ink.Point) {
        val builder = Ink.Stroke.builder()
        val strokeBuilderCopy = strokeBuilderBuild()
        for (iPoint in strokeBuilderCopy!!.points) {
            builder.addPoint(iPoint)
        }
        builder.addPoint(point)
        mStrokeBuilder.value = builder
    }

    fun clearStrokeBuilder() {
        mStrokeBuilder.value = Ink.Stroke.builder()
    }

    fun setRadicalsList(radicalsParentModels: List<Array<String>>) {
        radicalsList.postValue(radicalsParentModels)
    }
}
