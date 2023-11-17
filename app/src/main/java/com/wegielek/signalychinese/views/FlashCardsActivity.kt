package com.wegielek.signalychinese.views

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.databinding.ActivityFlashCardsBinding
import com.wegielek.signalychinese.enums.Direction
import com.wegielek.signalychinese.viewmodels.FlashCardsViewModel

class FlashCardsActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityFlashCardsBinding
    lateinit var flashCardsViewModel: FlashCardsViewModel

    fun switchFragment(direction: Direction) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerFlashCards) as NavHostFragment
        val navController = navHostFragment.navController
        val navBuilder = NavOptions.Builder()
        if (direction == Direction.RIGHT) {
            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right)
        } else if (direction == Direction.LEFT) {
            navBuilder.setEnterAnim(R.anim.slide_in_left).setExitAnim(R.anim.slide_out_right)
                .setPopEnterAnim(R.anim.slide_in_right).setPopExitAnim(R.anim.slide_out_left)
        }
        navController.navigate(R.id.flashCardFragment, null, navBuilder.build())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityFlashCardsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val flashCardsToolbar: Toolbar = mBinding.flashCardsToolbar
        setSupportActionBar(flashCardsToolbar)
        flashCardsToolbar.setTitleTextColor(getColor(R.color.dark_mode_white))
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.title = getString(R.string.flash_cards)
        }

        val group = intent.getStringExtra("group")
        flashCardsViewModel = ViewModelProvider(this)[FlashCardsViewModel::class.java]
        if (group != null) {
            if (flashCardsViewModel.flashCardsList.value == null) {
                flashCardsViewModel.getFlashCardGroup(group).observe(this) {
                    flashCardsViewModel.setFlashCardsList(it)
                    Toast.makeText(this, it.size.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}