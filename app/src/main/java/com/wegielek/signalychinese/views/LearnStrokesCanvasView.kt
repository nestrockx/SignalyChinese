package com.wegielek.signalychinese.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.sdsmdg.harjot.vectormaster.VectorMasterDrawable
import com.sdsmdg.harjot.vectormaster.models.PathModel
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.enums.CharacterMode
import com.wegielek.signalychinese.models.FingerPath
import com.wegielek.signalychinese.models.HanziCharacter
import com.wegielek.signalychinese.utils.Utils.Companion.getPaint
import kotlin.math.abs

class LearnStrokesCanvasView : View {
    private var hanziCharacter: HanziCharacter? = null
    private lateinit var valueStrokeAnimator: ValueAnimator
    private lateinit var valueSuccessAnimator: ValueAnimator
    private lateinit var valueMistakeAnimator: ValueAnimator
    private lateinit var mPath: Path
    private lateinit var mainPaint: Paint
    private lateinit var fingerPathPaint: Paint
    private lateinit var sketchPaint: Paint
    private lateinit var gridPaint: Paint
    private lateinit var gridThinPaint: Paint
    private lateinit var mBitmap: Bitmap
    private lateinit var mCanvas: Canvas
    private var characterMode = CharacterMode.LEARN
    private val markerProgress = FloatArray(51)
    private val pos = FloatArray(2)
    private val tan = FloatArray(2)
    private val pathMeasure = PathMeasure()
    private val pointF = PointF()
    private var setup = true
    private val transformMat = Matrix()
    private val inverseTransformMat = Matrix()
    private var width = 0
    private var height = 0
    private val DEFAULT_COLOR = ContextCompat.getColor(this.context, R.color.dark_mode_white)
    private val BACKGROUND_COLOR = ContextCompat.getColor(this.context, R.color.canvas_background)
    private var mX = 0f
    private var mY = 0f
    private val paths = ArrayList<FingerPath>()
    private var strokeWidth = 0
    private val mBitmapPaint = Paint(Paint.DITHER_FLAG)
    private var hanziChar = 0.toChar()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    fun setMode(characterMode: CharacterMode) {
        this.characterMode = characterMode
        setHanziCharacter(hanziChar)
        if (characterMode === CharacterMode.PRESENTATION) {
            valueStrokeAnimator.duration = 1000
        } else {
            valueStrokeAnimator.duration = 1500
        }
    }

    fun setHanziCharacter(hanziChar: Char) {
        this.hanziChar = hanziChar
        clear()
        valueStrokeAnimator.cancel()
        hanziCharacter = initHanziStrokes(hanziChar)
        if (characterMode === CharacterMode.PRESENTATION) {
            valueStrokeAnimator.start()
        }
    }

    fun setDimensions(dimensions: Int) {
        width = dimensions
        height = dimensions
        setMeasuredDimension(width, height)
    }

    private fun clear() {
        valueStrokeAnimator.cancel()
        hanziCharacter?.reset()
        invalidate()
    }

    private fun initAnimators() {
        valueMistakeAnimator = ValueAnimator.ofArgb(
            ContextCompat.getColor(context, R.color.mistake),
            ContextCompat.getColor(context, R.color.mistake_fade)
        )
        val vMistakeDuration = 500
        valueMistakeAnimator.duration = vMistakeDuration.toLong()
        valueMistakeAnimator.addUpdateListener { animation: ValueAnimator ->
            val color: Int = animation.animatedValue as Int
            fingerPathPaint.color = color
            invalidate()
            if (color == Color.parseColor("#00B60000")) {
                fingerPathPaint.color = DEFAULT_COLOR
                fingerPathPaint.alpha = 0xff
                paths.clear()
            }
        }
        valueSuccessAnimator = ValueAnimator.ofArgb(
            ContextCompat.getColor(context, R.color.dark_mode_white),
            ContextCompat.getColor(context, R.color.success), DEFAULT_COLOR
        )
        val vSuccessDuration = 1000
        valueSuccessAnimator.duration = vSuccessDuration.toLong()
        valueSuccessAnimator.addUpdateListener { animation: ValueAnimator ->
            mainPaint.color = (animation.animatedValue as Int?)!!
            invalidate()
        }
        valueStrokeAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        if (characterMode === CharacterMode.PRESENTATION) {
            valueStrokeAnimator.duration = 1000
        } else {
            valueStrokeAnimator.duration = 1500
        }
        valueStrokeAnimator.addUpdateListener { animation: ValueAnimator ->
            markerProgress[hanziCharacter!!.index] =
                animation.animatedValue as Float
            invalidate()
        }
    }

    private fun initialize() {
        initAnimators()
        initPaints()
        hanziChar = '一'
        setHanziCharacter('一')
        width = DEFAULT_DIMENSIONS
        height = DEFAULT_DIMENSIONS
        mBitmap = Bitmap.createBitmap(BITMAP_DIMENSION, BITMAP_DIMENSION, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
        strokeWidth = BRUSH_SIZE
    }

    @SuppressLint("DiscouragedApi")
    private fun initHanziStrokes(hanziChar: Char): HanziCharacter {
        val hanziCharacterUnicode: String = Integer.toHexString(hanziChar.code or 0x10000).substring(1)
        val hanziCharacterStrokeList: MutableList<Path> = ArrayList()
        Log.d("UNICODE", "_" + hanziCharacterUnicode + " " + "drawable" + " " + context.packageName)
        val vectorMasterDrawable: VectorMasterDrawable
        try {
            vectorMasterDrawable = VectorMasterDrawable(
                context,
                resources.getIdentifier(
                    "_$hanziCharacterUnicode",
                    "drawable",
                    context.packageName
                )
            )
        } catch (e: NotFoundException) {
            Log.e(LOG_TAG, "Error message: " + e.message)
            return initHanziStrokes('一')
        }
        val outline = vectorMasterDrawable.getGroupModelByName("Hanzi")
        for (model: PathModel in outline.pathModels) {
            hanziCharacterStrokeList.add(model.path)
        }
        return HanziCharacter(hanziCharacterStrokeList)
    }

    private fun drawGrid() {
        mCanvas.drawLine(
            0.0f, 0.0f, BITMAP_DIMENSION.toFloat(), BITMAP_DIMENSION.toFloat(),
            gridThinPaint
        )
        mCanvas.drawLine(
            0.0f, BITMAP_DIMENSION.toFloat(), BITMAP_DIMENSION.toFloat(), 0.0f,
            gridThinPaint
        )
        mCanvas.drawLine(
            BITMAP_DIMENSION.toFloat() / 2,
            10.0f,
            BITMAP_DIMENSION.toFloat() / 2,
            BITMAP_DIMENSION - 10.0f,
            gridPaint
        )
        mCanvas.drawLine(
            10.0f,
            BITMAP_DIMENSION.toFloat() / 2,
            BITMAP_DIMENSION - 10.0f,
            BITMAP_DIMENSION.toFloat() / 2,
            gridPaint
        )
    }

    private fun drawHanziCharacterPath() {
        if (characterMode == CharacterMode.LEARN) {
            mCanvas.drawPath(hanziCharacter!!.currPath, sketchPaint)
            pathMeasure.setPath(hanziCharacter!!.currPath, false)
            pathMeasure.getPosTan(
                markerProgress[hanziCharacter!!.index] * pathMeasure.length,
                pos,
                tan
            )
            mCanvas.drawPoint(pos[0], pos[1], mainPaint)
            if (!valueStrokeAnimator.isRunning) {
                valueStrokeAnimator.start()
            }
        }
        if (characterMode == CharacterMode.PRESENTATION) {
            mCanvas.drawPath(hanziCharacter!!.currPath, sketchPaint)
            pathMeasure.setPath(hanziCharacter!!.currPath, false)
            pathMeasure.getPosTan(
                markerProgress[hanziCharacter!!.index]
                        * pathMeasure.length, pos, tan
            )
            mCanvas.drawPoint(pos[0], pos[1], (mainPaint))
            if (!valueStrokeAnimator.isRunning) {
                if (hanziCharacter!!.nextIndex()) {
                    valueStrokeAnimator.start()
                }
            }
        }
        if (hanziCharacter!!.isMatched) {
            if (!hanziCharacter!!.nextIndex()) {
                valueSuccessAnimator.start()
            }
            valueStrokeAnimator.cancel()
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        mCanvas.drawColor(BACKGROUND_COLOR)
        if (setup) {
            setupScaleMatrices()
        }
        drawGrid()
        if (!hanziCharacter!!.isFinished) {
            drawHanziCharacterPath()
            for (fp: FingerPath in paths) {
                mCanvas.drawPath(fp.path, (fingerPathPaint))
            }
        }
        if (hanziCharacter!!.isFinished) {
            for (i in 0 until (hanziCharacter!!.index + 1)) {
                mCanvas.drawPath(hanziCharacter!!.paths[i], (mainPaint))
            }
        } else {
            for (i in 0 until hanziCharacter!!.index) {
                mCanvas.drawPath(hanziCharacter!!.paths[i], (mainPaint))
            }
        }
        canvas.drawBitmap((mBitmap), transformMat, mBitmapPaint)
        canvas.restore()
    }

    private fun touchStart(x: Float, y: Float) {
        if (characterMode !== CharacterMode.PRESENTATION) {
            if (valueMistakeAnimator.isRunning) {
                valueMistakeAnimator.cancel()
                fingerPathPaint.color = DEFAULT_COLOR
                fingerPathPaint.alpha = 0xff
                paths.clear()
            }
            mPath = Path()
            val fp = FingerPath(DEFAULT_COLOR, strokeWidth, mPath)
            paths.add(fp)
            mPath.reset()
            mPath.moveTo(x, y)
            mX = x
            mY = y
            if (hanziCharacter!!.matchStart(mX, mY, 60.0f)) {
                Log.i(LOG_TAG, "MATCHED_START")
            }
        }
    }

    private fun touchMove(x: Float, y: Float) {
        if (characterMode !== CharacterMode.PRESENTATION) {
            val dx = abs(x - mX)
            val dy = abs(y - mY)
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                mX = x
                mY = y
            }
            if (hanziCharacter!!.matchControlPoint(mX, mY, 50.0f)) {
                Log.i(LOG_TAG, "MATCHED_CONTROL_POINT")
            }
        }
    }

    private fun touchUp() {
        if (characterMode !== CharacterMode.PRESENTATION) {
            if (hanziCharacter!!.isMatchedStart && hanziCharacter!!.isMatchedControlPoint) {
                if (hanziCharacter!!.matchEnd(mX, mY, 60.0f)) {
                    Log.i(LOG_TAG, "MATCHED_END")
                } else {
                    hanziCharacter!!.matchReset()
                }
            }
            mPath.lineTo(mX, mY)
            if (hanziCharacter!!.isMatched) {
                paths.clear()
            } else {
                valueMistakeAnimator.start()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                getBitmapCoordinates(x, y, pointF)
                Log.i(LOG_TAG, "Starting point " + pointF.x + " : " + pointF.y)
                touchStart(pointF.x, pointF.y)
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                getBitmapCoordinates(x, y, pointF)
                touchMove(pointF.x, pointF.y)
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                performClick()
                touchUp()
                invalidate()
            }
        }
        return true
    }

    private fun setupScaleMatrices() {
        // View size.
        val width = width.toFloat()
        val height = height.toFloat()
        val scaleW = width / BITMAP_DIMENSION
        val scaleH = height / BITMAP_DIMENSION
        var scale = scaleW
        if (scale > scaleH) {
            scale = scaleH
        }

        // Translation to center bitmap in view after it is scaled up.
        val centerX = BITMAP_DIMENSION * scale / 2
        val centerY = BITMAP_DIMENSION * scale / 2
        val dx = width / 2 - centerX
        val dy = height / 2 - centerY
        transformMat.setScale(scale, scale)
        transformMat.postTranslate(dx, dy)
        transformMat.invert(inverseTransformMat)
        setup = false
    }

    private fun getBitmapCoordinates(x: Float, y: Float, out: PointF) {
        val points = FloatArray(2)
        points[0] = x
        points[1] = y
        inverseTransformMat.mapPoints(points)
        out.x = points[0]
        out.y = points[1]
    }

    private fun initPaints() {
        fingerPathPaint = getPaint(DEFAULT_COLOR, BRUSH_SIZE.toFloat(), false)
        mainPaint = getPaint(DEFAULT_COLOR, BRUSH_SIZE.toFloat(), false)
        sketchPaint = getPaint(Color.GRAY, 5.0f, false)
        gridPaint = getPaint(
            ContextCompat.getColor(
                context, R.color.grid_color
            ), 1.0f, true
        )
        gridThinPaint = getPaint(
            ContextCompat.getColor(
                context, R.color.grid_color
            ), 0.4f, true
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    companion object {
        private const val LOG_TAG = "LearnStrokesView"
        private const val BITMAP_DIMENSION = 400
        private const val DEFAULT_DIMENSIONS = 1000
        private const val BRUSH_SIZE = 10
        private const val TOUCH_TOLERANCE = 4f
    }
}
