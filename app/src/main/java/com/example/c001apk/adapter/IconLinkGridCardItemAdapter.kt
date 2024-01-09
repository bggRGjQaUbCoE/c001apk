package com.example.c001apk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.IconLinkGridCardBean
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.NetWorkUtil.openLink

class IconLinkGridCardItemAdapter(
    private val mContext: Context,
    private val dataList: List<IconLinkGridCardBean>
) : RecyclerView.Adapter<IconLinkGridCardItemAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var iconLinkGridCard: ImageView = itemView.findViewById(R.id.iconLinkGridCard)
        var title: TextView = itemView.findViewById(R.id.title)
        var url = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(mContext)
                .inflate(R.layout.item_home_icon_link_grid_card_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            openLink(parent.context, viewHolder.url, viewHolder.title.text.toString())
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.title.text = data.title
        holder.url = data.url
        ImageUtil.showIMG(holder.iconLinkGridCard, data.pic)
    }

    override fun getItemCount() = dataList.size
}