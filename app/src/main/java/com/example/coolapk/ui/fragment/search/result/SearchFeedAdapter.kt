package com.example.coolapk.ui.fragment.search.result

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bili.util.PubDateUtil
import com.example.coolapk.R
import com.example.coolapk.logic.model.HomeFeedResponse
import com.example.coolapk.ui.activity.feed.FeedActivity
import com.example.coolapk.ui.fragment.feed.FeedContentPicAdapter
import com.example.coolapk.util.EmojiUtil
import com.example.coolapk.util.ImageShowUtil
import com.example.coolapk.util.SpacesItemDecoration
import java.util.regex.Pattern

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
        val reply: TextView = view.findViewById(R.id.reply)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        var id = ""
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_feed, parent, false)
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

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feed = searchList[position]
        holder.id = feed.id
        holder.uname.text = feed.username

        val mess = Html.fromHtml(
            feed.message.replace("\n", "<br />"),
            Html.FROM_HTML_MODE_COMPACT
        )
        val builder = SpannableStringBuilder(mess)
        val pattern = Pattern.compile("\\[[^\\]]+\\]")
        val matcher = pattern.matcher(mess)
        holder.message.text = mess
        while (matcher.find()) {
            val group = matcher.group()
            val emoji: Drawable =
                mContext.getDrawable(EmojiUtil.getEmoji(group))!!
            emoji.setBounds(
                0,
                0,
                (holder.message.textSize * 1.3).toInt(),
                (holder.message.textSize * 1.3).toInt()
            )
            val imageSpan = ImageSpan(emoji, ImageSpan.ALIGN_BASELINE)
            builder.setSpan(
                imageSpan,
                matcher.start(),
                matcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.message.text = builder
        }

        holder.pubDate.text = PubDateUtil.time(feed.dateline)

        holder.like.text = feed.likenum
        val drawableLike: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
        drawableLike.setBounds(
            0,
            0,
            holder.like.textSize.toInt(),
            holder.like.textSize.toInt()
        )
        holder.like.setCompoundDrawables(drawableLike, null, null, null)

        holder.reply.text = feed.replynum
        val drawableReply: Drawable = mContext.getDrawable(R.drawable.ic_message)!!
        drawableReply.setBounds(
            0,
            0,
            holder.like.textSize.toInt(),
            holder.like.textSize.toInt()
        )
        holder.reply.setCompoundDrawables(drawableReply, null, null, null)
        ImageShowUtil.showAvatar(holder.avatar, feed.userAvatar)

        if (feed.picArr.isNotEmpty()) {
            holder.recyclerView.visibility = View.VISIBLE
            val mAdapter = FeedContentPicAdapter(feed.picArr)
            val count =
                if (feed.picArr.size < 3) feed.picArr.size
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
        } else holder.recyclerView.visibility = View.GONE
    }

}