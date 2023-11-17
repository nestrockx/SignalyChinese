package com.wegielek.signalychinese.views.fragments

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.databinding.FragmentFlashCardBinding
import com.wegielek.signalychinese.enums.Direction
import com.wegielek.signalychinese.utils.Preferences.Companion.isFlashCardsReversed
import com.wegielek.signalychinese.utils.Utils
import com.wegielek.signalychinese.viewmodels.FlashCardsViewModel
import com.wegielek.signalychinese.views.FlashCardsActivity
import kotlin.math.abs

class FlashCardFragment : Fragment() {

    private lateinit var frontAnimUp: AnimatorSet
    private lateinit var backAnimUp: AnimatorSet
    private lateinit var mBinding: FragmentFlashCardBinding
    private lateinit var swipeListener: FlashCardFragment.SwipeListener
    private lateinit var mFlashCardsViewModel: FlashCardsViewModel
    private var isFront = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentFlashCardBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFlashCardsViewModel = (activity as FlashCardsActivity).flashCardsViewModel

        mFlashCardsViewModel.flashCardsList.observe(viewLifecycleOwner) {
            mFlashCardsViewModel.currentIndex.observe(viewLifecycleOwner) { index ->
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    if (!isFlashCardsReversed(requireContext())) {
                        mBinding.cardFront.text =
                            it[index].traditionalSign + "\n" + it[index].simplifiedSign
                        mBinding.cardBack.text =
                            it[index].pronunciation + "\n\n" + it[index].translation.replace(
                                " /",
                                ";"
                            )
                    } else {
                        mBinding.cardFront.text =
                            it[index].pronunciation + "\n\n" + it[index].translation.replace(
                                " /",
                                ";"
                            )
                        mBinding.cardBack.text =
                            it[index].traditionalSign + "\n" + it[index].simplifiedSign

                    }
                }, 150)

                if (mFlashCardsViewModel.currentIndex.value == mFlashCardsViewModel.getFlashCardsList()!!.size - 1) {
                    mBinding.nextBtn.visibility = View.INVISIBLE
                }
                if (mFlashCardsViewModel.currentIndex.value == 0) {
                    mBinding.prevBtn.visibility = View.INVISIBLE
                }
            }
        }

        mBinding.incorrectBtn.visibility = View.INVISIBLE
        mBinding.correctBtn.visibility = View.INVISIBLE

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })

        swipeListener = SwipeListener(mBinding.gesturePlain)
        if (!Utils.isScreenRotated(requireContext())) {
            mBinding.cardFront.width = Utils.getScreenWidth(activity as FlashCardsActivity) -
                    Utils.dpToPixels(requireContext(), 64f)
            mBinding.cardFront.height = Utils.getScreenWidth(activity as FlashCardsActivity) -
                    Utils.dpToPixels(requireContext(), 64f)
            mBinding.cardBack.width = Utils.getScreenWidth(activity as FlashCardsActivity) -
                    Utils.dpToPixels(requireContext(), 64f)
            mBinding.cardBack.height = Utils.getScreenWidth(activity as FlashCardsActivity) -
                    Utils.dpToPixels(requireContext(), 64f)
        } else {
            mBinding.cardFront.width = Utils.getScreenHeight(activity as FlashCardsActivity) -
                    Utils.dpToPixels(requireContext(), 130f)
            mBinding.cardFront.height = Utils.getScreenHeight(activity as FlashCardsActivity) -
                    Utils.dpToPixels(requireContext(), 150f)
            mBinding.cardBack.width = Utils.getScreenHeight(activity as FlashCardsActivity) -
                    Utils.dpToPixels(requireContext(), 130f)
            mBinding.cardBack.height = Utils.getScreenHeight(activity as FlashCardsActivity) -
                    Utils.dpToPixels(requireContext(), 150f)
        }
        val scale = resources.displayMetrics.density
        mBinding.cardFront.cameraDistance = 8000 * scale
        mBinding.cardBack.cameraDistance = 8000 * scale
        frontAnimUp =
            AnimatorInflater.loadAnimator(requireContext(), R.animator.front_animator_up)
                    as AnimatorSet
        backAnimUp =
            AnimatorInflater.loadAnimator(requireContext(), R.animator.back_animator_up)
                    as AnimatorSet


        mBinding.flipBtn.setOnClickListener {
            flip(Direction.UP)
        }

        mBinding.nextBtn.setOnClickListener {
            if (mFlashCardsViewModel.increaseIndex()) {
                (activity as FlashCardsActivity).switchFragment(Direction.RIGHT)
            }
        }

        mBinding.prevBtn.setOnClickListener {
            if (mFlashCardsViewModel.decreaseIndex()) {
                (activity as FlashCardsActivity).switchFragment(Direction.LEFT)
            }
        }

        mBinding.incorrectBtn.setOnClickListener {
            if (mFlashCardsViewModel.increaseIndex()) {
                (activity as FlashCardsActivity).switchFragment(
                    Direction.RIGHT
                )
            } else {
                (activity as FlashCardsActivity).finish()
            }
        }

        mBinding.correctBtn.setOnClickListener {
            if (mFlashCardsViewModel.increaseIndex()) {
                (activity as FlashCardsActivity).switchFragment(
                    Direction.RIGHT
                )
            } else {
                (activity as FlashCardsActivity).finish()
            }
        }
    }

    private fun flip(direction: Direction) {
        if (!frontAnimUp.isRunning && !backAnimUp.isRunning) {
            if (direction == Direction.UP) {
                isFront = if (isFront) {
                    frontAnimUp.setTarget(mBinding.cardFront)
                    backAnimUp.setTarget(mBinding.cardBack)
                    frontAnimUp.start()
                    backAnimUp.start()

                    mBinding.incorrectBtn.postDelayed({ mBinding.incorrectBtn.visibility = View.VISIBLE }, 500)
                    mBinding.correctBtn.postDelayed({ mBinding.correctBtn.visibility = View.VISIBLE }, 500)

                    false
                } else {
                    backAnimUp.setTarget(mBinding.cardFront)
                    frontAnimUp.setTarget(mBinding.cardBack)
                    backAnimUp.start()
                    frontAnimUp.start()

                    mBinding.incorrectBtn.visibility = View.INVISIBLE
                    mBinding.correctBtn.visibility = View.INVISIBLE

                    true
                }
            }
        }
    }

    private inner class SwipeListener(v: View) :
        View.OnTouchListener {
        var gestureDetector: GestureDetector

        init {
            val threshold = 100
            val velocityThreshold = 100
            val listener: GestureDetector.SimpleOnGestureListener =
                object : GestureDetector.SimpleOnGestureListener() {
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
                                        if (mFlashCardsViewModel.decreaseIndex()) {
                                            (activity as FlashCardsActivity).switchFragment(
                                                Direction.LEFT
                                            )
                                        }
                                    } else {
                                        //left
                                        if (mFlashCardsViewModel.increaseIndex()) {
                                            (activity as FlashCardsActivity).switchFragment(
                                                Direction.RIGHT
                                            )
                                        }
                                    }
                                    return true
                                }
                            } else {
                                if (abs(yDiff) > threshold && abs(velocityY) > velocityThreshold) {
                                    if (yDiff > 0) {
                                        //down
                                        flip(Direction.UP)
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
            gestureDetector = GestureDetector(requireContext(), listener)
            v.setOnTouchListener(this)
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            v.performClick()
            return gestureDetector.onTouchEvent(event)
        }
    }
}