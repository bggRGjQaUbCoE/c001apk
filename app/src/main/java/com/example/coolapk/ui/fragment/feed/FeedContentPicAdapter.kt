package com.example.coolapk.ui.fragment.feed

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.bean.ImageInfo
import com.example.coolapk.R
import com.example.coolapk.ui.fragment.minterface.IOnFeedPicClickContainer
import com.example.coolapk.util.ImageShowUtil

class FeedContentPicAdapter(
    private val feedPicList: List<String>
) :
    RecyclerView.Adapter<FeedContentPicAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val feedPic: ImageView = view.findViewById(R.id.feedPic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_feed_content_pic, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            val urlList: MutableList<ImageInfo> = ArrayList()
            for (element in feedPicList) {
                val imageInfo = ImageInfo()
                imageInfo.thumbnailUrl = "$element.s.jpg"
                imageInfo.originUrl = element
                urlList.add(imageInfo)
            }
            IOnFeedPicClickContainer.controller?.onShowPic(position, urlList)
        }
        return viewHolder
    }

    override fun getItemCount() = feedPicList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        ImageShowUtil.showIMG1(holder.feedPic, feedPicList[position] + ".xs.jpg")
    }

}