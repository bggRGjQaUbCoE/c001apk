package com.example.coolapk.ui.fragment.search.result

import android.graphics.drawable.Drawable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bili.util.PubDateUtil
import com.example.coolapk.R
import com.example.coolapk.logic.model.HomeFeedResponse
import com.example.coolapk.util.ImageShowUtil

class SearchFeedAdapter(
    private val searchList: List<HomeFeedResponse.Data>
) :
    RecyclerView.Adapter<SearchFeedAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val avatar: ImageView = view.findViewById(R.id.avatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_feed_content_reply_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = searchList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feed = searchList[position]
        holder.uname.text = feed.username
        holder.message.text = Html.fromHtml(feed.message, Html.FROM_HTML_MODE_COMPACT)
        holder.pubDate.text = PubDateUtil.time(feed.dateline)
        holder.like.text = feed.likenum
        ImageShowUtil.showAvatar(holder.avatar, feed.userAvatar)
    }

}