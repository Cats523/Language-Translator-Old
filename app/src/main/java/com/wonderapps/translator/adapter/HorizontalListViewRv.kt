package com.wonderapps.translator.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wonderapps.translator.R
import com.wonderapps.translator.utils.dataclasses.ConversationLanguage

interface OnItemClickListener {
    fun onItemClick(language: ConversationLanguage)
}

class HorizontalListViewRv(
    private var languageList: ArrayList<ConversationLanguage>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<HorizontalListViewRv.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_horizontal_lan_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = languageList[position].languageName
    }

    override fun getItemCount(): Int {
        return languageList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.horizontal_language_textview)

        init {
            itemView.setOnClickListener {

                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val language = languageList[position]
                    itemClickListener.onItemClick(language)
                }
            }
        }
    }
}
