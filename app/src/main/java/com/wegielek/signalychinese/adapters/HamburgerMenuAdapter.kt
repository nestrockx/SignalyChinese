package com.wegielek.signalychinese.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wegielek.signalychinese.R
import com.wegielek.signalychinese.utils.Utils.Companion.showMicLanguagePopup
import com.wegielek.signalychinese.utils.Utils.Companion.showSearchModePopup
import com.wegielek.signalychinese.views.AboutActivity
import com.wegielek.signalychinese.views.HistoryActivity
import com.wegielek.signalychinese.views.SchoolActivity

class HamburgerMenuAdapter(private val dataList: List<String>) : RecyclerView.Adapter<HamburgerMenuAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_hamburger_menu , parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameTv.text = dataList[position]
        when (position) {
            0 -> {
                holder.nameTv.setOnClickListener {
                    showSearchModePopup(it)
                }
            }
            1 -> {
                holder.nameTv.setOnClickListener {
                    showMicLanguagePopup(it)
                }
            }
            2 -> {
                holder.nameTv.setOnClickListener {
                    val intent = Intent(it.context, HistoryActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    it.context.startActivity(intent)
                }
            }
            3 -> {
                holder.nameTv.setOnClickListener {
                    val intent = Intent(it.context, SchoolActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    it.context.startActivity(intent)
                }
            }
            4 -> {
                holder.nameTv.setOnClickListener {
                    val intent = Intent(it.context, AboutActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    it.context.startActivity(intent)
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTv: TextView

        init {
            nameTv = itemView.findViewById(R.id.menuItem)
        }
    }
}