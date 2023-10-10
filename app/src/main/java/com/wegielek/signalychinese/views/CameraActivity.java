package com.wegielek.signalychinese.views;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.wegielek.signalychinese.R;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}