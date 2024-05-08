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

class SearchResultsAdapter(
    private val resultsRecyclerViewListener: ResultsRecyclerViewListener,
    private val context: Context
) :
    RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {
    private val dataList: MutableList<Dictionary> = ArrayList()

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
        val list = listOf(
            *dictionary.translation.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
        val translation = StringBuilder()
        for (i in list.indices) {
            if (i == 0) {
                translation.append(1).append(".\u00A0").append(list[i].trim { it <= ' ' })
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
        val charactersTv: TextView = itemView.findViewById(R.id.labelTv)
        val pronunciationTv: TextView = itemView.findViewById(R.id.pronunciationTv)
        val translationTv: TextView = itemView.findViewById(R.id.translationTv)

        init {
            itemView.setOnClickListener {
                resultsRecyclerViewListener.onResultClicked(
                    adapterPosition
                )
            }
        }
    }
}
