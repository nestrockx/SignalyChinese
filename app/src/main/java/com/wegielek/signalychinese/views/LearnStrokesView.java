package com.wegielek.signalychinese.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
    private CharacterMode characterMode = CharacterMode.LEARN;
    boolean hintOn = false;

    private ValueAnimator valueStrokeAnimator;
    private ValueAnimator valueSuccessAnimator;
    private ValueAnimator valueMistakeAnimator;

    private final float[] markerProgress = new float[51];
    private final float[] pos = new float[2];
    private final float[] tan = new float[2];
    private final PathMeasure pathMeasure = new PathMeasure();

    private final PointF pointF = new PointF();
    private boolean setup = false;
    private static final int BITMAP_DIMENSION = 400;

    private final Matrix transformMat = new Matrix();
    private final Matrix inverseTransformMat = new Matrix();

    private final int DEFAULT_DIMENSIONS = 1000;
    private int width;
    private int height;

    private int BRUSH_SIZE = 10;
    private final int DEFAULT_COLOR = ContextCompat.getColor(this.getContext(), R.color.white);
    private final int BACKGROUND_COLOR = ContextCompat.getColor(this.getContext(), R.color.writing_background);
    private final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;

    private Paint mainPaint;
    private Paint fingerPathPaint;
    private Paint sketchPaint;
    private Paint gridPaint;
    private Paint gridThinPaint;


    private final ArrayList<FingerPath> paths = new ArrayList<>();
    private int currentColor;
    private int backgroundColor = BACKGROUND_COLOR;
    private int strokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private final Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);


    public LearnStrokesView(Context context) {
        super(context);
    }

    public LearnStrokesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public LearnStrokesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setHanziCharacter(char hanziChar) {
        clear();
        valueStrokeAnimator.cancel();
        this.hanziCharacter = initHanziStrokes(hanziChar);
    }

    public void setDimensions(int dimensions) {
        width = dimensions;
        height = dimensions;
    }

    public void clear() {
        valueStrokeAnimator.cancel();
        if(hanziCharacter != null) {
            hanziCharacter.reset();
        }
        backgroundColor = BACKGROUND_COLOR;
        invalidate();
    }

    private void initAnimators() {
        valueMistakeAnimator = ValueAnimator.ofArgb(ContextCompat.getColor(getContext(), R.color.mistake),
                ContextCompat.getColor(getContext(), R.color.mistake_fade));
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

        valueSuccessAnimator = ValueAnimator.ofArgb(ContextCompat.getColor(getContext(), R.color.white),
                ContextCompat.getColor(getContext(), R.color.success), DEFAULT_COLOR);
        int vSuccessDuration = 1000;
        valueSuccessAnimator.setDuration(vSuccessDuration);
        valueSuccessAnimator.addUpdateListener(animation -> {
            mainPaint.setColor((Integer) animation.getAnimatedValue());
            invalidate();
        });

        valueStrokeAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        int mDuration = 2000;
        valueStrokeAnimator.setDuration(mDuration);
        valueStrokeAnimator.addUpdateListener(animation -> {
            markerProgress[hanziCharacter.getIndex()] = (float)animation.getAnimatedValue();
            invalidate();
        });
    }

    private void initialize() {
        this.characterMode = CharacterMode.LEARN;
        ///
        initAnimators();
        initPaints();
        setHanziCharacter('一');
        ///
        this.width = DEFAULT_DIMENSIONS;
        this.height = DEFAULT_DIMENSIONS;
        mBitmap = Bitmap.createBitmap(BITMAP_DIMENSION, BITMAP_DIMENSION, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
    }

    public void showHint() {
        hintOn = true;
        invalidate();
    }

    public void hideHint() {
        hintOn = false;
        invalidate();
    }

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
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }

        GroupModel outline = vectorMasterDrawable.getGroupModelByName("Hanzi");
        for (PathModel model : outline.getPathModels()) {
            hanziCharacterStrokeList.add(model.getPath());
        }

        return new HanziCharacter(hanziCharacterStrokeList);
    }

    private void drawGrid() {
        mCanvas.drawLine((float)BITMAP_DIMENSION / 2, 10.0f, (float)BITMAP_DIMENSION / 2, BITMAP_DIMENSION - 10.0f, gridPaint);
        mCanvas.drawLine((float)BITMAP_DIMENSION / 4, 30.0f, (float)BITMAP_DIMENSION / 4, BITMAP_DIMENSION - 30.0f, gridThinPaint);
        mCanvas.drawLine((float)BITMAP_DIMENSION * 3 / 4, 30.0f, (float)BITMAP_DIMENSION * 3 / 4, BITMAP_DIMENSION - 30.0f, gridThinPaint);
        mCanvas.drawLine(10.0f, (float)BITMAP_DIMENSION / 2, BITMAP_DIMENSION - 10.0f, (float)BITMAP_DIMENSION / 2, gridPaint);
        mCanvas.drawLine(10.0f, (float)BITMAP_DIMENSION / 4, BITMAP_DIMENSION - 10.0f, (float)BITMAP_DIMENSION / 4, gridThinPaint);
        mCanvas.drawLine(10.0f, (float)BITMAP_DIMENSION * 3 / 4, BITMAP_DIMENSION - 10.0f, (float)BITMAP_DIMENSION * 3 / 4, gridThinPaint);
    }

    private void drawHanziCharacterPath() {
        if (hanziCharacter != null) {
            if (characterMode.equals(CharacterMode.LEARN) || hintOn) {
                mCanvas.drawPath(hanziCharacter.getCurrPath(), sketchPaint);

                pathMeasure.setPath(hanziCharacter.getCurrPath(), false);
                pathMeasure.getPosTan(markerProgress[hanziCharacter.getIndex()]
                        * pathMeasure.getLength(), pos, tan);
                mCanvas.drawPoint(pos[0], pos[1], mainPaint);

                if (!valueStrokeAnimator.isRunning()) {
                    valueStrokeAnimator.start();
                }
            }
            if (hanziCharacter.isMatched()) {
                if (!hanziCharacter.nextIndex()) {
                    valueSuccessAnimator.start();
                }
                hintOn = false;
                valueStrokeAnimator.cancel();
                invalidate();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(backgroundColor);

        if(!setup) {
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

        if(valueMistakeAnimator.isRunning()) {
            valueMistakeAnimator.cancel();
            fingerPathPaint.setColor(DEFAULT_COLOR);
            fingerPathPaint.setAlpha(0xff);
            paths.clear();
        }

        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, strokeWidth, mPath);
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

    private void touchMove(float x, float y) {
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

    private void touchUp() {
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                getBitmapCoords(x, y, pointF);
                Log.i(LOG_TAG, "Starting point " + pointF.x + " : " + pointF.y);
                touchStart(pointF.x, pointF.y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE :
                getBitmapCoords(x, y, pointF);
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
        setup = true;
    }

    private void getBitmapCoords(float x, float y, PointF out) {
        float[] points = new float[2];
        points[0] = x;
        points[1] = y;
        inverseTransformMat.mapPoints(points);
        out.x = points[0];
        out.y = points[1];
    }

    private void initPaints() {
        fingerPathPaint = new Paint();
        fingerPathPaint.setAntiAlias(true);
        fingerPathPaint.setDither(true);
        fingerPathPaint.setColor(DEFAULT_COLOR);
        fingerPathPaint.setStyle(Paint.Style.STROKE);
        fingerPathPaint.setStrokeJoin(Paint.Join.ROUND);
        fingerPathPaint.setStrokeCap(Paint.Cap.ROUND);
        fingerPathPaint.setXfermode(null);
        fingerPathPaint.setAlpha(0xff);
        fingerPathPaint.setStrokeWidth(BRUSH_SIZE);

        mainPaint = new Paint();
        mainPaint.setAntiAlias(true);
        mainPaint.setDither(true);
        mainPaint.setColor(DEFAULT_COLOR);
        mainPaint.setStyle(Paint.Style.STROKE);
        mainPaint.setStrokeJoin(Paint.Join.ROUND);
        mainPaint.setStrokeCap(Paint.Cap.ROUND);
        mainPaint.setXfermode(null);
        mainPaint.setAlpha(0xff);
        mainPaint.setStrokeWidth(BRUSH_SIZE);

        sketchPaint = new Paint();
        sketchPaint.setAntiAlias(true);
        sketchPaint.setDither(true);
        sketchPaint.setColor(Color.GRAY);
        sketchPaint.setStyle(Paint.Style.STROKE);
        sketchPaint.setStrokeJoin(Paint.Join.ROUND);
        sketchPaint.setStrokeCap(Paint.Cap.ROUND);
        sketchPaint.setXfermode(null);
        sketchPaint.setAlpha(0xff);
        sketchPaint.setStrokeWidth(5.0f);

        gridPaint = new Paint();
        gridPaint.setAntiAlias(true);
        gridPaint.setDither(true);
        gridPaint.setColor(Color.parseColor("#666666"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeJoin(Paint.Join.ROUND);
        gridPaint.setStrokeCap(Paint.Cap.ROUND);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 5f));
        gridPaint.setXfermode(null);
        gridPaint.setAlpha(0xff);
        gridPaint.setStrokeWidth(1.0f);

        gridThinPaint = new Paint();
        gridThinPaint.setAntiAlias(true);
        gridThinPaint.setDither(true);
        gridThinPaint.setColor(Color.parseColor("#555555"));
        gridThinPaint.setStyle(Paint.Style.STROKE);
        gridThinPaint.setStrokeJoin(Paint.Join.ROUND);
        gridThinPaint.setStrokeCap(Paint.Cap.ROUND);
        gridThinPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 5f));
        gridThinPaint.setXfermode(null);
        gridThinPaint.setAlpha(0xff);
        gridThinPaint.setStrokeWidth(0.2f);
    }

}
