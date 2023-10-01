package com.example.coolapk.ui.fragment.search.result

import android.content.Context
import android.content.Intent
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
import com.example.coolapk.ui.activity.feed.FeedActivity
import com.example.coolapk.util.ImageShowUtil

class SearchFeedAdapter(
    private val mContext: Context,
    private val searchList: List<HomeFeedResponse.Data>
) :
    RecyclerView.Adapter<SearchFeedAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        var id = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_feed_content_reply_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(parent.context, FeedActivity::class.java)
            intent.putExtra("type", "feed")
            intent.putExtra("id", viewHolder.id)
            //intent.putExtra("uname", viewHolder.uname.text)
            //intent.putExtra("device", viewHolder.device.text)
            parent.context.startActivity(intent)
        }
        return viewHolder
    }

    override fun getItemCount() = searchList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feed = searchList[position]
        holder.id = feed.id
        holder.uname.text = feed.username
        holder.message.text = Html.fromHtml(feed.message, Html.FROM_HTML_MODE_COMPACT)
        holder.pubDate.text = feed.dateline?.let { PubDateUtil.time(feed.dateline) }
        holder.like.text = feed.likenum
        val drawable: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
        drawable.setBounds(
            0,
            0,
            holder.like.textSize.toInt(),
            holder.like.textSize.toInt()
        )
        holder.like.setCompoundDrawables(drawable, null, null, null)
        ImageShowUtil.showAvatar(holder.avatar, feed.userAvatar)
    }

}