package com.wegielek.signalychinese.models;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HanziCharacter {

    private int index = 0;
    private final List<Path> characterPaths;
    private final int size;
    private final PathMeasure pathMeasure = new PathMeasure();
    private final float[] pos = new float[2];
    private final float[] tan = new float[2];
    private final boolean[] matchStart;
    private final boolean[] matchControlPoint;
    private final boolean[] matchEnd;
    private boolean finished = false;

    public HanziCharacter(List<Path> characterPaths) {
        this.characterPaths = new ArrayList<>(characterPaths);
        this.size = characterPaths.size();
        matchStart = new boolean[this.size];
        matchControlPoint = new boolean[this.size];
        matchEnd = new boolean[this.size];
        Arrays.fill(matchStart, false);
        Arrays.fill(matchControlPoint, false);
        Arrays.fill(matchEnd, false);

        initDefaultResize();
    }


    public boolean nextIndex() {
        if(index < size - 1) {
            index++;
            return true;
        }
        finished = true;
        return false;
    }

    public boolean prevIndex() {
        if(index > 0) {
            index--;
            return true;
        }
        return false;
    }

    public int getIndex() {
        return index;
    }


    public List<Path> getPaths() {
        return characterPaths;
    }

    public Path getCurrPath() {
        return characterPaths.get(index);
    }

    public boolean matchStart(float x, float y, float r) {
        pathMeasure.setPath(this.characterPaths.get(index), false);
        pathMeasure.getPosTan(0.0f, pos, tan);
        if (Math.pow(x - pos[0], 2) + Math.pow(y - pos[1], 2) <= Math.pow(r, 2)) {
            matchStart[index] = true;
            return true;
        }
        return false;
    }

    public boolean matchControlPoint(float x, float y, float r) {
        pathMeasure.setPath(this.characterPaths.get(index), false);
        pathMeasure.getPosTan(pathMeasure.getLength()/2, pos, tan);
        if (Math.pow(x - pos[0], 2) + Math.pow(y - pos[1], 2) <= Math.pow(r, 2)) {
            matchControlPoint[index] = true;
            return true;
        }
        return false;
    }

    public boolean matchEnd(float x, float y, float r) {
        pathMeasure.setPath(this.characterPaths.get(index), false);
        pathMeasure.getPosTan(pathMeasure.getLength(), pos, tan);
        if (Math.pow(x - pos[0], 2) + Math.pow(y - pos[1], 2) <= Math.pow(r, 2)) {
            matchEnd[index] = true;
            return true;
        }
        return false;
    }

    public void matchReset() {
        matchStart[index] = false;
        matchControlPoint[index] = false;
        matchEnd[index] = false;
    }

    public boolean isMatchedStart() {
        return matchStart[index];
    }

    public boolean isMatchedControlPoint() {
        return matchControlPoint[index];
    }

    public boolean isMatched() {
        return matchStart[index] && matchEnd[index];
    }

    public boolean isFinished() {
        return finished;
    }

    public void reset() {
        Arrays.fill(matchStart, false);
        Arrays.fill(matchControlPoint, false);
        Arrays.fill(matchEnd, false);
        index = 0;
        finished = false;
    }

    private void initDefaultResize() {
        Matrix transformMatrix = new Matrix();
        transformMatrix.setScale(3.2f, 3.2f);

        for (int i = 0; i < characterPaths.size(); i++) {
            characterPaths.get(i).transform(transformMatrix);
        }
        transformMatrix.setTranslate(28,25);
        for (int i = 0; i < characterPaths.size(); i++) {
            characterPaths.get(i).transform(transformMatrix);
        }
    }
}

