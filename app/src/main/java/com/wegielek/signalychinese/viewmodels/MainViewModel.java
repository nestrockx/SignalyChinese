package com.wegielek.signalychinese.viewmodels;

import android.app.Application;
import android.graphics.Path;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.mlkit.vision.digitalink.Ink;
import com.wegielek.signalychinese.database.Dictionary;
import com.wegielek.signalychinese.models.RadicalsParentModel;
import com.wegielek.signalychinese.repository.DictionaryRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    public MutableLiveData<Ink.Stroke.Builder> strokeBuilder = new MutableLiveData<>();
    public MutableLiveData<ArrayList<Ink.Stroke.Builder>> strokesHistory = new MutableLiveData<>();
    public MutableLiveData<Ink.Builder> inkBuilder = new MutableLiveData<>();
    public MutableLiveData<Path> currentVisibleStroke = new MutableLiveData<>();
    public MutableLiveData<ArrayList<Path>> visibleStrokesHistory = new MutableLiveData<>();
    public MutableLiveData<Integer> cursorPosition = new MutableLiveData<>();
    public MutableLiveData<List<String>> charactersList = new MutableLiveData<>();
    public MutableLiveData<List<String>> dictionaryResultsList = new MutableLiveData<>();
    public MutableLiveData<List<RadicalsParentModel>> radicalsList = new MutableLiveData<>();
    private DictionaryRepository dictionaryRepository;

    public MainViewModel(Application application) {
        super(application);
        dictionaryRepository = new DictionaryRepository(application);
        strokeBuilder.setValue(Ink.Stroke.builder());
        inkBuilder.setValue(Ink.builder());
        strokesHistory.setValue(new ArrayList<>());
        dictionaryResultsList.setValue(new ArrayList<>());
        charactersList.setValue(new ArrayList<>());
        visibleStrokesHistory.setValue(new ArrayList<>());
        currentVisibleStroke.setValue(new Path());
        cursorPosition.setValue(0);
        radicalsList.setValue(new ArrayList<>());
    }

    public LiveData<List<Dictionary>> searchByWord(String searchQuery) {
        return dictionaryRepository.searchByWord(searchQuery);
    }

    public LiveData<List<Dictionary>> getAllWords() {
        return dictionaryRepository.getAllWords();
    }

    public String getResult(int index) {
        return dictionaryResultsList.getValue().get(index);
    }

    public void updateResults(List<String> searchResults) {
        dictionaryResultsList.setValue(searchResults);
    }

    public void addToCharacterList(String text) {
        List<String> characterListCopy = new ArrayList<>(charactersList.getValue());
        characterListCopy.add(text);
        charactersList.setValue(characterListCopy);
    }

    public void clearCharacterList() {
        charactersList.setValue(Collections.emptyList());
    }

    public void setCursorPosition(int x) {
        cursorPosition.setValue(x);
    }

    public int getCursorPosition() {
        return cursorPosition.getValue();
    }

    public void addVisibleStroke(Path path) {
        ArrayList<Path> visibleStrokesHistoryCopy = new ArrayList<>(visibleStrokesHistory.getValue());
        visibleStrokesHistoryCopy.add(path);
        visibleStrokesHistory.setValue(visibleStrokesHistoryCopy);
    }

    public void removeVisibleStroke(int index) {
        ArrayList<Path> visibleStrokesHistoryCopy = new ArrayList<>(visibleStrokesHistory.getValue());
        visibleStrokesHistoryCopy.remove(index);
        visibleStrokesHistory.setValue(visibleStrokesHistoryCopy);
    }

    public int getVisibleStrokeSize() {
        return visibleStrokesHistory.getValue().size();
    }

    public Path getVisibleStroke(int index) {
        return visibleStrokesHistory.getValue().get(index);
    }

    public void clearVisibleStrokes() {
        visibleStrokesHistory.setValue(new ArrayList<>());
    }

    public Path getCurrentVisibleStroke() {
        return currentVisibleStroke.getValue();
    }

    public void setCurrentVisibleStroke(Path path) {
        currentVisibleStroke.setValue(path);
    }

    public void resetCurrentVisibleStroke() {
        currentVisibleStroke.setValue(new Path());
    }

    public void lineToCurrentVisibleStroke(float x, float y) {
        Path currentVisibleStrokeCopy = new Path(currentVisibleStroke.getValue());
        currentVisibleStrokeCopy.lineTo(x, y);
        currentVisibleStroke.setValue(currentVisibleStrokeCopy);
    }

    public void moveToCurrentVisibleStroke(float x, float y) {
        Path currentVisibleStrokeCopy = new Path(currentVisibleStroke.getValue());
        currentVisibleStrokeCopy.moveTo(x, y);
        currentVisibleStroke.setValue(currentVisibleStrokeCopy);
    }

    public void setInkBuilder(Ink.Builder inkBuilder) {
        this.inkBuilder.setValue(inkBuilder);
    }

    public Ink inkBuilderBuild() {
        return inkBuilder.getValue().build();
    }

    public void addInkBuilderStroke(Ink.Stroke stroke) {
        Ink.Builder builder = new Ink.Builder();
        Ink inkBuilderCopy = inkBuilderBuild();
        for (Ink.Stroke iStroke : inkBuilderCopy.getStrokes()) {
            builder.addStroke(iStroke);
        }
        builder.addStroke(stroke);
        inkBuilder.setValue(builder);
    }

    public void removeFromStrokesHistory(int index) {
        ArrayList<Ink.Stroke.Builder> strokesHistoryCopy = new ArrayList<>(strokesHistory.getValue());
        strokesHistoryCopy.remove(index);
        strokesHistory.setValue(strokesHistoryCopy);
    }

    public int getStrokesHistorySize() {
        return strokesHistory.getValue().size();
    }

    public void clearStrokesHistory() {
        strokesHistory.setValue(new ArrayList<>());
    }

    public void addToStrokesHistory(Ink.Stroke.Builder strokeBuilder) {
        ArrayList<Ink.Stroke.Builder> strokesHistoryCopy = new ArrayList<>(strokesHistory.getValue());
        strokesHistoryCopy.add(strokeBuilder);
        strokesHistory.setValue(strokesHistoryCopy);
    }

    public Ink.Stroke strokeHistoryBuild(int index) {
        return strokesHistory.getValue().get(index).build();
    }

    public Ink.Stroke.Builder getStrokeBuilder() {
        return strokeBuilder.getValue();
    }

    public Ink.Stroke strokeBuilderBuild() {
        return strokeBuilder.getValue().build();
    }

    public void addStrokeBuilderPoint(Ink.Point point) {
        Ink.Stroke.Builder builder = Ink.Stroke.builder();
        Ink.Stroke strokeBuilderCopy = strokeBuilderBuild();
        for (Ink.Point iPoint : strokeBuilderCopy.getPoints()) {
            builder.addPoint(iPoint);
        }
        builder.addPoint(point);
        strokeBuilder.setValue(builder);
    }

    public void clearStrokeBuilder() {
        strokeBuilder.setValue(Ink.Stroke.builder());
    }

    public List<RadicalsParentModel> getRadicalsList() {
        return radicalsList.getValue();
    }

    public void setRadicalsList(List<RadicalsParentModel> radicalsParentModels) {
        radicalsList.postValue(radicalsParentModels);
    }


}
