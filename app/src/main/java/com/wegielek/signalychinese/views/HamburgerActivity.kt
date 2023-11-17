package com.wegielek.signalychinese.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.adapters.HamburgerMenuAdapter
import com.wegielek.signalychinese.databinding.ActivityHamburgerBinding
import java.util.ArrayList

class HamburgerActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityHamburgerBinding
    lateinit var mHamburgerMenuAdapter: HamburgerMenuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityHamburgerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val definitionToolbar = findViewById<Toolbar>(R.id.definitionToolbar)
        setSupportActionBar(definitionToolbar)
        definitionToolbar.setTitleTextColor(getColor(R.color.dark_mode_white))
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.title = getString(R.string.menu)
        } else {
            Log.e(LOG_TAG, "Support action bar is null in onCreate")
        }


        val menuArray = ArrayList<String>()
        menuArray.add("Tryb wyszukiwania")
        menuArray.add("Język wyszukiwania głosem")
        menuArray.add("Historia wyszukiwania")
        menuArray.add("Nauka")
        menuArray.add("O aplikacji")

        mBinding.menuItemsRv.layoutManager = LinearLayoutManager(applicationContext)
        mHamburgerMenuAdapter = HamburgerMenuAdapter(menuArray)
        mBinding.menuItemsRv.adapter = mHamburgerMenuAdapter
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        } else {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val LOG_TAG = "SettingsActivity"
    }
}