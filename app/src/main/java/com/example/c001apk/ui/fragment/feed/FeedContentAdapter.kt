package com.example.c001apk.ui.fragment.feed

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.user.UserActivity
import com.example.c001apk.util.EmojiUtil
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.PubDateUtil
import com.example.c001apk.util.SpacesItemDecoration
import com.example.c001apk.view.CenteredImageSpan
import com.example.c001apk.view.MyURLSpan
import com.google.android.material.card.MaterialCardView
import java.util.regex.Pattern


class FeedContentAdapter(
    private val mContext: Context,
    private val feedList: List<FeedContentResponse>,
    private var replyList: ArrayList<HomeFeedResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var iOnTotalReplyClickListener: IOnTotalReplyClickListener? = null

    fun setIOnTotalReplyClickListener(iOnTotalReplyClickListener: IOnTotalReplyClickListener) {
        this.iOnTotalReplyClickListener = iOnTotalReplyClickListener
    }

    class FeedContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val uname: TextView = view.findViewById(R.id.uname)
        val device: TextView = view.findViewById(R.id.device)
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val like: TextView = view.findViewById(R.id.like)
        val reply: TextView = view.findViewById(R.id.reply)
    }

    class FeedContentReplyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val reply: TextView = view.findViewById(R.id.reply)
        val replyLayout: MaterialCardView = view.findViewById(R.id.replyLayout)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val totalReply: TextView = view.findViewById(R.id.totalReply)
        val picRecyclerView: RecyclerView = view.findViewById(R.id.picRecyclerView)
        var id = ""
        var uid = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_feed_content, parent, false)
                val viewHolder = FeedContentViewHolder(view)
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
                viewHolder
            }

            else -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_feed_content_reply_item, parent, false)
                val viewHolder = FeedContentReplyViewHolder(view)
                viewHolder.totalReply.setOnClickListener {
                    iOnTotalReplyClickListener?.onShowTotalReply(viewHolder.uid, viewHolder.id)
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
                viewHolder.message.setOnLongClickListener {
                    val intent = Intent(parent.context, CopyActivity::class.java)
                    intent.putExtra("text", viewHolder.message.text.toString())
                    parent.context.startActivity(intent)
                    true
                }
                viewHolder
            }
        }
    }

    override fun getItemCount() = replyList.size + 1

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FeedContentViewHolder -> {
                if (feedList.isNotEmpty()) {
                    val feed = feedList[position]
                    holder.uname.text = feed.data.username
                    holder.device.text = feed.data.deviceTitle
                    if (feed.data.deviceTitle != "") {
                        val drawable: Drawable = mContext.getDrawable(R.drawable.ic_device)!!
                        drawable.setBounds(
                            0,
                            0,
                            holder.device.textSize.toInt(),
                            holder.device.textSize.toInt()
                        )
                        holder.device.setCompoundDrawables(drawable, null, null, null)
                    } else {
                        holder.device.visibility = View.GONE
                    }
                    holder.pubDate.text = PubDateUtil.time(feed.data.dateline)
                    val drawable1: Drawable = mContext.getDrawable(R.drawable.ic_date)!!
                    drawable1.setBounds(
                        0,
                        0,
                        holder.pubDate.textSize.toInt(),
                        holder.pubDate.textSize.toInt()
                    )
                    holder.pubDate.setCompoundDrawables(drawable1, null, null, null)
                    holder.like.text = feed.data.likenum
                    val drawableLike: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
                    drawableLike.setBounds(
                        0,
                        0,
                        holder.like.textSize.toInt(),
                        holder.like.textSize.toInt()
                    )
                    holder.like.setCompoundDrawables(drawableLike, null, null, null)

                    holder.reply.text = feed.data.replynum
                    val drawableReply: Drawable = mContext.getDrawable(R.drawable.ic_message)!!
                    drawableReply.setBounds(
                        0,
                        0,
                        holder.like.textSize.toInt(),
                        holder.like.textSize.toInt()
                    )
                    holder.reply.setCompoundDrawables(drawableReply, null, null, null)

                    val mess = Html.fromHtml(
                        StringBuilder(feed.data.message).append(" ").toString()
                            .replace("\n", " <br />"),
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
                        if (EmojiUtil.getEmoji(group) != -1) {
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
                holder.id = reply.id
                holder.uid = reply.uid
                holder.uname.text = reply.username

                val mess = Html.fromHtml(
                    StringBuilder(reply.message).append(" ").toString().replace("\n", " <br />"),
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
                    if (EmojiUtil.getEmoji(group) != -1) {
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

                if (reply.replyRows.isNotEmpty()) {
                    holder.replyLayout.visibility = View.VISIBLE
                    val mAdapter = Reply2ReplyAdapter(mContext, reply.uid, reply.replyRows)
                    val mLayoutManager = LinearLayoutManager(mContext)
                    //val space = mContext.resources.getDimensionPixelSize(R.dimen.minor_space)
                    holder.recyclerView.apply {
                        adapter = mAdapter
                        layoutManager = mLayoutManager
                        //if (itemDecorationCount == 0) addItemDecoration(ReplyItemDecoration(space))
                    }
                } else holder.replyLayout.visibility = View.GONE

                if (reply.replyRowsMore != 0) {
                    holder.totalReply.visibility = View.VISIBLE
                    val count = reply.replyRowsMore + reply.replyRows.size
                    holder.totalReply.text = "查看更多回复($count)"
                } else
                    holder.totalReply.visibility = View.GONE

                if (reply.picArr != null && reply.picArr.isNotEmpty()) {
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

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}