package com.wegielek.signalychinese.views

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

    private lateinit var binding: ActivityHamburgerBinding
    private lateinit var mHamburgerMenuAdapter: HamburgerMenuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHamburgerBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        val menuArray = ArrayList(resources.getStringArray(R.array.menu_array).asList())
        binding.menuItemsRv.layoutManager = LinearLayoutManager(applicationContext)
        mHamburgerMenuAdapter = HamburgerMenuAdapter(menuArray)
        binding.menuItemsRv.adapter = mHamburgerMenuAdapter
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left, R.color.dark_mode_black)
        } else {
            @Suppress("DEPRECATION")
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