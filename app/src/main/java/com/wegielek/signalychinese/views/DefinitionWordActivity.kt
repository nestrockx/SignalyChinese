package com.wegielek.signalychinese.views

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.adapters.BottomSheetsGroupsAdapter
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.database.FlashCards
import com.wegielek.signalychinese.databinding.ActivityDefinitionWordBinding
import com.wegielek.signalychinese.interfaces.BottomSheetRecyclerViewListener
import com.wegielek.signalychinese.utils.Preferences.Companion.getTtsAlertShow
import com.wegielek.signalychinese.utils.Preferences.Companion.setTtsAlertShow
import com.wegielek.signalychinese.utils.TextToSpeechManager
import com.wegielek.signalychinese.utils.Utils
import com.wegielek.signalychinese.utils.Utils.Companion.copyToClipboard
import com.wegielek.signalychinese.utils.Utils.Companion.isScreenRotated
import com.wegielek.signalychinese.utils.Utils.Companion.showPopup
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel

class DefinitionWordActivity : AppCompatActivity(), BottomSheetRecyclerViewListener {
    lateinit var binding: ActivityDefinitionWordBinding
    lateinit var definitionViewModel: DefinitionViewModel
    private lateinit var mMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDefinitionWordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        definitionViewModel = ViewModelProvider(this)[DefinitionViewModel::class.java]
        val dictionary: Dictionary? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras!!.getParcelable(
                "word",
                Dictionary::class.java
            )
        } else {
            intent.extras!!.getParcelable("word")
        }
        val definitionToolbar: Toolbar = binding.definitionToolbar
        setSupportActionBar(definitionToolbar)
        definitionToolbar.setTitleTextColor(getColor(R.color.dark_mode_white))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        if (dictionary != null) {
            supportActionBar!!.title = dictionary.simplifiedSign
            definitionViewModel.setWord(dictionary)
        }

        binding.speakBtn.setOnClickListener {
            /*
            Intent intent = new Intent();
            intent.setAction("com.android.settings.TTS_SETTINGS");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
             */
            if (!getTtsAlertShow(
                applicationContext,
                "tts_dialog_do_not_show_again"
            )
        ) {
            val dialogTtsView: View = LayoutInflater.from(this@DefinitionWordActivity)
                .inflate(R.layout.dialog_tts_setup, null)
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
            dialogPronunciationCHImageButton.setOnClickListener {
                if (definitionViewModel.word.value != null) {
                    speakCH(definitionViewModel.word.value!!.simplifiedSign)
                } else {
                    Log.e(
                        LOG_TAG,
                        "Word definition is null in speakBtn.onClick"
                    )
                }
            }
            val dialogPronunciationPLImageButton =
                dialogTtsView.findViewById<AppCompatImageButton>(R.id.dialogPronunciationPLBtn)
            dialogPronunciationPLImageButton.setOnClickListener {
                if (definitionViewModel.word.value != null) {
                    speakPL(
                        definitionViewModel.word.value!!.translation.split("/".toRegex())
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
            dialogSettingsBtn.setOnClickListener {
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
            }
            val dialogCloseBtn =
                dialogTtsView.findViewById<AppCompatImageButton>(R.id.dialogCloseBtn)
            dialogCloseBtn.setOnClickListener { dialogTts.hide() }
            val dialogDoNotShowAgainBtn =
                dialogTtsView.findViewById<AppCompatButton>(R.id.dialogDontShowAgainBtn)
            dialogDoNotShowAgainBtn.setOnClickListener {
                setTtsAlertShow(
                    applicationContext,
                    "tts_dialog_do_not_show_again",
                    true
                )
                dialogTts.hide()
            }
        }
            if (definitionViewModel.word.value != null) {
                speakCH(definitionViewModel.word.value!!.simplifiedSign)
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
            binding.bottomNavigationView.setOnItemSelectedListener { item ->
                if (!isScreenRotated(applicationContext)) {
                    if (item.itemId == R.id.strokesFragment) {
                        adjustToCanvas()
                    } else {
                        defaultView()
                    }
                }
                onNavDestinationSelected(item, dictionaryWordNavHostFragment.navController)
                true
            }
        }
        if (definitionViewModel.word.value != null) {
            binding.definitionCharactersTraditonalTv.text = definitionViewModel.word.value!!.traditionalSign
            binding.definitionCharactersSimplifiedTv.text = getString(R.string.simplified_placeholder, definitionViewModel.word.value!!.simplifiedSign)
            binding.definitionPronunciationTv.text = definitionViewModel.word.value!!.pronunciation
        } else {
            Log.e(LOG_TAG, "Word definition is null in onCreate")
        }
        binding.definitionCharactersTraditonalTv.setOnClickListener { v ->
            showPopup(
                v,
                definitionViewModel.word.value!!.traditionalSign,
                "zh-TW",
                "en",
                TextToSpeechManager.instanceCH
            )
        }
        binding.definitionCharactersSimplifiedTv.setOnClickListener { v ->
            showPopup(
                v,
                definitionViewModel.word.value!!.simplifiedSign,
                "zh-CN",
                "en",
                TextToSpeechManager.instanceCH
            )
        }
        binding.definitionPronunciationTv.setOnClickListener { v ->
            showPopup(
                v,
                definitionViewModel.word.value!!.pronunciation.trim { it <= ' ' } + "/" + definitionViewModel.word.value!!.pronunciationPhonetic.trim { it <= ' ' },
                "zh-CN",
                "en",
                TextToSpeechManager.instanceCH
            )
        }

        if (!isScreenRotated(this)) {
            if (dictionaryWordNavHostFragment!!.navController.currentDestination!!.label!! == "fragment_strokes") {
                adjustToCanvasImmed()
            }
        }
    }

    private fun adjustToCanvasImmed() {
        definitionViewModel.isAdjusted = true
        val targetHeight = Utils.getScreenHeight(this@DefinitionWordActivity) -
                (Utils.getScreenWidth(this@DefinitionWordActivity) +
                        Utils.dpToPixels(applicationContext, 250f))

        binding.scroll?.updateLayoutParams {
            height = targetHeight
        }
    }

    private fun adjustToCanvas() {
        if (!definitionViewModel.isAdjusted) {
            definitionViewModel.isAdjusted = true
            val initialHeight = binding.scroll!!.height
            if (!definitionViewModel.isSetup) {
                definitionViewModel.wrapContentHeight = initialHeight
                definitionViewModel.isSetup = true
            }
            val targetHeight = Utils.getScreenHeight(this@DefinitionWordActivity) -
                    (Utils.getScreenWidth(this@DefinitionWordActivity) +
                            Utils.dpToPixels(applicationContext, 250f))

            val animator = ValueAnimator.ofInt(initialHeight, targetHeight)
            animator.duration = 300 // Set the duration of the animation in milliseconds
            //animator.interpolator = AccelerateDecelerateInterpolator()

            animator.addUpdateListener { valueAnimator ->
                val animatedValue = valueAnimator.animatedValue as Int
                binding.scroll?.updateLayoutParams {
                    height = animatedValue
                }
            }
            animator.start()
        }
    }

    private fun defaultView() {
        if (definitionViewModel.isAdjusted) {
            definitionViewModel.isAdjusted = false
            val initialHeight = Utils.getScreenHeight(this@DefinitionWordActivity) -
                    (Utils.getScreenWidth(this@DefinitionWordActivity) +
                            Utils.dpToPixels(applicationContext, 250f))
            val targetHeight = definitionViewModel.wrapContentHeight

            val animator = ValueAnimator.ofInt(initialHeight, targetHeight)
            animator.duration = 300 // Set the duration of the animation in milliseconds
            //animator.interpolator = AccelerateDecelerateInterpolator()

            animator.addUpdateListener { valueAnimator ->
                val animatedValue = valueAnimator.animatedValue as Int
                binding.scroll?.updateLayoutParams {
                    height = animatedValue
                }
            }
            animator.start()
        }
    }

    private fun speakCH(text: String) {
        TextToSpeechManager.speakCH(text)
    }

    private fun speakPL(text: String) {
        TextToSpeechManager.speakPL(text)
    }

    private lateinit var mBottomSheetGroupsAdapter: BottomSheetsGroupsAdapter

    private fun showBottomSheet() {
        val bottomSheet = Dialog(this)
        bottomSheet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bottomSheet.setContentView(R.layout.dialog_bottom_sheet)

        bottomSheet.show()
        bottomSheet.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
        bottomSheet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bottomSheet.window?.setWindowAnimations(R.style.BottomSheetAnimation)
        bottomSheet.window?.setGravity(Gravity.BOTTOM)

        val bottomSheetGroupsRv: RecyclerView = bottomSheet.findViewById(R.id.bottomSheetGroupsRv)

        bottomSheetGroupsRv.layoutManager = LinearLayoutManager(applicationContext)
        val future = definitionViewModel.getFlashCardGroups(
            binding.definitionCharactersTraditonalTv.text.toString(),
            binding.definitionCharactersSimplifiedTv.text.toString(),
            binding.definitionPronunciationTv.text.toString()
        )
        Futures.addCallback<List<String>>(future, object : FutureCallback<List<String>> {
            override fun onSuccess(result: List<String>?) {
                mBottomSheetGroupsAdapter = BottomSheetsGroupsAdapter(applicationContext, this@DefinitionWordActivity, result as ArrayList<String>)
                bottomSheetGroupsRv.adapter = mBottomSheetGroupsAdapter
                val future1 = definitionViewModel.getFlashCardsGroupsNonObserve()
                Futures.addCallback(future1, object : FutureCallback<List<String>> {
                    override fun onSuccess(result1: List<String>?) {
                        if (result.isEmpty() && result1 != null) {
                            if (result1.isNotEmpty()) {
                                val flashCards = FlashCards()
                                flashCards.group = result1[0]
                                flashCards.traditionalSign =
                                    binding.definitionCharactersTraditonalTv.text.toString()
                                flashCards.simplifiedSign =
                                    binding.definitionCharactersSimplifiedTv.text.toString()
                                flashCards.pronunciation =
                                    binding.definitionPronunciationTv.text.toString()
                                flashCards.pronunciationPhonetic =
                                    definitionViewModel.word.value!!.pronunciationPhonetic
                                flashCards.translation =
                                    definitionViewModel.word.value!!.translation
                                definitionViewModel.addFlashCardToGroup(flashCards)
                            }
                        }
                        if (result1 != null) {
                            if (result1.isNotEmpty()) {
                                mBottomSheetGroupsAdapter.setData(result1)
                            }
                        }
                    }

                    override fun onFailure(t: Throwable) {
                        t.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(applicationContext))
            }

            override fun onFailure(t: Throwable) {
                t.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))


        bottomSheet.setOnDismissListener {
            val future2 = definitionViewModel.isFlashCardExists(
                binding.definitionCharactersTraditonalTv.text.toString(),
                binding.definitionCharactersSimplifiedTv.text.toString(),
                binding.definitionPronunciationTv.text.toString()
            )
            Futures.addCallback<Boolean>(future2, object : FutureCallback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    if (result) {
                        mMenu.findItem(R.id.add).setIcon(R.drawable.ic_note_add_active)
                        true
                    } else {
                        mMenu.findItem(R.id.add).setIcon(R.drawable.ic_note_add_default)
                        false
                    }
                }

                override fun onFailure(t: Throwable) {
                    t.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(this))
        }

        val newCollectionInputLayout: TextInputLayout = bottomSheet.findViewById(R.id.newCollectionInputLayout)
        val newCollectionEditText: TextInputEditText = bottomSheet.findViewById(R.id.newCollectionEditText)
        val newCollectionDoneBtn: AppCompatImageButton = bottomSheet.findViewById(R.id.newCollectionDoneBtn)
        val addCollectionBtn: AppCompatImageButton = bottomSheet.findViewById(R.id.addCollectionBtn)
        addCollectionBtn.setOnClickListener {
            newCollectionInputLayout.visibility = View.VISIBLE
            newCollectionDoneBtn.visibility = View.VISIBLE

            newCollectionEditText.setText(R.string.new_collection)
            newCollectionDoneBtn.setOnClickListener {
                newCollectionInputLayout.visibility = View.GONE
                newCollectionDoneBtn.visibility = View.GONE
                if (newCollectionEditText.text!!.length > 2) {
                    mBottomSheetGroupsAdapter.addData(newCollectionEditText.text.toString())
                }
            }
            //mBottomSheetGroupsAdapter.addData("new_one")
        }

        val collectionConfirmBtn: AppCompatButton = bottomSheet.findViewById(R.id.collectionConfirmBtn)
        collectionConfirmBtn.setOnClickListener {
            bottomSheet.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.definition_word_menu, menu)

        mMenu = menu

        val future = definitionViewModel.isFlashCardExists(
            binding.definitionCharactersTraditonalTv.text.toString(),
            binding.definitionCharactersSimplifiedTv.text.toString(),
            binding.definitionPronunciationTv.text.toString()
        )
        Futures.addCallback<Boolean>(future, object : FutureCallback<Boolean> {
            override fun onSuccess(result: Boolean) {
                if (result) {
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
                    binding.definitionCharactersSimplifiedTv.text.toString().substring(
                        1,
                        binding.definitionCharactersSimplifiedTv.text.toString().length - 1
                    )
                )
            ) {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    item.setIcon(R.drawable.ic_done_default)
                }, 100)
                handler.postDelayed({
                    item.setIcon(R.drawable.ic_copy_default)
                }, 1500)
            }
        } else if (item.itemId == R.id.add) {
            showBottomSheet()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val LOG_TAG = "DefinitionWordActivity"
    }

    override fun onCollectionCheckChanged(button: CompoundButton, checked: Boolean) {
        if (checked) {
            val flashCards = FlashCards()
            flashCards.group = button.text.toString()
            flashCards.traditionalSign =
                binding.definitionCharactersTraditonalTv.text.toString()
            flashCards.simplifiedSign =
                binding.definitionCharactersSimplifiedTv.text.toString()
            flashCards.pronunciation = binding.definitionPronunciationTv.text.toString()
            flashCards.pronunciationPhonetic =
                definitionViewModel.word.value!!.pronunciationPhonetic
            flashCards.translation = definitionViewModel.word.value!!.translation
            definitionViewModel.addFlashCardToGroup(flashCards)
        } else {
            definitionViewModel.deleteFlashCardFromGroup(
                button.text.toString(),
                binding.definitionCharactersTraditonalTv.text.toString(),
                binding.definitionCharactersSimplifiedTv.text.toString(),
                binding.definitionPronunciationTv.text.toString()
            )
        }
    }
}