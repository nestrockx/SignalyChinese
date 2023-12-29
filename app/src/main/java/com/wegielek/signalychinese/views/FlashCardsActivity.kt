package com.wegielek.signalychinese.views

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.database.FlashCards
import com.wegielek.signalychinese.databinding.ActivityFlashCardsBinding
import com.wegielek.signalychinese.enums.Direction
import com.wegielek.signalychinese.utils.Preferences
import com.wegielek.signalychinese.viewmodels.FlashCardsViewModel

class FlashCardsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFlashCardsBinding
    lateinit var flashCardsViewModel: FlashCardsViewModel
    private var group: String? = null

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
        binding = ActivityFlashCardsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val flashCardsToolbar: Toolbar = binding.flashCardsToolbar
        setSupportActionBar(flashCardsToolbar)
        flashCardsToolbar.setTitleTextColor(getColor(R.color.dark_mode_white))
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.title = getString(R.string.flash_cards)
        }

        group = intent.getStringExtra("group")
        flashCardsViewModel = ViewModelProvider(this)[FlashCardsViewModel::class.java]
        if (group != null) {
            if (flashCardsViewModel.flashCardsList.value == null) {
                flashCardsViewModel.getFlashCardGroup(group!!).observe(this) {
                    flashCardsViewModel.setFlashCardsList(it)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.flash_cards_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        } else if (item.itemId == R.id.reverse) {
            if (!Preferences.isFlashCardsReversed()) {
                if (group != null) {
                    flashCardsViewModel.getFlashCardGroup(group!!).observe(this) {
                        flashCardsViewModel.setFlashCardsList(it)
                    }
                }
                Preferences.setFlashCardsReversed(true)
            } else {
                if (group != null) {
                    flashCardsViewModel.getFlashCardGroup(group!!).observe(this) {
                        flashCardsViewModel.setFlashCardsList(it)
                    }
                }
                Preferences.setFlashCardsReversed(false)
            }
        } else if (item.itemId == R.id.trash) {
            val flashCards: FlashCards? = flashCardsViewModel.flashCardsList.value?.get(flashCardsViewModel.getCurrentIndex())
            if (flashCards != null) {
                if (flashCardsViewModel.getCurrentIndex() == flashCardsViewModel.flashCardsList.value!!.size - 1) {
                    flashCardsViewModel.decreaseIndex()
                }
                flashCardsViewModel.deleteFlashCard(flashCards.traditionalSign, flashCards.simplifiedSign, flashCards.pronunciation)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}