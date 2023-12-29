package com.wegielek.signalychinese.views

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.adapters.FlashCardGroupsAdapter
import com.wegielek.signalychinese.databinding.ActivitySchoolBinding
import com.wegielek.signalychinese.interfaces.FlashCardsGroupsRecyclerViewListener
import com.wegielek.signalychinese.viewmodels.SchoolViewModel

class SchoolActivity : AppCompatActivity(), FlashCardsGroupsRecyclerViewListener {

    private lateinit var binding: ActivitySchoolBinding
    private lateinit var mFlashCardGroupsAdapter: FlashCardGroupsAdapter
    private lateinit var mSchoolViewModel: SchoolViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySchoolBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val definitionToolbar: Toolbar = binding.schoolToolbar
        setSupportActionBar(definitionToolbar)
        definitionToolbar.setTitleTextColor(getColor(R.color.dark_mode_white))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = getString(R.string.school)

        mSchoolViewModel = ViewModelProvider(this)[SchoolViewModel::class.java]

        binding.flashCardsGroupsRv.layoutManager = LinearLayoutManager(applicationContext)
        mFlashCardGroupsAdapter = FlashCardGroupsAdapter(this, this)
        binding.flashCardsGroupsRv.adapter = mFlashCardGroupsAdapter

        mSchoolViewModel.getFlashCardsGroups().observe(this) {
            mFlashCardGroupsAdapter.setData(it)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onFlashCardsGroupClicked(group: String) {
        val intent = Intent(baseContext, FlashCardsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra("group", group)
        startActivity(intent)
    }

    override fun onWritingGroupClicked(group: String) {
        val intent = Intent(baseContext, SchoolWritingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra("group", group)
        startActivity(intent)
    }

    override fun onDeleteFlashCardGroupClicked(group: String) {
        val deleteDialog = Dialog(this)
        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        deleteDialog.setContentView(R.layout.dialog_are_you_sure_delete_flash_card)

        val textView: TextView = deleteDialog.findViewById(R.id.areYouSureDialogTv)
        textView.text = getString(R.string.are_you_sure_delete_flash_cards, group)

        deleteDialog.show()
        deleteDialog.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
        deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val noBtn: AppCompatButton = deleteDialog.findViewById(R.id.noBtn)
        noBtn.setOnClickListener {
            deleteDialog.dismiss()
        }

        val yesBtn: AppCompatButton = deleteDialog.findViewById(R.id.yesBtn)
        yesBtn.setOnClickListener {
            mSchoolViewModel.deleteFlashCardGroup(group)
            deleteDialog.dismiss()
        }
    }

}