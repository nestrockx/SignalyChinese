package com.wegielek.signalychinese.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.interfaces.FlashCardsGroupsRecyclerViewListener
import com.wegielek.signalychinese.views.DefinitionWordActivity
import com.wegielek.signalychinese.views.FlashCardsActivity
import com.wegielek.signalychinese.views.SchoolActivity
import com.wegielek.signalychinese.views.SchoolWritingActivity

class FlashCardGroupsAdapter(private val context: Context, private val flashCardsGroupsRecyclerViewListener: FlashCardsGroupsRecyclerViewListener) : RecyclerView.Adapter<FlashCardGroupsAdapter.ViewHolder>() {
    private val dataList: MutableList<String>

    init {
        dataList = ArrayList()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataList: List<String>?) {
        this.dataList.clear()
        this.dataList.addAll(dataList!!)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_flash_group_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.groupNameTv.text = context.getString(R.string.collection) + dataList[position]
        holder.flashCardsBtn.setOnClickListener {
            flashCardsGroupsRecyclerViewListener.onFlashCardsGroupClicked(dataList[position])
        }
        holder.deleteBtn.setOnClickListener {
            flashCardsGroupsRecyclerViewListener.onDeleteFlashCardGroupClicked(dataList[position])
        }
        holder.writingBtn.setOnClickListener {
            flashCardsGroupsRecyclerViewListener.onWritingGroupClicked(dataList[position])
        }
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val groupNameTv: TextView
        val deleteBtn: AppCompatImageButton
        val writingBtn: AppCompatButton
        val flashCardsBtn: AppCompatButton

        init {
            groupNameTv = itemView.findViewById(R.id.groupName)
            deleteBtn = itemView.findViewById(R.id.deleteFlashCardGroup)
            writingBtn = itemView.findViewById(R.id.writingBtn)
            flashCardsBtn = itemView.findViewById(R.id.flashCardsBtn)
        }
    }
}
