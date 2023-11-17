package com.wegielek.signalychinese.interfaces

import android.widget.CompoundButton

interface BottomSheetRecyclerViewListener {
    fun onCollectionCheckChanged(button: CompoundButton, checked: Boolean)
}