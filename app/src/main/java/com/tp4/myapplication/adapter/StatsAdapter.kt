package com.tp4.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tp4.myapplication.R
import com.tp4.myapplication.data.FiliereStats

class StatsAdapter(private val context: Context, private var statsList: List<FiliereStats>) :
    RecyclerView.Adapter<StatsAdapter.StatsViewHolder>() {

    class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val filiereTextView: TextView = itemView.findViewById(R.id.textViewFiliere)
        val countTextView: TextView = itemView.findViewById(R.id.textViewCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.fil_item, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val stat = statsList[position]
        holder.filiereTextView.text = stat.filiere
        holder.countTextView.text = stat.count.toString()
    }

    override fun getItemCount(): Int {
        return statsList.size
    }

    fun updateStats(newStats: List<FiliereStats>) {
        statsList = newStats
        notifyDataSetChanged()
    }
}
