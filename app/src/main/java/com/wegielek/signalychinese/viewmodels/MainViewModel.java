package com.wegielek.signalychinese.viewmodels;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Path;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.mlkit.vision.digitalink.Ink;
import com.wegielek.signalychinese.database.Dictionary;
import com.wegielek.signalychinese.database.Radicals;
import com.wegielek.signalychinese.database.SearchHistory;
import com.wegielek.signalychinese.repository.DictionaryRepository;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {

    public MutableLiveData<Ink.Stroke.Builder> mStrokeBuilder = new MutableLiveData<>();
    public MutableLiveData<ArrayList<Ink.Stroke.Builder>> mStrokesHistory = new MutableLiveData<>();
    public MutableLiveData<Ink.Builder> mInkBuilder = new MutableLiveData<>();
    public MutableLiveData<Path> mCurrentVisibleStroke = new MutableLiveData<>();
    public MutableLiveData<ArrayList<Path>> mVisibleStrokesHistory = new MutableLiveData<>();
    public MutableLiveData<Integer> mCursorPosition = new MutableLiveData<>();
    public MutableLiveData<List<String>> mCharactersList = new MutableLiveData<>();
    public MutableLiveData<List<String>> mDictionaryResultsList = new MutableLiveData<>();
    public MutableLiveData<List<String[]>> mRadicalsList = new MutableLiveData<>();
    public MutableLiveData<Boolean> mIsRadicalChosen = new MutableLiveData<>();
    public MutableLiveData<String> mRadicalChosenCharacter = new MutableLiveData<>();
    private final DictionaryRepository mDictionaryRepository;

    public MainViewModel(Application application) {
        super(application);
        mDictionaryRepository = new DictionaryRepository(application);
        mStrokeBuilder.setValue(Ink.Stroke.builder());
        mInkBuilder.setValue(Ink.builder());
        mStrokesHistory.setValue(new ArrayList<>());
        mDictionaryResultsList.setValue(new ArrayList<>());
        mCharactersList.setValue(new ArrayList<>());
        mVisibleStrokesHistory.setValue(new ArrayList<>());
        mCurrentVisibleStroke.setValue(new Path());
        mCursorPosition.setValue(0);
        mRadicalChosenCharacter.setValue("0");
        mRadicalsList.setValue(new ArrayList<>());
        mIsRadicalChosen.setValue(false);
    }

    public LiveData<List<Radicals>> getRadicalsSection(String section) {
        setRadicalChosenCharacter(section);
        return mDictionaryRepository.getRadicalsSection(section);
    }

    public LiveData<List<Dictionary>> searchSingleCH(String searchQuery) {
        return mDictionaryRepository.searchSingleCH(searchQuery);
    }

    public LiveData<List<Dictionary>> searchByWordCH(String searchQuery) {
        return mDictionaryRepository.searchByWordCH(searchQuery);
    }

    public LiveData<List<Dictionary>> searchByWordPL(String searchQuery) {
        return mDictionaryRepository.searchByWordPL(searchQuery);
    }

    public String getRadicalChosenCharacter() {
        return mRadicalChosenCharacter.getValue();
    }

    public void setRadicalChosenCharacter(String radicalChosenCharacter) {
        mRadicalChosenCharacter.setValue(radicalChosenCharacter);
    }

    public String getResult(int index) {
        if (mDictionaryResultsList.getValue() != null) {
            String word = mDictionaryResultsList.getValue().get(index);

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String ts = sdf.format(timestamp);
            Log.i("TIME", ts);
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.time = ts;
            searchHistory.traditionalSign = word.split("/")[0];
            searchHistory.simplifiedSign = word.split("/")[1];
            searchHistory.pronunciation = word.split("/")[2];
            searchHistory.pronunciationPhonetic = word.split("/")[3];
            int x = word.split("/")[0].length() + word.split("/")[1].length()
                    + word.split("/")[2].length() + word.split("/")[3].length() + 4;
            searchHistory.translation = word.substring(x);
            mDictionaryRepository.addHistoryRecord(searchHistory).addListener(() ->
                    Log.d("MainViewModel", "History record added successfully"), Executors.newSingleThreadExecutor()
            );

            return mDictionaryResultsList.getValue().get(index);
        } else {
            return null;
        }
    }

    public void setRadicalChosen(Boolean isRadicalChosen) {
        mIsRadicalChosen.setValue(isRadicalChosen);
    }

    public Boolean getRadicalChosen() {
        return mIsRadicalChosen.getValue();
    }

    public void updateResults(List<String> searchResults) {
        mDictionaryResultsList.setValue(searchResults);
    }

    public void addToCharacterList(String text) {
        if (mCharactersList.getValue() != null) {
            List<String> characterListCopy = new ArrayList<>(mCharactersList.getValue());
            characterListCopy.add(text);
            mCharactersList.setValue(characterListCopy);
        }
    }

    public void clearCharacterList() {
        mCharactersList.setValue(Collections.emptyList());
    }

    public void setCursorPosition(int x) {
        mCursorPosition.setValue(x);
    }

    public int getCursorPosition() {
        if (mCursorPosition.getValue() != null) {
            return mCursorPosition.getValue();
        } else {
            return -1;
        }
    }

    public void addVisibleStroke(Path path) {
        if (mVisibleStrokesHistory.getValue() != null) {
            ArrayList<Path> visibleStrokesHistoryCopy = new ArrayList<>(mVisibleStrokesHistory.getValue());
            visibleStrokesHistoryCopy.add(path);
            mVisibleStrokesHistory.setValue(visibleStrokesHistoryCopy);
        }
    }

    public void removeVisibleStroke(int index) {
        if (mVisibleStrokesHistory.getValue() != null) {
            ArrayList<Path> visibleStrokesHistoryCopy = new ArrayList<>(mVisibleStrokesHistory.getValue());
            visibleStrokesHistoryCopy.remove(index);
            mVisibleStrokesHistory.setValue(visibleStrokesHistoryCopy);
        }
    }

    public int getVisibleStrokeSize() {
        if (mVisibleStrokesHistory.getValue() != null) {
            return mVisibleStrokesHistory.getValue().size();
        } else {
            return -1;
        }
    }

    public Path getVisibleStroke(int index) {
        if (mVisibleStrokesHistory.getValue() != null) {
            return mVisibleStrokesHistory.getValue().get(index);
        } else {
            return null;
        }
    }

    public void clearVisibleStrokes() {
        mVisibleStrokesHistory.setValue(new ArrayList<>());
    }

    public Path getCurrentVisibleStroke() {
        return mCurrentVisibleStroke.getValue();
    }

    public void setCurrentVisibleStroke(Path path) {
        mCurrentVisibleStroke.setValue(path);
    }

    public void resetCurrentVisibleStroke() {
        mCurrentVisibleStroke.setValue(new Path());
    }

    public void lineToCurrentVisibleStroke(float x, float y) {
        Path currentVisibleStrokeCopy = new Path(mCurrentVisibleStroke.getValue());
        currentVisibleStrokeCopy.lineTo(x, y);
        mCurrentVisibleStroke.setValue(currentVisibleStrokeCopy);
    }

    public void moveToCurrentVisibleStroke(float x, float y) {
        Path currentVisibleStrokeCopy = new Path(mCurrentVisibleStroke.getValue());
        currentVisibleStrokeCopy.moveTo(x, y);
        mCurrentVisibleStroke.setValue(currentVisibleStrokeCopy);
    }

    public void setInkBuilder(Ink.Builder mInkBuilder) {
        this.mInkBuilder.setValue(mInkBuilder);
    }

    public Ink inkBuilderBuild() {
        if (mInkBuilder.getValue() != null) {
            return mInkBuilder.getValue().build();
        } else {
            return null;
        }
    }

    public void addInkBuilderStroke(Ink.Stroke stroke) {
        Ink.Builder builder = new Ink.Builder();
        Ink inkBuilderCopy = inkBuilderBuild();
        for (Ink.Stroke iStroke : inkBuilderCopy.getStrokes()) {
            builder.addStroke(iStroke);
        }
        builder.addStroke(stroke);
        mInkBuilder.setValue(builder);
    }

    public void removeFromStrokesHistory(int index) {
        if (mStrokesHistory.getValue() != null) {
            ArrayList<Ink.Stroke.Builder> strokesHistoryCopy = new ArrayList<>(mStrokesHistory.getValue());
            strokesHistoryCopy.remove(index);
            mStrokesHistory.setValue(strokesHistoryCopy);
        }
    }

    public int getStrokesHistorySize() {
        if (mStrokesHistory.getValue() != null) {
            return mStrokesHistory.getValue().size();
        } else {
            return -1;
        }
    }

    public void clearStrokesHistory() {
        mStrokesHistory.setValue(new ArrayList<>());
    }

    public void addToStrokesHistory(Ink.Stroke.Builder strokeBuilder) {
        if (mStrokesHistory.getValue() != null) {
            ArrayList<Ink.Stroke.Builder> strokesHistoryCopy = new ArrayList<>(mStrokesHistory.getValue());
            strokesHistoryCopy.add(strokeBuilder);
            mStrokesHistory.setValue(strokesHistoryCopy);
        }
    }

    public Ink.Stroke strokeHistoryBuild(int index) {
        if (mStrokesHistory.getValue() != null) {
            return mStrokesHistory.getValue().get(index).build();
        } else {
            return null;
        }
    }

    public Ink.Stroke.Builder getmStrokeBuilder() {
        return mStrokeBuilder.getValue();
    }

    public Ink.Stroke strokeBuilderBuild() {
        if (mStrokeBuilder.getValue() != null) {
            return mStrokeBuilder.getValue().build();
        } else {
            return null;
        }
    }

    public void addStrokeBuilderPoint(Ink.Point point) {
        Ink.Stroke.Builder builder = Ink.Stroke.builder();
        Ink.Stroke strokeBuilderCopy = strokeBuilderBuild();
        for (Ink.Point iPoint : strokeBuilderCopy.getPoints()) {
            builder.addPoint(iPoint);
        }
        builder.addPoint(point);
        mStrokeBuilder.setValue(builder);
    }

    public void clearStrokeBuilder() {
        mStrokeBuilder.setValue(Ink.Stroke.builder());
    }

    public void setRadicalsList(List<String[]> radicalsParentModels) {
        mRadicalsList.postValue(radicalsParentModels);
    }

}
