package com.example.c001apk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.IconLinkGridCardBean

class IconLinkGridCardAdapter(
    private val mContext: Context,
    private val dataList: List<List<IconLinkGridCardBean>>
) : RecyclerView.Adapter<IconLinkGridCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView :RecyclerView= view.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(mContext)
                .inflate(R.layout.item_recyclerview, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val layoutManager = GridLayoutManager(mContext, 5, GridLayoutManager.VERTICAL, false)
        holder.recyclerView.layoutManager = layoutManager
        val itemAdapter = IconLinkGridCardItemAdapter(mContext, dataList[position])
        holder.recyclerView.adapter = itemAdapter
    }

}