package com.wegielek.signalychinese.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.database.Sentences

class SentencesAdapter (
    private val context: Context
) : RecyclerView.Adapter<SentencesAdapter.ViewHolder>() {

    private val dataList: MutableList<Sentences>

    init {
        dataList = ArrayList()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataList: List<Sentences>) {
        this.dataList.clear()
        this.dataList.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_sentences_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.simplified.text = dataList[position].simplifiedSign
        holder.translation.text = dataList[position].translation
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {

        val simplified: TextView
        //val traditional: TextView
        val translation: TextView

        init {
            simplified = itemView.findViewById(R.id.chinese_sent)
            translation = itemView.findViewById(R.id.translation_sent)
        }
    }
}