package com.wegielek.signalychinese.interfaces

import android.view.View
import android.widget.TextView

interface DefinitionListRecyclerViewListener {
    fun showPopup(v: View, tv: TextView, text: String)
}
