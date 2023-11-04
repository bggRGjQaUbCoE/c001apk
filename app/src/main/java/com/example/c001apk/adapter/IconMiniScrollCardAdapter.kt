package com.example.c001apk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.TopicActivity
import com.example.c001apk.util.ImageShowUtil

class IconMiniScrollCardAdapter(
    private val mContext: Context,
    private val iconMiniScrollCardList: List<HomeFeedResponse.Entities>
) :
    RecyclerView.Adapter<IconMiniScrollCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val iconMiniScrollCard: ImageView = view.findViewById(R.id.iconMiniScrollCard)
        var entityType = ""
        var aliasTitle = ""
        var url = ""
        var id = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_icon_mini_scroll_card_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(parent.context, TopicActivity::class.java)
            intent.putExtra("type", viewHolder.entityType)
            intent.putExtra("title", viewHolder.title.text)
            intent.putExtra("url", viewHolder.url)
            intent.putExtra("id", viewHolder.id)
            parent.context.startActivity(intent)
        }
        return viewHolder
    }

    override fun getItemCount() = iconMiniScrollCardList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val iconMiniScrollCard = iconMiniScrollCardList[position]
        holder.title.text = iconMiniScrollCard.title
        ImageShowUtil.showIMG(holder.iconMiniScrollCard, iconMiniScrollCard.logo)
        holder.url = iconMiniScrollCard.url
        holder.id = iconMiniScrollCard.id
        holder.entityType = iconMiniScrollCard.entityType
        if (iconMiniScrollCard.entityType == "product")
            holder.aliasTitle = iconMiniScrollCard.aliasTitle
        holder.entityType = iconMiniScrollCard.entityType
    }

}