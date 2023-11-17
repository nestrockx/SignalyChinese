package com.wegielek.signalychinese.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.interfaces.DefinitionListRecyclerViewListener

class DefinitionAdapter(
    private val context: Context,
    private val listener: DefinitionListRecyclerViewListener
) : RecyclerView.Adapter<DefinitionAdapter.ViewHolder>() {

    private val dataList: MutableList<String>

    init {
        dataList = ArrayList()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataList: List<String>) {
        this.dataList.clear()
        this.dataList.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_definition_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.definitionRowTv.text =
            context.getString(
                R.string.definition_list_item_placeholder,
                position + 1,
                dataList[position].trim { it <= ' ' })
        holder.moreBtn.setOnClickListener { v: View ->
            listener.showPopup(
                v, dataList[position]
            )
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val definitionRowTv: TextView
        val moreBtn: AppCompatImageButton

        init {
            moreBtn = itemView.findViewById(R.id.moreBtn)
            definitionRowTv = itemView.findViewById(R.id.definitionRowTv)
        }
    }
}
