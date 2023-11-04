package com.wegielek.signalychinese.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.database.Dictionary
import com.wegielek.signalychinese.interfaces.ResultsRecyclerViewListener
import java.util.Arrays

class SearchResultListAdapter(
    private val resultsRecyclerViewListener: ResultsRecyclerViewListener,
    private val context: Context
) :
    RecyclerView.Adapter<SearchResultListAdapter.ViewHolder>() {
    private val dataList: MutableList<Dictionary>

    init {
        dataList = ArrayList()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataList: List<Dictionary>?) {
        this.dataList.clear()
        this.dataList.addAll(dataList!!)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_search_result_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dictionary = dataList[position]
        val list = Arrays.asList(
            *dictionary.translation.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
        val translation = StringBuilder()
        for (i in list.indices) {
            if (i == 0) {
                translation.append(i + 1).append(".\u00A0").append(list[i].trim { it <= ' ' })
                    .append(" ")
            } else if (i != list.size - 1) {
                translation.append(i + 1).append(".\u00A0").append(list[i].trim { it <= ' ' })
                    .append(" ")
            } else {
                translation.append(i + 1).append(".\u00A0").append(list[i].trim { it <= ' ' })
                    .append("")
            }
        }
        if (dictionary.traditionalSign == dictionary.simplifiedSign) {
            holder.charactersTv.text = dictionary.simplifiedSign
        } else {
            holder.charactersTv.text = context.getString(
                R.string.result_text_placeholder_1,
                dictionary.traditionalSign,
                dictionary.simplifiedSign
            )
        }
        holder.pronunciationTv.text = dictionary.pronunciation
        holder.translationTv.text = translation
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val charactersTv: TextView
        val pronunciationTv: TextView
        val translationTv: TextView

        init {
            charactersTv = itemView.findViewById(R.id.labelTv)
            pronunciationTv = itemView.findViewById(R.id.pronunciationTv)
            translationTv = itemView.findViewById(R.id.translationTv)
            itemView.setOnClickListener { view: View? ->
                resultsRecyclerViewListener.onResultClicked(
                    adapterPosition
                )
            }
        }
    }
}
