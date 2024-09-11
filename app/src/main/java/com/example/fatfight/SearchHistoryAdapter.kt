package com.example.fatfight

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class SearchHistoryAdapter(private val allData: ArrayList<String>, private val activity: FragmentMain) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.search_history_dropdown, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item.text = allData[position]
        holder.item.setOnClickListener{
            activity.searchInput.setText(allData[position])
            activity.unFocusInputSearch()
            activity.doSearch(allData[position])
        }
    }

    override fun getItemCount(): Int {
        return  allData.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var item : Button

        init {
            item = itemView.findViewById(R.id.recipe)
        }
    }
}