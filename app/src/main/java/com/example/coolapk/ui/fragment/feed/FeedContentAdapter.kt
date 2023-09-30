package com.example.coolapk.ui.fragment.feed

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bili.util.PubDateUtil
import com.example.coolapk.R
import com.example.coolapk.logic.model.FeedContentResponse
import com.example.coolapk.logic.model.HomeFeedResponse
import com.example.coolapk.util.ImageShowUtil
import com.example.coolapk.util.SpacesItemDecoration

class FeedContentAdapter(
    private val mContext: Context,
    private val feedList: List<FeedContentResponse>,
    private val replyList: List<HomeFeedResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class FeedContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val uname: TextView = view.findViewById(R.id.uname)
        val device: TextView = view.findViewById(R.id.device)
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    class FeedContentReplyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val avatar: ImageView = view.findViewById(R.id.avatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_feed_content, parent, false)
                FeedContentViewHolder(view)
            }

            else -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_feed_content_reply_item, parent, false)
                FeedContentReplyViewHolder(view)
            }
        }
    }

    override fun getItemCount() = replyList.size + 1

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FeedContentViewHolder -> {
                if (feedList.isNotEmpty()) {
                    val feed = feedList[position]
                    holder.uname.text = feed.data.username
                    holder.device.text = feed.data.deviceTitle
                    holder.pubDate.text = PubDateUtil.time(feed.data.dateline)
                    holder.message.text =
                        Html.fromHtml(feed.data.message.replace("\n","<br />"), Html.FROM_HTML_MODE_COMPACT)
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

            is FeedContentReplyViewHolder -> {
                val reply = replyList[position - 1]
                holder.uname.text = reply.username
                holder.message.text = Html.fromHtml(reply.message, Html.FROM_HTML_MODE_COMPACT)
                holder.pubDate.text = PubDateUtil.time(reply.dateline)
                holder.like.text = reply.likenum
                val drawable: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
                drawable.setBounds(
                    0,
                    0,
                    holder.like.textSize.toInt(),
                    holder.like.textSize.toInt()
                )
                holder.like.setCompoundDrawables(drawable, null, null, null)
                ImageShowUtil.showAvatar(holder.avatar, reply.userAvatar)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}