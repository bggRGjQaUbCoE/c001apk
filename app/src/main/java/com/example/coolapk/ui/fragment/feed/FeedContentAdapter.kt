package com.example.coolapk.ui.fragment.feed

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bili.util.PubDateUtil
import com.example.coolapk.R
import com.example.coolapk.logic.model.FeedContentResponse
import com.example.coolapk.util.ImageShowUtil
import com.example.coolapk.util.SpacesItemDecoration

class FeedContentAdapter(
    private val mContext: Context,
    private val feedList: List<FeedContentResponse>
) :
    RecyclerView.Adapter<FeedContentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val uname: TextView = view.findViewById(R.id.uname)
        val device: TextView = view.findViewById(R.id.device)
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_feed_content, parent, false)
        val viewHolder = ViewHolder(view)
        return viewHolder
    }

    override fun getItemCount() = feedList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feed = feedList[position]
        holder.uname.text = feed.data.username
        holder.device.text = feed.data.deviceTitle
        holder.pubDate.text = PubDateUtil.time(feed.data.dateline)
        holder.message.text = Html.fromHtml(feed.data.message, Html.FROM_HTML_MODE_COMPACT)
        if (feed.data.picArr.isNotEmpty()) {
            holder.recyclerView.visibility = View.VISIBLE
            val mAdapter = FeedContentPicAdapter(feed.data.picArr)
            val count =
                if (feed.data.picArr.size < 3) feed.data.picArr.size
                else 3
            val mLayoutManager = GridLayoutManager(mContext, count)
            val space = mContext.resources.getDimensionPixelSize(R.dimen.minor_space)
            val spaceValue = HashMap<String, Int>()
            spaceValue[SpacesItemDecoration.TOP_SPACE] = 0
            spaceValue[SpacesItemDecoration.BOTTOM_SPACE] = space
            spaceValue[SpacesItemDecoration.LEFT_SPACE] = space
            spaceValue[SpacesItemDecoration.RIGHT_SPACE] = space
            holder.recyclerView.apply {
                setPadding(space, 0, space, space)
                adapter = mAdapter
                layoutManager = mLayoutManager
                if (itemDecorationCount == 0)
                    addItemDecoration(SpacesItemDecoration(count, spaceValue, true))
            }
        } else {
            holder.recyclerView.visibility = View.GONE
        }
        ImageShowUtil.showAvatar(holder.avatar, feed.data.userAvatar)
    }

}