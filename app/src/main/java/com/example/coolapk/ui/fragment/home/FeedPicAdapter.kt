package com.example.coolapk.ui.fragment.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.bean.ImageInfo
import com.example.coolapk.R
import com.example.coolapk.ui.fragment.minterface.IOnFeedPicClickContainer
import com.example.coolapk.util.ImageShowUtil

class FeedPicAdapter(
    private val feedPicList: List<String>
) :
    RecyclerView.Adapter<FeedPicAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val feedPic: ImageView = view.findViewById(R.id.feedPic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_feed_pic, parent, false)
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        ImageShowUtil.showIMG1(holder.feedPic, feedPicList[position] + ".xs.jpg")
    }

    private fun http2https(url: String) =
        if (StringBuilder(url)[4] != 's')
            StringBuilder(url).insert(4, "s").toString()
        else url

}