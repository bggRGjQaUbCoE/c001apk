package com.example.c001apk.ui.activity.fff

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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.ImagePreview
import cc.shinichi.library.bean.ImageInfo
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.feed.FeedActivity
import com.example.c001apk.ui.activity.user.UserActivity
import com.example.c001apk.ui.fragment.home.feed.HomeFeedAdapter
import com.example.c001apk.ui.fragment.search.result.SearchAdapter
import com.example.c001apk.util.EmojiUtil
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.PubDateUtil
import com.example.c001apk.view.CenteredImageSpan
import com.example.c001apk.view.MyURLSpan
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.util.regex.Pattern

class FFFListAdapter(
    private val mContext: Context,
    private val type: String,
    private val contentList: ArrayList<HomeFeedResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var loadState = 2
    val LOADING = 1
    val LOADING_COMPLETE = 2
    val LOADING_END = 3

    @SuppressLint("NotifyDataSetChanged")
    fun setLoadState(loadState: Int) {
        this.loadState = loadState
        notifyDataSetChanged()
    }

    class FootViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerLayout: FrameLayout = view.findViewById(R.id.footerLayout)
        val indicator: CircularProgressIndicator = view.findViewById(R.id.indicator)
        val noMore: TextView = view.findViewById(R.id.noMore)
    }

    class FollowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        val follow: TextView = view.findViewById(R.id.follow)
        val fans: TextView = view.findViewById(R.id.fans)
        val act: TextView = view.findViewById(R.id.act)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        var uid = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            -1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_rv_footer, parent, false)
                FootViewHolder(view)
            }

            0 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_home_feed, parent, false)
                val viewHolder = HomeFeedAdapter.FeedViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    val intent = Intent(parent.context, FeedActivity::class.java)
                    intent.putExtra("type", "feed")
                    intent.putExtra("id", viewHolder.id)
                    intent.putExtra("uid", viewHolder.uid)
                    intent.putExtra("uname", viewHolder.uname.text)
                    parent.context.startActivity(intent)
                }
                viewHolder.message.setOnClickListener {
                    val intent = Intent(parent.context, FeedActivity::class.java)
                    intent.putExtra("type", "feed")
                    intent.putExtra("id", viewHolder.id)
                    intent.putExtra("uid", viewHolder.uid)
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
                viewHolder
            }

            1 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_user, parent, false)
                val viewHolder = FollowViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    val intent = Intent(parent.context, UserActivity::class.java)
                    intent.putExtra("id", viewHolder.uname.text)
                    parent.context.startActivity(intent)
                }
                viewHolder
            }

            else -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_user, parent, false)
                val viewHolder = SearchAdapter.UserViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    val intent = Intent(parent.context, UserActivity::class.java)
                    intent.putExtra("id", viewHolder.uname.text)
                    parent.context.startActivity(intent)
                }
                viewHolder
            }

        }

    }

    override fun getItemCount() = contentList.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) -1
        else when (type) {
            "feed" -> 0
            "follow" -> 1
            else -> 2 //fans
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FootViewHolder -> {
                when (loadState) {
                    LOADING -> {
                        holder.footerLayout.visibility = View.VISIBLE
                        holder.indicator.visibility = View.VISIBLE
                        holder.indicator.isIndeterminate = true
                        holder.noMore.visibility = View.GONE

                    }

                    LOADING_COMPLETE -> {
                        holder.footerLayout.visibility = View.GONE
                        holder.indicator.visibility = View.GONE
                        holder.indicator.isIndeterminate = false
                        holder.noMore.visibility = View.GONE
                    }

                    LOADING_END -> {
                        holder.footerLayout.visibility = View.VISIBLE
                        holder.indicator.visibility = View.GONE
                        holder.indicator.isIndeterminate = false
                        holder.noMore.visibility = View.VISIBLE
                    }

                    else -> {}
                }
            }

            is FollowViewHolder -> {
                val user = contentList[position]
                holder.uid = user.fUserInfo.uid
                holder.uname.text = user.fUserInfo.username
                holder.follow.text = "${user.fUserInfo.follow}关注"
                holder.fans.text = "${user.fUserInfo.fans}粉丝"
                holder.act.text = PubDateUtil.time(user.fUserInfo.logintime) + "活跃"
                ImageShowUtil.showAvatar(holder.avatar, user.fUserInfo.userAvatar)
            }

            is SearchAdapter.UserViewHolder -> {
                val user = contentList[position]
                holder.uid = user.userInfo.uid
                holder.uname.text = user.userInfo.username
                holder.follow.text = "${user.userInfo.follow}关注"
                holder.fans.text = "${user.userInfo.fans}粉丝"
                holder.act.text = PubDateUtil.time(user.userInfo.logintime) + "活跃"
                ImageShowUtil.showAvatar(holder.avatar, user.userInfo.userAvatar)
            }

            is HomeFeedAdapter.FeedViewHolder -> {
                val feed = contentList[position]
                holder.id = feed.id
                holder.uid = feed.uid
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
                if (feed.infoHtml == "")
                    holder.from.visibility = View.GONE
                else
                    holder.from.text = Html.fromHtml(
                        feed.infoHtml.replace("\n", " <br />"),
                        Html.FROM_HTML_MODE_COMPACT
                    )

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

                if (!feed.picArr.isNullOrEmpty()) {
                    holder.multiImage.visibility = View.VISIBLE
                    val imageUrls= ArrayList<String>()
                    for (element in feed.picArr)
                        imageUrls.add(element)
                    holder.multiImage.setImageUrls(imageUrls)

                    val urlList: MutableList<ImageInfo> = ArrayList()
                    for (element in feed.picArr) {
                        val imageInfo = ImageInfo()
                        imageInfo.thumbnailUrl = "$element.s.jpg"
                        imageInfo.originUrl = element
                        urlList.add(imageInfo)
                    }

                    holder.multiImage.setOnClickItemListener { i, _ ->
                        ImagePreview.instance
                            .setContext(mContext)
                            .setImageInfoList(urlList)
                            .setIndex(i)
                            .setShowCloseButton(true)
                            .setEnableDragClose(true)
                            .setEnableUpDragClose(true)
                            .setFolderName("c001apk")
                            .start()
                    }
                } else {
                    holder.multiImage.visibility = View.GONE
                }
                ImageShowUtil.showAvatar(holder.avatar, feed.userAvatar)
            }

        }
    }
}

