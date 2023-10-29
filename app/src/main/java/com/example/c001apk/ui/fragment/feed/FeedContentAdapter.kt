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
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.ImagePreview
import cc.shinichi.library.bean.ImageInfo
import com.example.c001apk.R
import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.user.UserActivity
import com.example.c001apk.util.EmojiUtil
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.PubDateUtil
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.CenteredImageSpan
import com.example.c001apk.view.MyURLSpan
import com.example.c001apk.view.NineImageView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.util.regex.Pattern


class FeedContentAdapter(
    private val mContext: Context,
    private val feedList: List<FeedContentResponse>,
    private var replyList: ArrayList<TotalReplyResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_CONTENT = 0
    private val TYPE_FOOTER = 1
    private val TYPE_REPLY = 2
    private var loadState = 2
    val LOADING = 1
    val LOADING_COMPLETE = 2
    val LOADING_END = 3

    @SuppressLint("NotifyDataSetChanged")
    fun setLoadState(loadState: Int) {
        this.loadState = loadState
        notifyDataSetChanged()
    }

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
        val multiImage : NineImageView = view.findViewById(R.id.multiImage)
        val like: TextView = view.findViewById(R.id.like)
        val reply: TextView = view.findViewById(R.id.reply)
        val follow: Button = view.findViewById(R.id.follow)
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
        val multiImage : NineImageView = view.findViewById(R.id.multiImage)
        var id = ""
        var uid = ""
    }

    class FootViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerLayout: FrameLayout = view.findViewById(R.id.footerLayout)
        val indicator: CircularProgressIndicator = view.findViewById(R.id.indicator)
        val noMore: TextView = view.findViewById(R.id.noMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CONTENT -> {
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

            TYPE_FOOTER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_rv_footer, parent, false)
                FootViewHolder(view)
            }

            else -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_feed_content_reply_item, parent, false)
                val viewHolder = FeedContentReplyViewHolder(view)
                viewHolder.totalReply.setOnClickListener {
                    iOnTotalReplyClickListener?.onShowTotalReply(
                        viewHolder.adapterPosition,
                        viewHolder.uid,
                        viewHolder.id
                    )
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
                viewHolder.itemView.setOnLongClickListener {
                    val intent = Intent(parent.context, CopyActivity::class.java)
                    intent.putExtra("text", viewHolder.message.text.toString())
                    parent.context.startActivity(intent)
                    true
                }
                viewHolder.message.setOnClickListener {
                    IOnReplyClickContainer.controller?.onReply2Reply(
                        viewHolder.adapterPosition,
                        null,
                        viewHolder.id,
                        viewHolder.uid,
                        viewHolder.uname.text.toString(),
                        "reply"
                    )
                }
                viewHolder.itemView.setOnClickListener {
                    IOnReplyClickContainer.controller?.onReply2Reply(
                        viewHolder.adapterPosition,
                        null,
                        viewHolder.id,
                        viewHolder.uid,
                        viewHolder.uname.text.toString(),
                        "reply"
                    )
                }
                viewHolder
            }
        }
    }

    override fun getItemCount() = replyList.size + 2

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

            is FeedContentViewHolder -> {
                if (feedList.isNotEmpty()) {
                    val feed = feedList[position]
                    holder.uname.text = feed.data.username
                    if (feed.data.userAction.followAuthor == 1) {
                        holder.follow.text = "已关注"
                    } else {
                        holder.follow.text = "关注"
                    }
                    holder.follow.visibility = View.VISIBLE
                    if (feed.data.deviceTitle != "") {
                        holder.device.text = feed.data.deviceTitle
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

                    holder.message.movementMethod = LinkMovementMethod.getInstance()
                    holder.message.text = SpannableStringBuilderUtil.setText(mContext, feed.data.message, (holder.message.textSize*1.3).toInt())

                    if (!feed.data.picArr.isNullOrEmpty()) {
                        holder.multiImage.visibility = View.VISIBLE
                        val imageUrls= ArrayList<String>()
                        for (element in feed.data.picArr)
                            imageUrls.add(element)
                        holder.multiImage.setImageUrls(imageUrls)

                        val urlList: MutableList<ImageInfo> = ArrayList()
                        for (element in feed.data.picArr) {
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

                    ImageShowUtil.showAvatar(holder.avatar, feed.data.userAvatar)
                }
            }

            is FeedContentReplyViewHolder -> {
                if (replyList.isNotEmpty()) {
                    val reply = replyList[position - 1]
                    holder.id = reply.id
                    holder.uid = reply.uid
                    holder.uname.text = reply.username

                    holder.message.movementMethod = LinkMovementMethod.getInstance()
                    holder.message.text = SpannableStringBuilderUtil.setText(mContext, reply.message, (holder.message.textSize*1.3).toInt())

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
                        val mAdapter =
                            Reply2ReplyAdapter(mContext, reply.uid, position, reply.replyRows)
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

                    if (!reply.picArr.isNullOrEmpty()) {
                        holder.multiImage.visibility = View.VISIBLE
                        val imageUrls= ArrayList<String>()
                        for (element in reply.picArr)
                            imageUrls.add(element)
                        holder.multiImage.setImageUrls(imageUrls)

                        val urlList: MutableList<ImageInfo> = ArrayList()
                        for (element in reply.picArr) {
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
                }
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_CONTENT
            itemCount - 1 -> TYPE_FOOTER
            else -> TYPE_REPLY
        }
    }

}