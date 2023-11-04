package com.example.c001apk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.FeedActivity
import com.example.c001apk.util.DensityTool
import com.example.c001apk.util.ImageShowUtil
import com.google.android.material.imageview.ShapeableImageView

class ImageTextScrollCardAdapter(
    private val mContext: Context,
    private val imageCarouselCardList: List<HomeFeedResponse.Entities>
) :
    RecyclerView.Adapter<ImageTextScrollCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val imageTextScrollCard: ShapeableImageView = view.findViewById(R.id.imageTextScrollCard)
        var id = ""
        var uid = ""
        var uname = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_image_text_scroll_card_item, parent, false)
        val padding = 50f
        val spacePx = (DensityTool.dp2px(parent.context, padding))
        val imageWidth = DensityTool.getScreenWidth(parent.context) - spacePx
        view.layoutParams.width = (imageWidth - imageWidth / 3).toInt()
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(parent.context, FeedActivity::class.java)
            intent.putExtra("type", "feed")
            intent.putExtra("id", viewHolder.id)
            intent.putExtra("uid", viewHolder.uid)
            intent.putExtra("uname", viewHolder.uname)
            parent.context.startActivity(intent)
        }
        return viewHolder
    }

    override fun getItemCount() = imageCarouselCardList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageTextScrollCard = imageCarouselCardList[position]
        holder.id = imageTextScrollCard.id
        holder.uid = imageTextScrollCard.userInfo.uid
        holder.uname = imageTextScrollCard.username
        holder.title.text = imageTextScrollCard.title
        val space = mContext.resources.getDimensionPixelSize(R.dimen.normal_space)
        holder.title.setPadding(space, space, space, space)
        ImageShowUtil.showIMG1(holder.imageTextScrollCard, imageTextScrollCard.pic)
    }

}