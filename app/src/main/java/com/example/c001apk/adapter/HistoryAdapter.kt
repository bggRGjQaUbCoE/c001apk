package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.ui.fragment.minterface.IOnItemClickListener

class HistoryAdapter(private var historyList: ArrayList<String>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private lateinit var iOnItemClickListener: IOnItemClickListener

    fun setOnItemClickListener(iOnItemClickListener: IOnItemClickListener) {
        this.iOnItemClickListener = iOnItemClickListener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var keyWord: TextView = view.findViewById(R.id.keyWord)
        val delete: ImageButton = view.findViewById(R.id.delete)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_history, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.keyWord.setOnLongClickListener {
            if (viewHolder.delete.visibility == View.GONE)
                viewHolder.delete.visibility = View.VISIBLE
            else
                viewHolder.delete.visibility = View.GONE
            true
        }
        viewHolder.delete.setOnClickListener {
            val position = viewHolder.adapterPosition
            iOnItemClickListener.onItemDeleteClick(historyList[position])
            viewHolder.delete.visibility = View.GONE
        }
        viewHolder.keyWord.setOnClickListener {
            val position = viewHolder.adapterPosition
            iOnItemClickListener.onItemClick(historyList[position])
        }
        return viewHolder
    }

    override fun getItemCount() = historyList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val keyWord = historyList[position]
        holder.keyWord.text = keyWord
    }
}