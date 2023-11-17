package com.wegielek.signalychinese.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.interfaces.BottomSheetRecyclerViewListener
import com.wegielek.signalychinese.viewmodels.DefinitionViewModel

class BottomSheetsGroupsAdapter(
    private val context: Context,
    private val listener: BottomSheetRecyclerViewListener,
    private val groups: ArrayList<String>
) : RecyclerView.Adapter<BottomSheetsGroupsAdapter.ViewHolder>() {

    private val dataList: MutableList<String>

    init {
        dataList = ArrayList()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataList: List<String>) {
        if(groups.isEmpty()) {
            groups.add(dataList[0])
        }
        this.dataList.clear()
        this.dataList.addAll(dataList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(group: String) {
        this.dataList.add(group)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_bottom_sheet_collection, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.checkBox.text = dataList[position]
        holder.checkBox.isChecked = groups.contains(dataList[position])
        holder.checkBox.setOnCheckedChangeListener { button, checked ->
            if (!checked) {
                groups.remove(button.text.toString())
            } else {
                groups.add(button.text.toString())
            }
            listener.onCollectionCheckChanged(button, checked)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox

        init {
            checkBox = itemView.findViewById(R.id.collectionCb)
        }
    }
}