package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.ui.fragment.minterface.IOnItemClickListener

class HistoryAdapter(

) :
    ListAdapter<String, HistoryAdapter.ViewHolder>(HistoryDiffCallback()) {

    private lateinit var iOnItemClickListener: IOnItemClickListener

    fun setOnItemClickListener(iOnItemClickListener: IOnItemClickListener) {
        this.iOnItemClickListener = iOnItemClickListener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var keyWord: TextView = view.findViewById(R.id.keyWord)
        val delete: ImageView = view.findViewById(R.id.delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_search_history, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.keyWord.setOnLongClickListener {
            if (viewHolder.delete.visibility == View.GONE)
                viewHolder.delete.visibility = View.VISIBLE
            else
                viewHolder.delete.visibility = View.GONE
            true
        }
        viewHolder.delete.setOnClickListener {
            iOnItemClickListener.onItemDeleteClick(
                viewHolder.bindingAdapterPosition,
                viewHolder.keyWord.text.toString()
            )
            viewHolder.delete.visibility = View.GONE
        }
        viewHolder.keyWord.setOnClickListener {
            iOnItemClickListener.onItemClick(viewHolder.keyWord.text.toString())
        }
        return viewHolder
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.keyWord.text = currentList[position]
    }
}

class HistoryDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(
        oldItem: String,
        newItem: String
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: String,
        newItem: String
    ): Boolean {
        return oldItem == newItem
    }
}