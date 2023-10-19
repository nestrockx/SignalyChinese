package com.wegielek.signalychinese.views;

import static androidx.core.content.ContextCompat.getColor;

import static com.wegielek.signalychinese.utils.Utils.getPaint;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sdsmdg.harjot.vectormaster.VectorMasterDrawable;
import com.sdsmdg.harjot.vectormaster.models.GroupModel;
import com.sdsmdg.harjot.vectormaster.models.PathModel;

import com.wegielek.signalychinese.enums.CharacterMode;
import com.wegielek.signalychinese.models.FingerPath;
import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.models.HanziCharacter;

import java.util.ArrayList;
import java.util.List;

public class LearnStrokesView extends View {

    private static final String LOG_TAG = "LearnStrokesView";

    private HanziCharacter hanziCharacter;
    private CharacterMode characterMode = CharacterMode.PRESENTATION;

    private ValueAnimator valueStrokeAnimator;
    private ValueAnimator valueSuccessAnimator;
    private ValueAnimator valueMistakeAnimator;

    private final float[] markerProgress = new float[51];
    private final float[] pos = new float[2];
    private final float[] tan = new float[2];
    private final PathMeasure pathMeasure = new PathMeasure();

    private final PointF pointF = new PointF();
    private boolean setup = true;
    private static final int BITMAP_DIMENSION = 400;

    private final Matrix transformMat = new Matrix();
    private final Matrix inverseTransformMat = new Matrix();

    private static final int DEFAULT_DIMENSIONS = 1000;
    private int width;
    private int height;

    private static final int BRUSH_SIZE = 10;
    private final int DEFAULT_COLOR = getColor(this.getContext(), R.color.white);
    private final int BACKGROUND_COLOR = getColor(this.getContext(), R.color.canvas_background);
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;

    private Paint mainPaint;
    private Paint fingerPathPaint;
    private Paint sketchPaint;
    private Paint gridPaint;
    private Paint gridThinPaint;

    private final ArrayList<FingerPath> paths = new ArrayList<>();
    private int strokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private final Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    private char hanziChar;

    public LearnStrokesView(Context context) {
        super(context);
    }

    public LearnStrokesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public void setMode(CharacterMode characterMode) {
        this.characterMode = characterMode;
        setHanziCharacter(hanziChar);
        if (characterMode == CharacterMode.PRESENTATION) {
            valueStrokeAnimator.setDuration(1000);
        } else {
            valueStrokeAnimator.setDuration(1500);
        }
    }

    public void setHanziCharacter(char hanziChar) {
        this.hanziChar = hanziChar;
        clear();
        valueStrokeAnimator.cancel();
        this.hanziCharacter = initHanziStrokes(hanziChar);
        if (this.hanziCharacter != null) {
            if (characterMode == CharacterMode.PRESENTATION) {
                valueStrokeAnimator.start();
            }
        }
    }

    public void setDimensions(int dimensions) {
        width = dimensions;
        height = dimensions;
        setMeasuredDimension(width, height);
    }

    private void clear() {
        valueStrokeAnimator.cancel();
        if(hanziCharacter != null) {
            hanziCharacter.reset();
        }
        invalidate();
    }

    private void initAnimators() {
        valueMistakeAnimator = ValueAnimator.ofArgb(getColor(getContext(), R.color.mistake),
                getColor(getContext(), R.color.mistake_fade));
        int vMistakeDuration = 500;
        valueMistakeAnimator.setDuration(vMistakeDuration);
        valueMistakeAnimator.addUpdateListener(animation -> {
            int color = (Integer) animation.getAnimatedValue();
            fingerPathPaint.setColor(color);
            invalidate();
            if (color == Color.parseColor("#00B60000")) {
                fingerPathPaint.setColor(DEFAULT_COLOR);
                fingerPathPaint.setAlpha(0xff);
                paths.clear();
            }
        });

        valueSuccessAnimator = ValueAnimator.ofArgb(getColor(getContext(), R.color.white),
                getColor(getContext(), R.color.success), DEFAULT_COLOR);
        int vSuccessDuration = 1000;
        valueSuccessAnimator.setDuration(vSuccessDuration);
        valueSuccessAnimator.addUpdateListener(animation -> {
            mainPaint.setColor((Integer) animation.getAnimatedValue());
            invalidate();
        });

        valueStrokeAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        if (characterMode == CharacterMode.PRESENTATION) {
            valueStrokeAnimator.setDuration(1000);
        } else {
            valueStrokeAnimator.setDuration(1500);
        }
        valueStrokeAnimator.addUpdateListener(animation -> {
            markerProgress[hanziCharacter.getIndex()] = (float)animation.getAnimatedValue();
            invalidate();
        });
    }

    private void initialize() {
        initAnimators();
        initPaints();
        hanziChar = '一';
        setHanziCharacter('一');
        this.width = DEFAULT_DIMENSIONS;
        this.height = DEFAULT_DIMENSIONS;
        mBitmap = Bitmap.createBitmap(BITMAP_DIMENSION, BITMAP_DIMENSION, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        strokeWidth = BRUSH_SIZE;
    }

    @SuppressLint("DiscouragedApi")
    private HanziCharacter initHanziStrokes(char hanziChar) {
        String hanziCharacterUnicode;
        hanziCharacterUnicode = Integer.toHexString(hanziChar | 0x10000).substring(1);

        List<Path> hanziCharacterStrokeList = new ArrayList<>();

        Log.d("UNICODE", "_" + hanziCharacterUnicode + " " + "drawable" + " " + getContext().getPackageName());

        VectorMasterDrawable vectorMasterDrawable;
        try {
            vectorMasterDrawable = new VectorMasterDrawable(
                    getContext(),
                    getResources().getIdentifier(
                            "_" + hanziCharacterUnicode,
                            "drawable",
                            getContext().getPackageName()
                    )
            );
        } catch (Resources.NotFoundException e) {
            Log.e(LOG_TAG, "Error message: " + e.getMessage());
            return null;
        }

        GroupModel outline = vectorMasterDrawable.getGroupModelByName("Hanzi");
        for (PathModel model : outline.getPathModels()) {
            hanziCharacterStrokeList.add(model.getPath());
        }

        return new HanziCharacter(hanziCharacterStrokeList);
    }

    private void drawGrid() {
        mCanvas.drawLine(0.0f, 0.0f, BITMAP_DIMENSION, BITMAP_DIMENSION, gridThinPaint);
        mCanvas.drawLine(0.0f, BITMAP_DIMENSION, BITMAP_DIMENSION, 0.0f, gridThinPaint);
        mCanvas.drawLine((float)BITMAP_DIMENSION / 2, 10.0f, (float)BITMAP_DIMENSION / 2, BITMAP_DIMENSION - 10.0f, gridPaint);
        mCanvas.drawLine(10.0f, (float)BITMAP_DIMENSION / 2, BITMAP_DIMENSION - 10.0f, (float)BITMAP_DIMENSION / 2, gridPaint);
    }

    private void drawHanziCharacterPath() {
        if (hanziCharacter != null) {
            if (characterMode.equals(CharacterMode.LEARN)) {
                mCanvas.drawPath(hanziCharacter.getCurrPath(), sketchPaint);

                pathMeasure.setPath(hanziCharacter.getCurrPath(), false);
                pathMeasure.getPosTan(markerProgress[hanziCharacter.getIndex()] * pathMeasure.getLength(), pos, tan);
                mCanvas.drawPoint(pos[0], pos[1], mainPaint);

                if (!valueStrokeAnimator.isRunning()) {
                    valueStrokeAnimator.start();
                }
            }

            if (characterMode.equals(CharacterMode.PRESENTATION)) {
                mCanvas.drawPath(hanziCharacter.getCurrPath(), sketchPaint);

                pathMeasure.setPath(hanziCharacter.getCurrPath(), false);
                pathMeasure.getPosTan(markerProgress[hanziCharacter.getIndex()]
                        * pathMeasure.getLength(), pos, tan);
                mCanvas.drawPoint(pos[0], pos[1], mainPaint);

                if (!valueStrokeAnimator.isRunning()) {
                    if (hanziCharacter.nextIndex()) {
                        valueStrokeAnimator.start();
                    }
                }
            }

            if (hanziCharacter.isMatched()) {
                if (!hanziCharacter.nextIndex()) {
                    valueSuccessAnimator.start();
                }
                valueStrokeAnimator.cancel();
                invalidate();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(BACKGROUND_COLOR);

        if (setup) {
            setupScaleMatrices();
        }

        drawGrid();
        if (hanziCharacter != null) {
            if (!hanziCharacter.isFinished()) {
                drawHanziCharacterPath();
                for (FingerPath fp : paths) {
                    mCanvas.drawPath(fp.path, fingerPathPaint);
                }
            }

            if (hanziCharacter.isFinished()) {
                for (int i = 0; i < hanziCharacter.getIndex() + 1; i++) {
                    mCanvas.drawPath(hanziCharacter.getPaths().get(i), mainPaint);
                }
            } else {
                for (int i = 0; i < hanziCharacter.getIndex(); i++) {
                    mCanvas.drawPath(hanziCharacter.getPaths().get(i), mainPaint);
                }
            }
        }

        canvas.drawBitmap(mBitmap, transformMat, mBitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y) {
        if (characterMode != CharacterMode.PRESENTATION) {
            if (valueMistakeAnimator.isRunning()) {
                valueMistakeAnimator.cancel();
                fingerPathPaint.setColor(DEFAULT_COLOR);
                fingerPathPaint.setAlpha(0xff);
                paths.clear();
            }

            mPath = new Path();
            FingerPath fp = new FingerPath(DEFAULT_COLOR, strokeWidth, mPath);
            paths.add(fp);

            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
            if (hanziCharacter != null) {
                if (hanziCharacter.matchStart(mX, mY, 60.0f)) {
                    Log.i(LOG_TAG, "MATCHED_START");
                }
            }
        }
    }

    private void touchMove(float x, float y) {
        if (characterMode != CharacterMode.PRESENTATION) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
            if (hanziCharacter != null) {
                if (hanziCharacter.matchControlPoint(mX, mY, 50.0f)) {
                    Log.i(LOG_TAG, "MATCHED_CONTROL_POINT");
                }
            }
        }
    }

    private void touchUp() {
        if (characterMode != CharacterMode.PRESENTATION) {
            if (hanziCharacter != null) {
                if (hanziCharacter.isMatchedStart() && hanziCharacter.isMatchedControlPoint()) {
                    if (hanziCharacter.matchEnd(mX, mY, 60.0f)) {
                        Log.i(LOG_TAG, "MATCHED_END");
                    } else {
                        hanziCharacter.matchReset();
                    }
                }
                mPath.lineTo(mX, mY);
                if (hanziCharacter.isMatched()) {
                    paths.clear();
                } else {
                    valueMistakeAnimator.start();
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                getBitmapCoordinates(x, y, pointF);
                Log.i(LOG_TAG, "Starting point " + pointF.x + " : " + pointF.y);
                touchStart(pointF.x, pointF.y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE :
                getBitmapCoordinates(x, y, pointF);
                touchMove(pointF.x, pointF.y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP :
                touchUp();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void setupScaleMatrices() {
        // View size.
        float width = this.width;
        float height = this.height;
        float scaleW = width / BITMAP_DIMENSION;
        float scaleH = height / BITMAP_DIMENSION;

        float scale = scaleW;
        if (scale > scaleH) {
            scale = scaleH;
        }

        // Translation to center bitmap in view after it is scaled up.
        float centerX = BITMAP_DIMENSION * scale / 2;
        float centerY = BITMAP_DIMENSION * scale / 2;
        float dx = width / 2 - centerX;
        float dy = height / 2 - centerY;

        transformMat.setScale(scale, scale);
        transformMat.postTranslate(dx, dy);
        transformMat.invert(inverseTransformMat);
        setup = false;
    }

    private void getBitmapCoordinates(float x, float y, @NonNull PointF out) {
        float[] points = new float[2];
        points[0] = x;
        points[1] = y;
        inverseTransformMat.mapPoints(points);
        out.x = points[0];
        out.y = points[1];
    }

    private void initPaints() {
        fingerPathPaint = getPaint(DEFAULT_COLOR, BRUSH_SIZE, false);
        mainPaint = getPaint(DEFAULT_COLOR, BRUSH_SIZE, false);
        sketchPaint = getPaint(Color.GRAY, 5.0f, false);
        gridPaint = getPaint(getColor(getContext(), R.color.grid_color), 1.0f, true);
        gridThinPaint = getPaint(getColor(getContext(), R.color.grid_color), 0.4f, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
