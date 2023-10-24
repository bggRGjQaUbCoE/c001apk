package com.example.c001apk.ui.fragment.search.result

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.util.PubDateUtil
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.feed.FeedActivity
import com.example.c001apk.ui.activity.user.UserActivity
import com.example.c001apk.ui.fragment.feed.FeedContentPicAdapter
import com.example.c001apk.ui.fragment.home.feed.FeedPicAdapter
import com.example.c001apk.util.EmojiUtil
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.SpacesItemDecoration
import com.example.c001apk.view.CenteredImageSpan
import com.example.c001apk.view.MyURLSpan
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
        val device: TextView = view.findViewById(R.id.device)
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
            intent.putExtra("uname", viewHolder.uname.text)
            parent.context.startActivity(intent)
        }
        viewHolder.message.setOnClickListener {
            val intent = Intent(parent.context, FeedActivity::class.java)
            intent.putExtra("type", "feed")
            intent.putExtra("id", viewHolder.id)
            intent.putExtra("uname", viewHolder.uname.text)
            parent.context.startActivity(intent)
        }
        viewHolder.itemView.setOnLongClickListener {
            val intent = Intent(parent.context, CopyActivity::class.java)
            intent.putExtra("text", viewHolder.message.text.toString())
            parent.context.startActivity(intent)
            true
        }
        viewHolder.message.setOnLongClickListener {
            val intent = Intent(parent.context, CopyActivity::class.java)
            intent.putExtra("text", viewHolder.message.text.toString())
            parent.context.startActivity(intent)
            true
        }
        viewHolder.avatar.setOnClickListener {
            val intent = Intent(parent.context, UserActivity::class.java)
            intent.putExtra("id", viewHolder.uname.text)
            parent.context.startActivity(intent)
        }
        viewHolder.uname.setOnClickListener {
            val intent = Intent(parent.context, UserActivity::class.java)
            intent.putExtra("id", viewHolder.uname.text)
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
        if (feed.deviceTitle != "") {
            holder.device.text = feed.deviceTitle
            val drawable: Drawable = mContext.getDrawable(R.drawable.ic_device)!!
            drawable.setBounds(
                0,
                0,
                holder.device.textSize.toInt(),
                holder.device.textSize.toInt()
            )
            holder.device.setCompoundDrawables(drawable, null, null, null)
            holder.device.visibility = View.VISIBLE
        } else {
            holder.device.visibility = View.GONE
        }
        holder.pubDate.text = PubDateUtil.time(feed.dateline)
        val drawable1: Drawable = mContext.getDrawable(R.drawable.ic_date)!!
        drawable1.setBounds(
            0,
            0,
            holder.pubDate.textSize.toInt(),
            holder.pubDate.textSize.toInt()
        )
        holder.pubDate.setCompoundDrawables(drawable1, null, null, null)

        val mess = Html.fromHtml(
            StringBuilder(feed.message).append(" ").toString().replace("\n", " <br />"),
            Html.FROM_HTML_MODE_COMPACT
        )
        val builder = SpannableStringBuilder(mess)
        val pattern = Pattern.compile("\\[[^\\]]+\\]")
        val matcher = pattern.matcher(builder)
        val urls = builder.getSpans(
            0, mess.length,
            URLSpan::class.java
        )
        for (url in urls) {
            val myURLSpan = MyURLSpan(mContext, feed.id, url.url, null)
            val start = builder.getSpanStart(url)
            val end = builder.getSpanEnd(url)
            val flags = builder.getSpanFlags(url)
            builder.setSpan(myURLSpan, start, end, flags)
            builder.removeSpan(url)
        }
        holder.message.text = builder
        holder.message.movementMethod = LinkMovementMethod.getInstance()
        while (matcher.find()) {
            val group = matcher.group()
            if (EmojiUtil.getEmoji(group) != -1){
                val emoji: Drawable =
                    mContext.getDrawable(EmojiUtil.getEmoji(group))!!
                emoji.setBounds(
                    0,
                    0,
                    (holder.message.textSize * 1.3).toInt(),
                    (holder.message.textSize * 1.3).toInt()
                )
                val imageSpan = CenteredImageSpan(emoji)
                builder.setSpan(
                    imageSpan,
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                holder.message.text = builder
            }
        }
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
            val mAdapter = FeedPicAdapter(feed.picArr)
            val count =
                if (feed.picArr.size < 3) feed.picArr.size
                else 3
            val mLayoutManager = GridLayoutManager(mContext, count)
            val minorSpace = mContext.resources.getDimensionPixelSize(R.dimen.minor_space)
            val normalSpace = mContext.resources.getDimensionPixelSize(R.dimen.normal_space)
            holder.recyclerView.apply {
                setPadding(normalSpace, 0, minorSpace, minorSpace)
                adapter = mAdapter
                layoutManager = mLayoutManager
            }
        } else holder.recyclerView.visibility = View.GONE
    }

}