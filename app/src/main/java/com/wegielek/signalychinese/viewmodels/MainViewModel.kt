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
import com.wegielek.signalychinese.database.History
import com.wegielek.signalychinese.database.Radicals
import com.wegielek.signalychinese.repository.DictionaryRepository
import java.sql.Timestamp
import java.text.SimpleDateFormat

class MainViewModel(application: Application) : AndroidViewModel(application) {
    var charactersList = MutableLiveData<List<String>>()
    var dictionaryResultsList = MutableLiveData<List<Dictionary>>()
    var radicalsList = MutableLiveData<List<Array<String>>>()
    private val strokeBuilder = MutableLiveData<Ink.Stroke.Builder>()
    private val strokesHistory = MutableLiveData<ArrayList<Ink.Stroke.Builder>>()
    private val inkBuilder = MutableLiveData<Ink.Builder>()
    private val mCurrentVisibleStroke = MutableLiveData<Path>()
    private val visibleStrokesHistory = MutableLiveData<ArrayList<Path>>()
    private val mCursorPosition = MutableLiveData<Int>()
    private val isRadicalChosen = MutableLiveData<Boolean>()
    private val mRadicalChosenCharacter = MutableLiveData<String>()
    private val keepSplashScreen = MutableLiveData<Boolean>()
    private val dictionaryRepository: DictionaryRepository

    init {
        dictionaryRepository = DictionaryRepository(application)
        strokeBuilder.value = Ink.Stroke.builder()
        inkBuilder.value = Ink.builder()
        strokesHistory.value = ArrayList()
        dictionaryResultsList.value = ArrayList()
        charactersList.value = ArrayList()
        visibleStrokesHistory.value = ArrayList()
        mCurrentVisibleStroke.value = Path()
        mCursorPosition.value = 0
        mRadicalChosenCharacter.value = "0"
        radicalsList.value = ArrayList()
        isRadicalChosen.value = false
        keepSplashScreen.value = true
    }

    fun setKeepSplashScreen(keepSplashScreen: Boolean) {
        this.keepSplashScreen.value = keepSplashScreen
    }

    fun isKeepSplashScreen(): Boolean {
        return keepSplashScreen.value!!
    }

    fun getRadicalsSection(section: String): ListenableFuture<List<Radicals>> {
        setRadicalChosenCharacter(section)
        return dictionaryRepository.getRadicalsSection(section)
    }

    fun searchSingleCH(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return dictionaryRepository.searchSingleCH(searchQuery)
    }

    fun searchByWordCH(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return dictionaryRepository.searchByWordCH(searchQuery)
    }

    fun searchByWordCHAll(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return dictionaryRepository.searchByWordCHAll(searchQuery)
    }

    fun searchByWordPL(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return dictionaryRepository.searchByWordPL(searchQuery)
    }

    fun searchByWordPLAll(searchQuery: String): ListenableFuture<List<Dictionary>> {
        return dictionaryRepository.searchByWordPLAll(searchQuery)
    }

    val radicalChosenCharacter: String?
        get() = mRadicalChosenCharacter.value

    fun setRadicalChosenCharacter(radicalChosenCharacter: String) {
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
        dictionaryRepository.addHistoryRecord(history).addListener(
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
            val characterListCopy: MutableList<String> = ArrayList(charactersList.value)
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
        if (visibleStrokesHistory.value != null) {
            val visibleStrokesHistoryCopy = ArrayList(visibleStrokesHistory.value)
            visibleStrokesHistoryCopy.add(path)
            visibleStrokesHistory.value = visibleStrokesHistoryCopy
        }
    }

    fun removeVisibleStroke(index: Int) {
        if (visibleStrokesHistory.value != null) {
            val visibleStrokesHistoryCopy = ArrayList(visibleStrokesHistory.value)
            visibleStrokesHistoryCopy.removeAt(index)
            visibleStrokesHistory.value = visibleStrokesHistoryCopy
        }
    }

    val visibleStrokeSize: Int
        get() {
            return if (visibleStrokesHistory.value != null) {
                visibleStrokesHistory.value!!.size
            } else {
                -1
            }
        }

    fun getVisibleStroke(index: Int): Path? {
        return if (visibleStrokesHistory.value != null) {
            visibleStrokesHistory.value!!.get(index)
        } else {
            null
        }
    }

    fun clearVisibleStrokes() {
        visibleStrokesHistory.value = ArrayList()
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
        this.inkBuilder.value = mInkBuilder
    }

    fun inkBuilderBuild(): Ink? {
        return if (inkBuilder.value != null) {
            inkBuilder.value!!.build()
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
        inkBuilder.value = builder
    }

    fun removeFromStrokesHistory(index: Int) {
        if (strokesHistory.value != null) {
            val strokesHistoryCopy = ArrayList(strokesHistory.value)
            strokesHistoryCopy.removeAt(index)
            strokesHistory.value = strokesHistoryCopy
        }
    }

    val strokesHistorySize: Int
        get() {
            return if (strokesHistory.value != null) {
                strokesHistory.value!!.size
            } else {
                -1
            }
        }

    fun clearStrokesHistory() {
        strokesHistory.value = ArrayList()
    }

    fun addToStrokesHistory(strokeBuilder: Ink.Stroke.Builder) {
        if (strokesHistory.value != null) {
            val strokesHistoryCopy = ArrayList(strokesHistory.value)
            strokesHistoryCopy.add(strokeBuilder)
            strokesHistory.value = strokesHistoryCopy
        }
    }

    fun strokeHistoryBuild(index: Int): Ink.Stroke? {
        return if (strokesHistory.value != null) {
            strokesHistory.value!!.get(index).build()
        } else {
            null
        }
    }

    fun getmStrokeBuilder(): Ink.Stroke.Builder? {
        return strokeBuilder.value
    }

    fun strokeBuilderBuild(): Ink.Stroke? {
        return if (strokeBuilder.value != null) {
            strokeBuilder.value!!.build()
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
        strokeBuilder.value = builder
    }

    fun clearStrokeBuilder() {
        strokeBuilder.value = Ink.Stroke.builder()
    }

    fun setRadicalsList(radicalsParentModels: List<Array<String>>) {
        radicalsList.postValue(radicalsParentModels)
    }
}
