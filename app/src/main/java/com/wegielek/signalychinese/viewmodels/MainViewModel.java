package com.wegielek.signalychinese.viewmodels;

import android.app.Application;
import android.graphics.Path;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.mlkit.vision.digitalink.Ink;
import com.wegielek.signalychinese.database.Dictionary;
import com.wegielek.signalychinese.database.Radicals;
import com.wegielek.signalychinese.repository.DictionaryRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private DictionaryRepository mDictionaryRepository;

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
        mRadicalsList.setValue(new ArrayList<>());
    }

    public LiveData<List<Radicals>> getSection(String section) {
        return mDictionaryRepository.getSection(section);
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

    public LiveData<List<Dictionary>> getAllWords() {
        return mDictionaryRepository.getAllWords();
    }

    public String getResult(int index) {
        return mDictionaryResultsList.getValue().get(index);
    }

    public void updateResults(List<String> searchResults) {
        mDictionaryResultsList.setValue(searchResults);
    }

    public void addToCharacterList(String text) {
        List<String> characterListCopy = new ArrayList<>(mCharactersList.getValue());
        characterListCopy.add(text);
        mCharactersList.setValue(characterListCopy);
    }

    public void clearCharacterList() {
        mCharactersList.setValue(Collections.emptyList());
    }

    public void setCursorPosition(int x) {
        mCursorPosition.setValue(x);
    }

    public int getCursorPosition() {
        return mCursorPosition.getValue();
    }

    public void addVisibleStroke(Path path) {
        ArrayList<Path> visibleStrokesHistoryCopy = new ArrayList<>(mVisibleStrokesHistory.getValue());
        visibleStrokesHistoryCopy.add(path);
        mVisibleStrokesHistory.setValue(visibleStrokesHistoryCopy);
    }

    public void removeVisibleStroke(int index) {
        ArrayList<Path> visibleStrokesHistoryCopy = new ArrayList<>(mVisibleStrokesHistory.getValue());
        visibleStrokesHistoryCopy.remove(index);
        mVisibleStrokesHistory.setValue(visibleStrokesHistoryCopy);
    }

    public int getVisibleStrokeSize() {
        return mVisibleStrokesHistory.getValue().size();
    }

    public Path getVisibleStroke(int index) {
        return mVisibleStrokesHistory.getValue().get(index);
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
        return mInkBuilder.getValue().build();
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
        ArrayList<Ink.Stroke.Builder> strokesHistoryCopy = new ArrayList<>(mStrokesHistory.getValue());
        strokesHistoryCopy.remove(index);
        mStrokesHistory.setValue(strokesHistoryCopy);
    }

    public int getStrokesHistorySize() {
        return mStrokesHistory.getValue().size();
    }

    public void clearStrokesHistory() {
        mStrokesHistory.setValue(new ArrayList<>());
    }

    public void addToStrokesHistory(Ink.Stroke.Builder strokeBuilder) {
        ArrayList<Ink.Stroke.Builder> strokesHistoryCopy = new ArrayList<>(mStrokesHistory.getValue());
        strokesHistoryCopy.add(strokeBuilder);
        mStrokesHistory.setValue(strokesHistoryCopy);
    }

    public Ink.Stroke strokeHistoryBuild(int index) {
        return mStrokesHistory.getValue().get(index).build();
    }

    public Ink.Stroke.Builder getmStrokeBuilder() {
        return mStrokeBuilder.getValue();
    }

    public Ink.Stroke strokeBuilderBuild() {
        return mStrokeBuilder.getValue().build();
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

    public List<String[]> getRadicalsList() {
        return mRadicalsList.getValue();
    }

    public void setRadicalsList(List<String[]> radicalsParentModels) {
        mRadicalsList.postValue(radicalsParentModels);
    }


}
