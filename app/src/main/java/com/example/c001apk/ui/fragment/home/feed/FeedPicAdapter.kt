package com.example.c001apk.ui.fragment.home.feed

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.bean.ImageInfo
import com.example.c001apk.R
import com.example.c001apk.ui.fragment.minterface.IOnFeedPicClickContainer
import com.example.c001apk.util.ImageShowUtil

class FeedPicAdapter(
    private val feedPicList: List<String>
) :
    RecyclerView.Adapter<FeedPicAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val count: TextView = view.findViewById(R.id.count)
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

    override fun getItemCount() =
        when (feedPicList.size) {
            in 4..5 -> 3
            in 7..8 -> 6
            else -> feedPicList.size
        }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (feedPicList.size) {
            in 4..5 -> {
                if (position == 2) {
                    holder.count.visibility = View.VISIBLE
                    holder.count.text = "${feedPicList.size}图"
                }
            }

            in 7..8 -> {
                if (position == 5) {
                    holder.count.visibility = View.VISIBLE
                    holder.count.text = "${feedPicList.size}图"
                }
            }

            else -> holder.count.visibility = View.GONE
        }
        ImageShowUtil.showIMG1(holder.feedPic, feedPicList[position] + ".xs.jpg")
    }

}