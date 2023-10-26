package com.example.c001apk.ui.fragment.feed.total

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.user.UserActivity
import com.example.c001apk.ui.fragment.feed.FeedContentPicAdapter
import com.example.c001apk.ui.fragment.feed.IOnReplyClickContainer
import com.example.c001apk.util.EmojiUtil
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.PubDateUtil
import com.example.c001apk.view.CenteredImageSpan
import com.example.c001apk.view.MyURLSpan
import java.util.regex.Pattern


class Reply2ReplyTotalAdapter(
    private val mContext: Context,
    private val uid: String,
    private val replyList: List<HomeFeedResponse.Data>
) : RecyclerView.Adapter<Reply2ReplyTotalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        var id = ""
        var name = ""
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val reply: TextView = view.findViewById(R.id.reply)
        val picRecyclerView: RecyclerView = view.findViewById(R.id.picRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reply_to_reply_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.avatar.setOnClickListener {
            val intent = Intent(parent.context, UserActivity::class.java)
            intent.putExtra("id", viewHolder.name)
            parent.context.startActivity(intent)
        }
        viewHolder.message.setOnLongClickListener {
            val intent = Intent(parent.context, CopyActivity::class.java)
            intent.putExtra("text", viewHolder.message.text.toString())
            parent.context.startActivity(intent)
            true
        }
        viewHolder.itemView.setOnClickListener {
            IOnReplyClickContainer.controller?.onReply2Reply(
                viewHolder.id,
                viewHolder.name,
                "reply"
            )
        }
        viewHolder.message.setOnClickListener {
            IOnReplyClickContainer.controller?.onReply2Reply(
                viewHolder.id,
                viewHolder.name,
                "reply"
            )
        }
        return viewHolder
    }

    override fun getItemCount() = replyList.size

    @SuppressLint("SetTextI18n", "RestrictedApi", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reply = replyList[position]

        holder.id = reply.id
        holder.name = reply.username

        val text =
            if (uid == reply.ruid)
                """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>"""
            else
                """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>回复<a class="feed-link-uname" href="/u/${reply.rusername}">${reply.rusername}</a>"""
        val name = Html.fromHtml(
            text.replace("\n", " <br />"),
            Html.FROM_HTML_MODE_COMPACT
        )
        val nameBuilder = SpannableStringBuilder(name)
        val nameUrls = nameBuilder.getSpans(
            0, text.length,
            URLSpan::class.java
        )
        for (url in nameUrls) {
            val myURLSpan = MyURLSpan(mContext, "name", url.url, null)
            val start = nameBuilder.getSpanStart(url)
            val end = nameBuilder.getSpanEnd(url)
            val flags = nameBuilder.getSpanFlags(url)
            nameBuilder.setSpan(myURLSpan, start, end, flags)
            nameBuilder.removeSpan(url)
        }
        holder.uname.text = nameBuilder
        holder.uname.movementMethod = LinkMovementMethod.getInstance()

        val mess = Html.fromHtml(
            reply.message.replace("\n", " <br />"),
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
            val myURLSpan = MyURLSpan(mContext, null, url.url, null)
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

        holder.pubDate.text = PubDateUtil.time(reply.dateline)
        val drawable1: Drawable = mContext.getDrawable(R.drawable.ic_date)!!
        drawable1.setBounds(
            0,
            0,
            holder.pubDate.textSize.toInt(),
            holder.pubDate.textSize.toInt()
        )
        holder.pubDate.setCompoundDrawables(drawable1, null, null, null)
        holder.like.text = reply.likenum
        val drawableLike: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
        drawableLike.setBounds(
            0,
            0,
            holder.like.textSize.toInt(),
            holder.like.textSize.toInt()
        )
        holder.like.setCompoundDrawables(drawableLike, null, null, null)
        holder.reply.text = reply.replynum
        val drawableReply: Drawable = mContext.getDrawable(R.drawable.ic_message)!!
        drawableReply.setBounds(
            0,
            0,
            holder.like.textSize.toInt(),
            holder.like.textSize.toInt()
        )
        holder.reply.setCompoundDrawables(drawableReply, null, null, null)
        ImageShowUtil.showAvatar(holder.avatar, reply.userAvatar)

        if (!reply.picArr.isNullOrEmpty()) {
            holder.picRecyclerView.visibility = View.VISIBLE
            val mAdapter = FeedContentPicAdapter(reply.picArr)
            val count =
                if (reply.picArr.size < 3) reply.picArr.size
                else 3
            val mLayoutManager = GridLayoutManager(mContext, count)
            holder.picRecyclerView.apply {
                adapter = mAdapter
                layoutManager = mLayoutManager
            }
        } else holder.picRecyclerView.visibility = View.GONE

    }

}