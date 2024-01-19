package com.wonderapps.translator.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.wonderapps.translator.R

class WordAdapter(context: Context, words: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, words) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val word = getItem(position)

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            val customView = inflater.inflate(R.layout.custom_dropdown_item, parent, false)
            val textView = customView.findViewById<TextView>(R.id.textView)
            textView.text = word
            return customView
        }

        return view
    }
}
