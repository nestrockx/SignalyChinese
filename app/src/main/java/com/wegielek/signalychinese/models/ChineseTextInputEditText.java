package com.wegielek.signalychinese.models;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.wegielek.signalychinese.interfaces.SearchTextBoxListener;


public class ChineseTextInputEditText extends TextInputEditText {

    private SearchTextBoxListener mListener;

    public ChineseTextInputEditText(@NonNull Context context) {
        super(context);
    }

    public ChineseTextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChineseTextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setOnSelectionChangedListener(SearchTextBoxListener searchTextBoxListener) {
        mListener = searchTextBoxListener;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {

        if(mListener != null) {
            mListener.onSelectionChanged(selStart, selEnd);
        }

        super.onSelectionChanged(selStart, selEnd);
    }


}
