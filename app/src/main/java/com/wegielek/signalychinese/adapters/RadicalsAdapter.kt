package com.wegielek.signalychinese.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.interfaces.RadicalsRecyclerViewListener

class RadicalsAdapter(
    private val context: Context,
    private val radicalsRecyclerViewListener: RadicalsRecyclerViewListener
) :
    RecyclerView.Adapter<RadicalsAdapter.ViewHolder>() {
    private val dataList: MutableList<Array<String>>

    init {
        dataList = ArrayList()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataList: List<Array<String>>?) {
        this.dataList.clear()
        this.dataList.addAll(dataList!!)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_radicals_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.flexboxLayout.removeAllViews()
        for (i in dataList[position].indices) {
            val textView = TextView(context)
            textView.textSize = 32.0f
            textView.setTextColor(context.getColor(R.color.dark_mode_white))
            textView.text = dataList[position][i]
            textView.setPadding(16, 8, 16, 8)
            textView.setOnTouchListener { v: View, event: MotionEvent ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                        context.getColor(R.color.selection_color)
                    )

                    MotionEvent.ACTION_UP -> {
                        v.performClick()
                        v.setBackgroundColor(Color.TRANSPARENT)
                        radicalsRecyclerViewListener.onRadicalClicked(
                            dataList[position][i]
                        )
                    }

                    MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(Color.TRANSPARENT)
                }
                true
            }
            holder.flexboxLayout.addView(textView)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flexboxLayout: FlexboxLayout

        init {
            flexboxLayout = itemView.findViewById(R.id.radicalsLayoutItem)
            flexboxLayout.removeAllViews()
        }
    }
}
