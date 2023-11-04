package com.wegielek.signalychinese.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.RecognitionResult
import com.wegielek.signalychinese.interfaces.CanvasViewListener
import com.wegielek.signalychinese.viewmodels.MainViewModel

class CharacterRecognizeCanvasView @JvmOverloads constructor(
    context: Context?,
    attributeSet: AttributeSet? = null
) :
    View(context, attributeSet) {
    private val remoteModelManager = RemoteModelManager.getInstance()
    private lateinit var model: DigitalInkRecognitionModel
    private lateinit var recognizer: DigitalInkRecognizer
    private lateinit var mainViewModel: MainViewModel
    private val currentStrokePaint: Paint = Paint()
    private val canvasPaint: Paint
    private lateinit var drawCanvas: Canvas
    private lateinit var canvasBitmap: Bitmap
    private lateinit var canvasViewListener: CanvasViewListener

    init {
        currentStrokePaint.color = Color.WHITE
        currentStrokePaint.isAntiAlias = true
        currentStrokePaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, STROKE_WIDTH_DP.toFloat(), resources.displayMetrics
        )
        currentStrokePaint.style = Paint.Style.STROKE
        currentStrokePaint.strokeJoin = Paint.Join.ROUND
        currentStrokePaint.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
        initialize(100, 100)
    }

    fun setOnRecognizeListener(onRecognizeListener: CanvasViewListener) {
        canvasViewListener = onRecognizeListener
    }

    fun initialize(width: Int, height: Int, mainViewModel: MainViewModel) {
        this.mainViewModel = mainViewModel
        initialize(width, height)
    }

    private fun initialize(width: Int, height: Int) {
        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap)
        drawCanvas.drawColor(Color.BLACK)
        invalidate()
        val modelIdentifier: DigitalInkRecognitionModelIdentifier? = try {
            DigitalInkRecognitionModelIdentifier.fromLanguageTag("zh-Hani-CN")
        } catch (e: MlKitException) {
            throw RuntimeException()
        }
        if (modelIdentifier == null) {
            Toast.makeText(context, "Model not found", Toast.LENGTH_LONG).show()
            return
        }
        model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
        // Get a recognizer for the language
        recognizer = DigitalInkRecognition.getClient(
            DigitalInkRecognizerOptions.builder(model).build()
        )
        isModelDownloaded.onSuccessTask { result: Boolean? ->
            if (!result!!) {
                downloadModel()
            }
            Tasks.forResult<Any?>(null)
        }
    }

    fun clear() {
        mainViewModel.resetCurrentVisibleStroke()
        mainViewModel.setInkBuilder(Ink.builder())
        drawCanvas.drawColor(Color.BLACK)
        invalidate()
        clearStrokesHistory()
    }

    fun undoStroke() {
        if (mainViewModel.strokesHistorySize > 0) {
            val newInkBuilder = Ink.Builder()
            mainViewModel.removeFromStrokesHistory(mainViewModel.strokesHistorySize - 1)
            for (i in 0 until mainViewModel.strokesHistorySize) {
                newInkBuilder.addStroke(mainViewModel.strokeHistoryBuild(i)!!)
            }
            mainViewModel.setInkBuilder(newInkBuilder)
            if (mainViewModel.strokesHistorySize > 1) {
                recognizeCharacter()
            }
        }
        if (mainViewModel.visibleStrokeSize > 0) {
            val newCurrentVisibleStroke = Path()
            mainViewModel.removeVisibleStroke(mainViewModel.visibleStrokeSize - 1)
            for (i in 0 until mainViewModel.visibleStrokeSize) {
                newCurrentVisibleStroke.addPath(mainViewModel.getVisibleStroke(i)!!)
            }
            mainViewModel.setCurrentVisibleStroke(newCurrentVisibleStroke)
            drawCanvas.drawColor(Color.BLACK)
            invalidate()
        }
    }

    private fun clearStrokesHistory() {
        mainViewModel.clearStrokesHistory()
        mainViewModel.clearVisibleStrokes()
    }

    private fun recognizeCharacter() {
        isModelDownloaded.onSuccessTask { result: Boolean? ->
            if (!result!!) {
                Toast.makeText(context, "Model not downloaded yet", Toast.LENGTH_SHORT).show()
                return@onSuccessTask Tasks.forResult<Any?>(null)
            }
            recognize()
            Tasks.forResult(null)
        }
    }

    private fun recognize() {
        recognizer.recognize(mainViewModel.inkBuilderBuild()!!)
            .addOnSuccessListener { result: RecognitionResult ->
                canvasViewListener.onResults(
                    result.candidates
                )
            }.addOnFailureListener { e: Exception ->
                Toast.makeText(
                    context,
                    "Error during recognition: $e",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private val isModelDownloaded: Task<Boolean>
        get() = remoteModelManager.isModelDownloaded(model)

    private fun downloadModel() {
        remoteModelManager
            .download(model, DownloadConditions.Builder().build())
            .addOnSuccessListener {
                Log.i(LOG_TAG, "Model downloaded")
                Toast.makeText(context, "Model downloaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e: Exception ->
                Log.e(
                    LOG_TAG,
                    "Error while downloading a model: $e"
                )
            }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint)
        mainViewModel.currentVisibleStroke?.let { drawCanvas.drawPath(it, currentStrokePaint) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val x = event.x
        val y = event.y
        val t = System.currentTimeMillis()
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mainViewModel.moveToCurrentVisibleStroke(x, y)
                mainViewModel.addStrokeBuilderPoint(Ink.Point.create(x, y, t))
            }

            MotionEvent.ACTION_MOVE -> {
                mainViewModel.lineToCurrentVisibleStroke(x, y)
                mainViewModel.addStrokeBuilderPoint(Ink.Point.create(x, y, t))
            }

            MotionEvent.ACTION_UP -> {
                mainViewModel.lineToCurrentVisibleStroke(x, y)
                mainViewModel.addVisibleStroke(Path(mainViewModel.currentVisibleStroke))
                mainViewModel.addStrokeBuilderPoint(Ink.Point.create(x, y, t))
                mainViewModel.addToStrokesHistory(mainViewModel.getmStrokeBuilder()!!)
                mainViewModel.addInkBuilderStroke(mainViewModel.strokeBuilderBuild())
                mainViewModel.clearStrokeBuilder()
                recognizeCharacter()
            }
        }
        invalidate()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    companion object {
        private const val LOG_TAG = "CanvasView"
        private const val STROKE_WIDTH_DP = 3
    }
}
