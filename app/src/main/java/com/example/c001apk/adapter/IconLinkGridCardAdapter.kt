package com.example.c001apk.adapter

import android.view.ViewGroup
import android.widget.GridView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.view.NoScrollGridView

class IconLinkGridCardAdapter(
    private val dataList: List<List<HomeFeedResponse.Entities>>,
    private val listener: ItemListener,
) : RecyclerView.Adapter<IconLinkGridCardAdapter.ViewHolder>() {

    class ViewHolder(val view: GridView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = NoScrollGridView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            numColumns = 5
        }
        return ViewHolder(view)
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.view.adapter =
            IconLinkGridCardItemAdapter(data, listener)
    }

}