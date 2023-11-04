package com.wegielek.signalychinese.interfaces

import com.wegielek.signalychinese.database.Dictionary

interface HistoryRecyclerViewListener {
    fun onHistoryClicked(dictionary: Dictionary)
    fun onLongHistoryClicked(timestamp: String)
}