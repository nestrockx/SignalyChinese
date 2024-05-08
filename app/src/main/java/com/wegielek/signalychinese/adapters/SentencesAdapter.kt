package com.wegielek.signalychinese.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.database.Sentences
import com.wegielek.signalychinese.utils.TextToSpeechManager
import com.wegielek.signalychinese.utils.Utils.Companion.showDefinitionPopup
import com.wegielek.signalychinese.utils.Utils.Companion.showSentencePopup

class SentencesAdapter (
    private val context: Context
) : RecyclerView.Adapter<SentencesAdapter.ViewHolder>() {

    private val dataList: MutableList<Sentences> = ArrayList()

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
        holder.traditionalSent.text = dataList[position].traditionalSign
        holder.simplifiedSent.text = dataList[position].simplifiedSign
        if (holder.traditionalSent.text.toString() == holder.simplifiedSent.text.toString()) {
            holder.traditionalSent.visibility = View.GONE
        } else {
            holder.traditionalSent.setOnClickListener {

                showDefinitionPopup(
                    it,
                    holder.traditionalSent,
                    holder.traditionalSent.text.toString(),
                    "zh-CN",
                    "en",
                    context.getColor(R.color.selection_color)
                )

            }
        }
        holder.translation.text = dataList[position].translation
        holder.speakSentenceBtn.setOnClickListener {
            TextToSpeechManager.speakCH(
                holder.simplifiedSent,
                holder.simplifiedSent.text.toString(),
                context.getColor(R.color.selection_color)
            )
        }
        holder.moreBtn.setOnClickListener {
            showSentencePopup(it, holder.translation, holder.translation.text.toString(), context.getColor(R.color.dark_mode_white))
        }

        holder.simplifiedSent.setOnClickListener {

            showDefinitionPopup(
                it,
                holder.simplifiedSent,
                holder.simplifiedSent.text.toString(),
                "zh-TW",
                "en",
                context.getColor(R.color.selection_color)
            )

        }

        holder.translation.setOnClickListener {

            showDefinitionPopup(
                it,
                holder.translation,
                holder.translation.text.toString(),
                "pl",
                "en",
                context.getColor(R.color.dark_mode_white)
            )

        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {
        val traditionalSent: TextView = itemView.findViewById(R.id.traditionalSent)
        val simplifiedSent: TextView = itemView.findViewById(R.id.simplifiedSent)
        val translation: TextView = itemView.findViewById(R.id.translationSent)
        val speakSentenceBtn: ImageButton = itemView.findViewById(R.id.speakSentenceBtn)
        val moreBtn: ImageButton = itemView.findViewById(R.id.sentencesMoreBtn)
    }
}