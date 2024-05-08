package com.wegielek.signalychinese.views

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.databinding.ActivitySchoolWritingBinding
import com.wegielek.signalychinese.enums.CharacterMode
import com.wegielek.signalychinese.utils.Preferences
import com.wegielek.signalychinese.utils.Utils.Companion.dpToPixels
import com.wegielek.signalychinese.utils.Utils.Companion.getScreenHeight
import com.wegielek.signalychinese.utils.Utils.Companion.getScreenWidth
import com.wegielek.signalychinese.utils.Utils.Companion.isScreenRotated
import com.wegielek.signalychinese.utils.Utils.Companion.showWritingCharacterTypePopup
import com.wegielek.signalychinese.viewmodels.SchoolWritingViewModel

class SchoolWritingActivity : AppCompatActivity() {

    lateinit var binding: ActivitySchoolWritingBinding
    private var group: String? = null
    private var menu: Menu? = null

    private lateinit var schoolWritingViewModel: SchoolWritingViewModel
    private lateinit var schoolLearnStrokesCv: SchoolLearnStrokesCanvasView
    private lateinit var mCharArraySimplified: CharArray
    private lateinit var mCharArrayTraditional: CharArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySchoolWritingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        schoolWritingViewModel = ViewModelProvider(this)[SchoolWritingViewModel::class.java]

        val schoolWritingToolbar: Toolbar = binding.schoolWritingToolbar
        setSupportActionBar(schoolWritingToolbar)
        schoolWritingToolbar.setTitleTextColor(getColor(R.color.dark_mode_white))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = getString(R.string.writing)

        group = intent.getStringExtra("group")
        if (group != null) {
            if (schoolWritingViewModel.flashCardsList.value == null) {
                schoolWritingViewModel.getFlashCardGroup(group!!).observe(this) {
                    schoolWritingViewModel.setFlashCardsList(it)
                }
            }
        }

        if (Preferences.getWritingCharacterType() == "simplified") {
            binding.characterDropdownTv.text = getString(R.string.simplified)
        } else {
            binding.characterDropdownTv.text = getString(R.string.traditional)
        }

        schoolWritingViewModel.flashCardsList.observe(this) {

            schoolWritingViewModel.currentIndex.observe(this) {index ->
                mCharArraySimplified = schoolWritingViewModel.getFlashCardsList()?.get(index)?.simplifiedSign
                    ?.substring(1, schoolWritingViewModel.getFlashCardsList()?.get(index)?.simplifiedSign!!.length - 1)!!.toCharArray()
                mCharArrayTraditional = schoolWritingViewModel.getFlashCardsList()?.get(index)?.traditionalSign!!.toCharArray()
                if (Preferences.getWritingCharacterType() == "simplified") {
                    schoolLearnStrokesCv.setHanziCharacter(mCharArraySimplified[0], true)
                } else {
                    schoolLearnStrokesCv.setHanziCharacter(mCharArrayTraditional[0], true)
                }
                schoolWritingViewModel.setCharacterMode(CharacterMode.TEST)


                binding.writingTranslationTv.text =
                    schoolWritingViewModel.getFlashCardsList()?.get(index)?.translation
                        ?.replace('/', '\n', false)


                binding.hintText.visibility = View.GONE
                menu?.findItem(R.id.hint)?.setIcon(R.drawable.ic_hint_default)
                binding.characterCounterTv.text = 1.toString() + "/" + mCharArraySimplified.size.toString()
            }

            schoolWritingViewModel.charArrayIndex.observe(this) {
                if (Preferences.getWritingCharacterType() == "simplified") {
                    schoolLearnStrokesCv.setHanziCharacter(mCharArraySimplified[it], true)
                } else {
                    schoolLearnStrokesCv.setHanziCharacter(mCharArrayTraditional[it], true)
                }
                schoolWritingViewModel.setCharacterMode(CharacterMode.TEST)
                binding.characterCounterTv.text =
                    (it + 1).toString() + "/" + mCharArraySimplified.size.toString()
            }
        }

        schoolWritingViewModel.isContinue.observe(this) {
            binding.nextCharacterBtn.visibility = View.VISIBLE
        }

        schoolLearnStrokesCv = findViewById(R.id.schoolLearnStrokesCv)
        schoolLearnStrokesCv.setViewModel(schoolWritingViewModel)
        if (!isScreenRotated(applicationContext)) {
            schoolLearnStrokesCv.setDimensions(getScreenWidth(this)
                    - dpToPixels(applicationContext,32f))
        } else {
            schoolLearnStrokesCv.setDimensions(getScreenHeight(this)
                    - dpToPixels(applicationContext,128f))
        }

        binding.nextWordBtn.setOnClickListener {
            binding.nextCharacterBtn.visibility = View.INVISIBLE
            schoolWritingViewModel.increaseIndex()
            schoolWritingViewModel.charArrayIndex.value = 0
        }

        binding.prevWordBtn.setOnClickListener {
            binding.nextCharacterBtn.visibility = View.INVISIBLE
            schoolWritingViewModel.decreaseIndex()
            schoolWritingViewModel.charArrayIndex.value = 0
        }

        binding.nextCharacterBtn.setOnClickListener {
            binding.nextCharacterBtn.visibility = View.INVISIBLE
            if (!schoolWritingViewModel.increaseCharArrayIndex(mCharArraySimplified)) {
                schoolWritingViewModel.increaseIndex()
                schoolWritingViewModel.charArrayIndex.value = 0
            }
            menu?.findItem(R.id.hint)?.setIcon(R.drawable.ic_hint_default)
        }

        binding.characterDropdownTv.setOnClickListener {
            showWritingCharacterTypePopup(it) { type ->
                if (type == "simplified") {
                    binding.characterDropdownTv.setText(R.string.simplified)
                    Preferences.setWritingCharacterType("simplified")
                } else if (type == "traditional") {
                    binding.characterDropdownTv.setText(R.string.traditional)
                    Preferences.setWritingCharacterType("traditional")
                }

                schoolWritingViewModel.charArrayIndex.value = 0
                if (Preferences.getWritingCharacterType() == "simplified") {
                    schoolLearnStrokesCv.setHanziCharacter(mCharArraySimplified[0], true)
                } else {
                    schoolLearnStrokesCv.setHanziCharacter(mCharArrayTraditional[0], true)
                }
                schoolWritingViewModel.setCharacterMode(CharacterMode.TEST)
            }
        }
        binding.characterDropwdownIv.setOnClickListener {
            showWritingCharacterTypePopup(it) { type ->
                if (type == "simplified") {
                    binding.characterDropdownTv.setText(R.string.simplified)
                    Preferences.setWritingCharacterType("simplified")
                } else if (type == "traditional") {
                    binding.characterDropdownTv.setText(R.string.traditional)
                    Preferences.setWritingCharacterType("traditional")
                }

                schoolWritingViewModel.charArrayIndex.value = 0
                if (Preferences.getWritingCharacterType() == "simplified") {
                    schoolLearnStrokesCv.setHanziCharacter(mCharArraySimplified[0], true)
                } else {
                    schoolLearnStrokesCv.setHanziCharacter(mCharArrayTraditional[0], true)
                }
                schoolWritingViewModel.setCharacterMode(CharacterMode.TEST)
            }
        }

        binding.nextCharacterBtn.postDelayed(
            { binding.nextCharacterBtn.visibility = View.INVISIBLE },
            200
        )

        binding.resetCharacterBtn.setOnClickListener {
            schoolWritingViewModel.setCharacterMode(schoolWritingViewModel.getCharacterMode())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.school_writing_menu, menu)
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        } else if (item.itemId == R.id.hint) {
            item.setIcon(R.drawable.ic_hint_active)
            schoolWritingViewModel.setCharacterMode(CharacterMode.LEARN)
            if (Preferences.getWritingCharacterType() == "simplified") {
                binding.hintText.visibility = View.VISIBLE
                if (
                    (schoolWritingViewModel.getFlashCardsList()?.get(schoolWritingViewModel.getCurrentIndex())?.traditionalSign?.length ?: 0) > 1
                        &&
                    schoolWritingViewModel.charArrayIndex.value != schoolWritingViewModel.getFlashCardsList()?.get(schoolWritingViewModel.getCurrentIndex())?.traditionalSign?.length?.minus(1)
                    ) {
                    binding.hintText.text =
                        schoolWritingViewModel.getFlashCardsList()
                            ?.get(schoolWritingViewModel.getCurrentIndex())?.simplifiedSign?.substring(
                            1,
                            schoolWritingViewModel.getCharArrayIndex() + 2
                        ) + ".."
                } else {
                    binding.hintText.text =
                        schoolWritingViewModel.getFlashCardsList()
                            ?.get(schoolWritingViewModel.getCurrentIndex())?.simplifiedSign?.substring(
                                1,
                                schoolWritingViewModel.getCharArrayIndex() + 2
                            )
                }
            } else {
                binding.hintText.visibility = View.VISIBLE
                if (
                    (schoolWritingViewModel.getFlashCardsList()?.get(schoolWritingViewModel.getCurrentIndex())?.traditionalSign?.length ?: 1) > 1
                    &&
                    schoolWritingViewModel.charArrayIndex.value != schoolWritingViewModel.getFlashCardsList()?.get(schoolWritingViewModel.getCurrentIndex())?.traditionalSign?.length?.minus(1)
                ) {
                    binding.hintText.text =
                        schoolWritingViewModel.getFlashCardsList()
                            ?.get(schoolWritingViewModel.getCurrentIndex())?.traditionalSign?.substring(
                            0,
                            schoolWritingViewModel.getCharArrayIndex() + 1
                        ) + ".."
                } else {
                    binding.hintText.text =
                        schoolWritingViewModel.getFlashCardsList()
                            ?.get(schoolWritingViewModel.getCurrentIndex())?.traditionalSign?.substring(
                            0,
                            schoolWritingViewModel.getCharArrayIndex() + 1
                        )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}