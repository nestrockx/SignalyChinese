package com.wegielek.signalychinese.views;

import static com.wegielek.signalychinese.utils.Utils.dpToPixels;
import static com.wegielek.signalychinese.utils.Utils.getScreenHeight;
import static com.wegielek.signalychinese.utils.Utils.getScreenWidth;
import static com.wegielek.signalychinese.utils.Utils.isScreenRotated;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.wegielek.signalychinese.R;
import com.wegielek.signalychinese.databinding.ActivityFlashCardsBinding;

public class FlashCardsActivity extends AppCompatActivity {

    private ActivityFlashCardsBinding mBinding;

    private AnimatorSet frontAnim;
    private AnimatorSet backAnim;
    boolean isFront = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_flash_cards);

        Toolbar flashCardsToolbar = mBinding.flashCardsToolbar;
        setSupportActionBar(flashCardsToolbar);
        flashCardsToolbar.setTitleTextColor(getColor(R.color.dark_mode_white));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Flash cards");
        }

        if (!isScreenRotated(this)) {
            mBinding.cardFront.setWidth(getScreenWidth(this) - dpToPixels(this, 64));
            mBinding.cardFront.setHeight(getScreenWidth(this) - dpToPixels(this, 64));
            mBinding.cardBack.setWidth(getScreenWidth(this) - dpToPixels(this, 64));
            mBinding.cardBack.setHeight(getScreenWidth(this) - dpToPixels(this, 64));
        } else {
            mBinding.cardFront.setWidth(getScreenHeight(this) - dpToPixels(this, 128));
            mBinding.cardFront.setHeight(getScreenHeight(this) - dpToPixels(this, 128));
            mBinding.cardBack.setWidth(getScreenHeight(this) - dpToPixels(this, 128));
            mBinding.cardBack.setHeight(getScreenHeight(this) - dpToPixels(this, 128));
        }

        float scale = getResources().getDisplayMetrics().density;
        mBinding.cardFront.setCameraDistance(8000 * scale);
        mBinding.cardBack.setCameraDistance(8000 * scale);

        frontAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.front_animator);
        backAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.back_animator);

        mBinding.cardFront.setOnClickListener(v -> {
            if (!frontAnim.isRunning() && !backAnim.isRunning()) {
                if (isFront) {
                    frontAnim.setTarget(mBinding.cardFront);
                    backAnim.setTarget(mBinding.cardBack);
                    frontAnim.start();
                    backAnim.start();
                    isFront = false;
                } else {
                    backAnim.setTarget(mBinding.cardFront);
                    frontAnim.setTarget(mBinding.cardBack);
                    backAnim.start();
                    frontAnim.start();
                    isFront = true;
                }
                mBinding.button3.setVisibility(View.INVISIBLE);
                mBinding.button4.setVisibility(View.INVISIBLE);
                mBinding.button3.postDelayed(() ->
                        mBinding.button3.setVisibility(View.VISIBLE), 500);
                mBinding.button4.postDelayed(() ->
                        mBinding.button4.setVisibility(View.VISIBLE), 500);
            }
        });

        mBinding.cardBack.setOnClickListener(v -> {
            if (!frontAnim.isRunning() && !backAnim.isRunning()) {
                if (isFront) {
                    frontAnim.setTarget(mBinding.cardFront);
                    backAnim.setTarget(mBinding.cardBack);
                    frontAnim.start();
                    backAnim.start();
                    isFront = false;
                } else {
                    backAnim.setTarget(mBinding.cardFront);
                    frontAnim.setTarget(mBinding.cardBack);
                    backAnim.start();
                    frontAnim.start();
                    isFront = true;
                }
                mBinding.button3.setVisibility(View.INVISIBLE);
                mBinding.button4.setVisibility(View.INVISIBLE);
                mBinding.button3.postDelayed(() ->
                        mBinding.button3.setVisibility(View.VISIBLE), 500);
                mBinding.button4.postDelayed(() ->
                        mBinding.button4.setVisibility(View.VISIBLE), 500);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}