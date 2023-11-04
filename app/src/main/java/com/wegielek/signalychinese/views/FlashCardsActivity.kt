package com.wegielek.signalychinese.views

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.databinding.ActivityFlashCardsBinding
import com.wegielek.signalychinese.enums.Direction
import com.wegielek.signalychinese.utils.Utils.Companion.dpToPixels
import com.wegielek.signalychinese.utils.Utils.Companion.getScreenHeight
import com.wegielek.signalychinese.utils.Utils.Companion.getScreenWidth
import com.wegielek.signalychinese.utils.Utils.Companion.isScreenRotated
import kotlin.math.abs

class FlashCardsActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityFlashCardsBinding
    private lateinit var frontAnimUp: AnimatorSet
    private lateinit var backAnimUp: AnimatorSet
    private lateinit var swipeListener: SwipeListener
    private var isFront = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityFlashCardsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val flashCardsToolbar: Toolbar = mBinding.flashCardsToolbar
        setSupportActionBar(flashCardsToolbar)
        flashCardsToolbar.setTitleTextColor(getColor(R.color.dark_mode_white))
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setTitle("Flash cards")
        }
        swipeListener = SwipeListener(mBinding.gesturePlain)
        if (!isScreenRotated(this)) {
            mBinding.cardFront.width = getScreenWidth(this) - dpToPixels(this, 64f)
            mBinding.cardFront.height = getScreenWidth(this) - dpToPixels(this, 64f)
            mBinding.cardBack.width = getScreenWidth(this) - dpToPixels(this, 64f)
            mBinding.cardBack.height = getScreenWidth(this) - dpToPixels(this, 64f)
        } else {
            mBinding.cardFront.width = getScreenHeight(this) - dpToPixels(this, 64f)
            mBinding.cardFront.height = getScreenHeight(this) - dpToPixels(this, 64f)
            mBinding.cardBack.width = getScreenHeight(this) - dpToPixels(this, 64f)
            mBinding.cardBack.height = getScreenHeight(this) - dpToPixels(this, 64f)
        }
        val scale = resources.displayMetrics.density
        mBinding.cardFront.cameraDistance = 8000 * scale
        mBinding.cardBack.cameraDistance = 8000 * scale
        frontAnimUp =
            AnimatorInflater.loadAnimator(this, R.animator.front_animator_up) as AnimatorSet
        backAnimUp = AnimatorInflater.loadAnimator(this, R.animator.back_animator_up) as AnimatorSet
    }

    private fun flip(direction: Direction) {
        if (!frontAnimUp.isRunning && !backAnimUp.isRunning) {
            if (direction === Direction.UP) {
                isFront = if (isFront) {
                    frontAnimUp.setTarget(mBinding.cardFront)
                    backAnimUp.setTarget(mBinding.cardBack)
                    frontAnimUp.start()
                    backAnimUp.start()
                    false
                } else {
                    backAnimUp.setTarget(mBinding.cardFront)
                    frontAnimUp.setTarget(mBinding.cardBack)
                    backAnimUp.start()
                    frontAnimUp.start()
                    true
                }
                mBinding.button3.visibility = View.INVISIBLE
                mBinding.button4.visibility = View.INVISIBLE
                mBinding.button3.postDelayed({ mBinding.button3.visibility = View.VISIBLE }, 500)
                mBinding.button4.postDelayed({ mBinding.button4.visibility = View.VISIBLE }, 500)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class SwipeListener(v: View) :
        OnTouchListener {
        var gestureDetector: GestureDetector

        init {
            val threshold = 100
            val velocityThreshold = 100
            val listener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    val xDiff = e2.x - e1!!.x
                    val yDiff = e2.y - e1.y
                    try {
                        if (abs(xDiff) > abs(yDiff)) {
                            if (abs(xDiff) > threshold && abs(velocityX) > velocityThreshold) {
                                if (xDiff > 0) {
                                    //right
                                } else {
                                    //left
                                }
                                return true
                            }
                        } else {
                            if (abs(yDiff) > threshold && abs(velocityY) > velocityThreshold) {
                                if (yDiff > 0) {
                                    //down
                                } else {
                                    //up
                                    flip(Direction.UP)
                                }
                                return true
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return false
                }
            }
            gestureDetector = GestureDetector(applicationContext, listener)
            v.setOnTouchListener(this)
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            v.performClick()
            return gestureDetector.onTouchEvent(event)
        }
    }
}