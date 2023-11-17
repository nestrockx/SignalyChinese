package com.wegielek.signalychinese.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.interfaces.CharactersRecyclerViewListener

class SuggestedCharactersAdapter(private val charactersRecyclerViewListener: CharactersRecyclerViewListener?) :
    RecyclerView.Adapter<SuggestedCharactersAdapter.ViewHolder>() {
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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val character: TextView

        init {
            character = itemView.findViewById(R.id.textViewItem)
            itemView.setOnTouchListener { view: View, motionEvent: MotionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    charactersRecyclerViewListener?.onItemPressed(itemView)
                } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                    view.performClick()
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        charactersRecyclerViewListener?.onItemReleased(pos)
                    }
                } else if (motionEvent.action == MotionEvent.ACTION_CANCEL) {
                    charactersRecyclerViewListener?.onItemCanceled(itemView)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_suggested_character_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.character.text = dataList[position]
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
