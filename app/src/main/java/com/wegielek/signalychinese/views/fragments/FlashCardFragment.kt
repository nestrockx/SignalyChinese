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
import com.wegielek.signalychinese.utils.Utils.Companion.dpToPixels
import com.wegielek.signalychinese.utils.Utils.Companion.getScreenHeight
import com.wegielek.signalychinese.utils.Utils.Companion.getScreenWidth
import com.wegielek.signalychinese.utils.Utils.Companion.isScreenRotated
import com.wegielek.signalychinese.viewmodels.FlashCardsViewModel
import com.wegielek.signalychinese.views.FlashCardsActivity
import kotlin.math.abs

class FlashCardFragment : Fragment() {

    private lateinit var binding: FragmentFlashCardBinding
    private lateinit var mFrontAnimUp: AnimatorSet
    private lateinit var mBackAnimUp: AnimatorSet
    private lateinit var mSwipeListener: FlashCardFragment.SwipeListener
    private lateinit var mFlashCardsViewModel: FlashCardsViewModel
    private var isFront = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFlashCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFlashCardsViewModel = (activity as FlashCardsActivity).flashCardsViewModel

        mFlashCardsViewModel.flashCardsList.observe(viewLifecycleOwner) {
            mFlashCardsViewModel.currentIndex.observe(viewLifecycleOwner) { index ->
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    if (it.isNotEmpty()) {
                        if (!isFlashCardsReversed()) {
                            binding.cardFront.text =
                                it[index].traditionalSign + "\n" + it[index].simplifiedSign
                            binding.cardBack.text =
                                it[index].pronunciation + "\n\n" + it[index].translation.replace(
                                    " /",
                                    ";"
                                )
                        } else {
                            binding.cardFront.text =
                                it[index].pronunciation + "\n\n" + it[index].translation.replace(
                                    " /",
                                    ";"
                                )
                            binding.cardBack.text =
                                it[index].traditionalSign + "\n" + it[index].simplifiedSign

                        }
                    } else {
                        (context as FlashCardsActivity).finish()
                    }
                }, 150)

                if (mFlashCardsViewModel.getCurrentIndex() == mFlashCardsViewModel.getFlashCardsList()!!.size - 1) {
                    binding.nextBtn.visibility = View.INVISIBLE
                }
                if (mFlashCardsViewModel.getCurrentIndex() == 0) {
                    binding.prevBtn.visibility = View.INVISIBLE
                }
            }
        }

        binding.incorrectBtn.visibility = View.INVISIBLE
        binding.correctBtn.visibility = View.INVISIBLE

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })

        mSwipeListener = SwipeListener(binding.gesturePlain)
        if (!isScreenRotated(requireContext())) {
            binding.cardFront.width = getScreenWidth(activity as FlashCardsActivity) -
                    dpToPixels(requireContext(), 64f)
            binding.cardFront.height = getScreenWidth(activity as FlashCardsActivity) -
                    dpToPixels(requireContext(), 64f)
            binding.cardBack.width = getScreenWidth(activity as FlashCardsActivity) -
                    dpToPixels(requireContext(), 64f)
            binding.cardBack.height = getScreenWidth(activity as FlashCardsActivity) -
                    dpToPixels(requireContext(), 64f)
        } else {
            binding.cardFront.width = getScreenHeight(activity as FlashCardsActivity) -
                    dpToPixels(requireContext(), 130f)
            binding.cardFront.height = getScreenHeight(activity as FlashCardsActivity) -
                    dpToPixels(requireContext(), 150f)
            binding.cardBack.width = getScreenHeight(activity as FlashCardsActivity) -
                    dpToPixels(requireContext(), 130f)
            binding.cardBack.height = getScreenHeight(activity as FlashCardsActivity) -
                    dpToPixels(requireContext(), 150f)
        }
        val scale = resources.displayMetrics.density
        binding.cardFront.cameraDistance = 8000 * scale
        binding.cardBack.cameraDistance = 8000 * scale
        mFrontAnimUp =
            AnimatorInflater.loadAnimator(requireContext(), R.animator.front_animator_up)
                    as AnimatorSet
        mBackAnimUp =
            AnimatorInflater.loadAnimator(requireContext(), R.animator.back_animator_up)
                    as AnimatorSet


        binding.flipBtn.setOnClickListener {
            flip(Direction.UP)
        }

        binding.nextBtn.setOnClickListener {
            if (mFlashCardsViewModel.increaseIndex()) {
                (activity as FlashCardsActivity).switchFragment(Direction.RIGHT)
            }
        }

        binding.prevBtn.setOnClickListener {
            if (mFlashCardsViewModel.decreaseIndex()) {
                (activity as FlashCardsActivity).switchFragment(Direction.LEFT)
            }
        }

        binding.incorrectBtn.setOnClickListener {
            if (mFlashCardsViewModel.increaseIndex()) {
                (activity as FlashCardsActivity).switchFragment(
                    Direction.RIGHT
                )
            } else {
                (activity as FlashCardsActivity).finish()
            }
        }

        binding.correctBtn.setOnClickListener {
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
        if (!mFrontAnimUp.isRunning && !mBackAnimUp.isRunning) {
            if (direction == Direction.UP || direction == Direction.DOWN) {
                isFront = if (isFront) {
                    mFrontAnimUp.setTarget(binding.cardFront)
                    mBackAnimUp.setTarget(binding.cardBack)
                    mFrontAnimUp.start()
                    mBackAnimUp.start()

                    //binding.incorrectBtn.postDelayed({ binding.incorrectBtn.visibility = View.VISIBLE }, 500)
                    binding.correctBtn.postDelayed({ binding.correctBtn.visibility = View.VISIBLE }, 500)

                    false
                } else {
                    mBackAnimUp.setTarget(binding.cardFront)
                    mFrontAnimUp.setTarget(binding.cardBack)
                    mBackAnimUp.start()
                    mFrontAnimUp.start()

                    binding.incorrectBtn.visibility = View.INVISIBLE
                    binding.correctBtn.visibility = View.INVISIBLE

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
                                        flip(Direction.DOWN)
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