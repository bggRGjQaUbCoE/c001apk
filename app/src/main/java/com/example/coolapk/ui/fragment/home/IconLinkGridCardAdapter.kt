package com.example.coolapk.ui.fragment.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coolapk.R
import com.example.coolapk.logic.model.HomeFeedResponse
import com.example.coolapk.util.ImageShowUtil

class IconLinkGridCardAdapter(
    private val iconLinkGridCardList: List<HomeFeedResponse.Entities>
) :
    RecyclerView.Adapter<IconLinkGridCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val iconLinkGridCard: ImageView = view.findViewById(R.id.iconLinkGridCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_icon_link_grid_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = iconLinkGridCardList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val iconLinkGridCard = iconLinkGridCardList[position]
        holder.title.text = iconLinkGridCard.title
        ImageShowUtil.showIMG(holder.iconLinkGridCard, iconLinkGridCard.pic)
    }

}