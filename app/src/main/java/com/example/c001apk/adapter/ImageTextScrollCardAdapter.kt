package com.example.c001apk.adapter

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.FeedActivity
import com.example.c001apk.util.DensityTool
import com.example.c001apk.util.HistoryUtil
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
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
        var avatarUrl = ""
        var pubDate = ""
        var device = ""
        var message = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_image_text_scroll_card_item, parent, false)
        val padding = 50.dp
        val imageWidth =
            if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                DensityTool.getScreenWidth(parent.context) - padding
            else
                DensityTool.getScreenWidth(parent.context) / 2 - padding
        view.layoutParams.width = (imageWidth - imageWidth / 3)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            IntentUtil.startActivity<FeedActivity>(parent.context) {
                putExtra("id", viewHolder.id)
            }
            if (PrefManager.isRecordHistory)
                HistoryUtil.saveHistory(
                    viewHolder.id,
                    viewHolder.uid,
                    viewHolder.uname,
                    viewHolder.avatarUrl,
                    viewHolder.device,
                    viewHolder.message,
                    viewHolder.pubDate
                )
        }
        return viewHolder
    }

    override fun getItemCount() = imageCarouselCardList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageTextScrollCard = imageCarouselCardList[position]
        holder.avatarUrl = imageTextScrollCard.userInfo.userAvatar
        holder.pubDate = imageTextScrollCard.dateline
        holder.device = imageTextScrollCard.deviceTitle
        holder.message = imageTextScrollCard.message
        holder.id = imageTextScrollCard.id
        holder.uid = imageTextScrollCard.userInfo.uid
        holder.uname = imageTextScrollCard.username
        holder.title.text = imageTextScrollCard.title
        holder.title.setPadding(10.dp, 10.dp, 10.dp, 10.dp)
        ImageUtil.showIMG(holder.imageTextScrollCard, imageTextScrollCard.pic)
    }

}