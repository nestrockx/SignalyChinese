package com.wegielek.signalychinese.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.common.util.concurrent.ListenableFuture
import com.wegielek.signalychinese.database.History
import com.wegielek.signalychinese.repository.DictionaryRepository

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val mDictionaryRepository: DictionaryRepository = DictionaryRepository(application)

    val wholeHistory: LiveData<List<History>>
        get() = mDictionaryRepository.wholeHistory

    fun deleteWholeHistory(): ListenableFuture<Void?> {
        return mDictionaryRepository.deleteWholeHistory()
    }
}
