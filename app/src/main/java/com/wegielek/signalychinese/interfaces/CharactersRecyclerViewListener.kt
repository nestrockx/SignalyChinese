package com.wegielek.signalychinese.interfaces

import android.view.View

interface CharactersRecyclerViewListener {
    fun onItemReleased(position: Int)
    fun onItemPressed(itemView: View)
    fun onItemCanceled(itemView: View)
}