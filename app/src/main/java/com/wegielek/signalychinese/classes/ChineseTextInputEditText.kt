package com.wegielek.signalychinese.classes

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.wegielek.signalychinese.interfaces.SearchTextBoxListener

class ChineseTextInputEditText : AppCompatEditText {
    private var mListener: SearchTextBoxListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setOnSelectionChangedListener(searchTextBoxListener: SearchTextBoxListener) {
        mListener = searchTextBoxListener
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        mListener?.onSelectionChanged(selStart, selEnd)
        super.onSelectionChanged(selStart, selEnd)
    }
}