package com.wegielek.signalychinese.views

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.database.FlashCards
import com.wegielek.signalychinese.databinding.ActivityDefinitionWordBinding
import com.wegielek.signalychinese.utils.Preferences.Companion.getTtsAlertShow
import com.wegielek.signalychinese.utils.Preferences.Companion.setTtsAlertShow
import com.wegielek.signalychinese.utils.Utils.Companion.copyToClipboard
import com.wegielek.signalychinese.utils.Utils.Companion.showPopup
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel

class DefinitionWordActivity : AppCompatActivity() {
    private var added = false
    private lateinit var mBinding: ActivityDefinitionWordBinding
    lateinit var mDefinitionViewModel: DefinitionViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_definition_word)
        mDefinitionViewModel = ViewModelProvider(this)[DefinitionViewModel::class.java]
        mBinding.executePendingBindings()
        val dictionary: Dictionary? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras!!.getParcelable(
                "word",
                Dictionary::class.java
            )
        } else {
            intent.extras!!.getParcelable("word")
        }
        val definitionToolbar: Toolbar = mBinding.definitionToolbar
        setSupportActionBar(definitionToolbar)
        definitionToolbar.setTitleTextColor(getColor(R.color.dark_mode_white))

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        if (dictionary != null) {
            supportActionBar!!.setTitle(dictionary.simplifiedSign)
            mDefinitionViewModel.setWord(dictionary)
        }


        mBinding.speakBtn.setOnClickListener {
            /*
            Intent intent = new Intent();
            intent.setAction("com.android.settings.TTS_SETTINGS");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
             */
            if (!getTtsAlertShow(
                applicationContext,
                "tts_dialog_dont_show_again"
            )
        ) {
            val dialogTtsView: View = LayoutInflater.from(this@DefinitionWordActivity)
                .inflate(R.layout.alert_dialog_tts, null)
            val dialogTtsBuilder =
                AlertDialog.Builder(this@DefinitionWordActivity)
            dialogTtsBuilder.setView(dialogTtsView)
            val dialogTts = dialogTtsBuilder.create()
            if (dialogTts.window != null) {
                dialogTts.window!!
                    .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            } else {
                Log.e(
                    LOG_TAG,
                    "Dialog getWindow() is null"
                )
            }
            dialogTts.show()
            val dialogPronunciationCHImageButton =
                dialogTtsView.findViewById<AppCompatImageButton>(R.id.dialogPronunciationCHBtn)
            dialogPronunciationCHImageButton.setOnClickListener { v1: View? ->
                if (mDefinitionViewModel.word.value != null) {
                    speakCH(mDefinitionViewModel.word.value!!.simplifiedSign)
                } else {
                    Log.e(
                        LOG_TAG,
                        "Word definition is null in speakBtn.onClick"
                    )
                }
            }
            val dialogPronunciationPLImageButton =
                dialogTtsView.findViewById<AppCompatImageButton>(R.id.dialogPronunciationPLBtn)
            dialogPronunciationPLImageButton.setOnClickListener { v1: View? ->
                if (mDefinitionViewModel.word.value != null) {
                    speakPL(
                        mDefinitionViewModel.word.value!!.translation.split("/".toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0])
                } else {
                    Log.e(
                        LOG_TAG,
                        "Word definition is null in speakBtn.onClick"
                    )
                }
            }
            val dialogSettingsBtn =
                dialogTtsView.findViewById<AppCompatButton>(R.id.dialogSettingsBtn)
            dialogSettingsBtn.setOnClickListener { v12: View? ->
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
            }
            val dialogCloseBtn =
                dialogTtsView.findViewById<AppCompatImageButton>(R.id.dialogCloseBtn)
            dialogCloseBtn.setOnClickListener { v13: View? -> dialogTts.hide() }
            val dialogDontShowAgainBtn =
                dialogTtsView.findViewById<AppCompatButton>(R.id.dialogDontShowAgainBtn)
            dialogDontShowAgainBtn.setOnClickListener { v14: View? ->
                setTtsAlertShow(
                    applicationContext,
                    "tts_dialog_dont_show_again",
                    true
                )
                dialogTts.hide()
            }
        }
            if (mDefinitionViewModel.word.value != null) {
                speakCH(mDefinitionViewModel.word.value!!.simplifiedSign)
            } else {
                Log.e(
                    LOG_TAG,
                    "Word definition is null in speakBtn.onClick"
                )
            }
        }
        val dictionaryWordNavHostFragment =
            supportFragmentManager.findFragmentById(R.id.dictionaryWordNavHostFragment) as NavHostFragment?
        if (dictionaryWordNavHostFragment != null) {
            mBinding.bottomNavigationView.setOnItemSelectedListener { item ->
                onNavDestinationSelected(item, dictionaryWordNavHostFragment.navController)
                true
            }
        }
        if (mDefinitionViewModel.word.value != null) {
            mBinding.definitionCharactersTraditonalTv.text = mDefinitionViewModel.word.value!!.traditionalSign
            mBinding.definitionCharactersSimplifiedTv.text = "(" + mDefinitionViewModel.word.value!!.simplifiedSign + ")"
            mBinding.definitionPronunciationTv.text = mDefinitionViewModel.word.value!!.pronunciation
        } else {
            Log.e(LOG_TAG, "Word definition is null in onCreate")
        }
        mBinding.definitionCharactersTraditonalTv.setOnClickListener { v ->
            showPopup(
                v,
                mDefinitionViewModel.word.value!!.traditionalSign,
                "zh-TW",
                "en",
                mDefinitionViewModel.ttsCH
            )
        }
        mBinding.definitionCharactersSimplifiedTv.setOnClickListener { v ->
            showPopup(
                v,
                mDefinitionViewModel.word.value!!.simplifiedSign,
                "zh-CN",
                "en",
                mDefinitionViewModel.ttsCH
            )
        }
        mBinding.definitionPronunciationTv.setOnClickListener { v ->
            showPopup(
                v,
                mDefinitionViewModel.word.value!!.pronunciation.trim { it <= ' ' } + "/" + mDefinitionViewModel.word.value!!.pronunciationPhonetic.trim { it <= ' ' },
                "zh-CN",
                "en",
                mDefinitionViewModel.ttsCH
            )
        }
    }

    private fun speakCH(text: String) {
        mDefinitionViewModel.ttsCH.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun speakPL(text: String) {
        mDefinitionViewModel.ttsPL.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.definition_word_menu, menu)
        val future = mDefinitionViewModel.getFlashCard(
            mBinding.definitionCharactersTraditonalTv.text.toString(),
            mBinding.definitionCharactersSimplifiedTv.text.toString(),
            mBinding.definitionPronunciationTv.text.toString()
        )
        Futures.addCallback<Boolean>(future, object : FutureCallback<Boolean> {
            override fun onSuccess(result: Boolean) {
                added = if (result) {
                    menu.findItem(R.id.add).setIcon(R.drawable.ic_note_add_active)
                    true
                } else {
                    menu.findItem(R.id.add).setIcon(R.drawable.ic_note_add_default)
                    false
                }
            }

            override fun onFailure(t: Throwable) {
                t.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        } else if (item.itemId == R.id.copy) {
            if (copyToClipboard(
                    this,
                    mBinding.definitionCharactersSimplifiedTv.text.toString().substring(
                        1,
                        mBinding.definitionCharactersSimplifiedTv.text.toString().length - 1
                    )
                )
            ) {
                Toast.makeText(this, "Successfully copied", Toast.LENGTH_SHORT).show()
            }
        } else if (item.itemId == R.id.add) {
            if (!added) {
                item.setIcon(R.drawable.ic_note_add_active)
                val flashCards = FlashCards()
                flashCards.group = "saved"
                flashCards.traditionalSign =
                    mBinding.definitionCharactersTraditonalTv.text.toString()
                flashCards.simplifiedSign =
                    mBinding.definitionCharactersSimplifiedTv.text.toString()
                flashCards.pronunciation = mBinding.definitionPronunciationTv.text.toString()
                flashCards.pronunciationPhonetic =
                    mDefinitionViewModel.word.value!!.pronunciationPhonetic
                flashCards.translation = mDefinitionViewModel.word.value!!.translation
                mDefinitionViewModel.addFlashCardToSaved(flashCards)
                Toast.makeText(this, "Added to flash cards", Toast.LENGTH_SHORT).show()
                added = true
            } else {
                item.setIcon(R.drawable.ic_note_add_default)
                mDefinitionViewModel.deleteFlashCard(
                    mBinding.definitionCharactersTraditonalTv.text.toString(),
                    mBinding.definitionCharactersSimplifiedTv.text.toString(),
                    mBinding.definitionPronunciationTv.text.toString()
                )
                Toast.makeText(this, "Removed from flash cards", Toast.LENGTH_SHORT).show()
                added = false
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val LOG_TAG = "DefinitionWordActivity"
    }
}