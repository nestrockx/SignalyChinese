package com.wegielek.signalychinese.interfaces

interface FlashCardsGroupsRecyclerViewListener {
    fun onFlashCardsGroupClicked(group: String)
    fun onWritingGroupClicked(group: String)
    fun onDeleteFlashCardGroupClicked(group: String)
}