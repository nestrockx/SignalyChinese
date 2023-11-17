package com.wegielek.signalychinese.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.adapters.HistoryAdapter
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.database.History
import com.wegielek.signalychinese.databinding.ActivityHistoryBinding
import com.wegielek.signalychinese.interfaces.HistoryRecyclerViewListener
import com.wegielek.signalychinese.viewmodels.HistoryViewModel

class HistoryActivity : AppCompatActivity(), HistoryRecyclerViewListener {

    private lateinit var mBinding: ActivityHistoryBinding
    private lateinit var mHistoryAdapter: HistoryAdapter
    private lateinit var mHistoryViewModel: HistoryViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mHistoryViewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        val historyToolbar: Toolbar = mBinding.historyToolbar
        setSupportActionBar(historyToolbar)
        historyToolbar.setTitleTextColor(getColor(R.color.dark_mode_white))
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.title = getString(R.string.history)
        }
        mBinding.historyRv.layoutManager = LinearLayoutManager(this)
        mHistoryAdapter = HistoryAdapter(this, this)
        mBinding.historyRv.adapter = mHistoryAdapter
        mHistoryViewModel.wholeHistory.observe(
            this
        ) { histories: List<History> ->
            mHistoryAdapter.setData(
                histories
            )
        }
    }

    override fun onHistoryClicked(dictionary: Dictionary) {
        val intent = Intent(baseContext, DefinitionWordActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra("word", dictionary)
        startActivity(intent)
    }

    override fun onLongHistoryClicked(timestamp: String) {
        Toast.makeText(this, timestamp, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        } else if (item.itemId == R.id.trash) {
            mHistoryViewModel.deleteWholeHistory().addListener(
                { Log.d(LOG_TAG, "History deleted successfully") },
                ContextCompat.getMainExecutor(this)
            )
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val LOG_TAG = "HistoryActivity"
    }
}