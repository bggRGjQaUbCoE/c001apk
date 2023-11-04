package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.ThemeUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnReplyClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTotalReplyClickListener
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.PubDateUtil
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.LinearAdapterLayout
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator


class FeedContentAdapter(
    private val mContext: Context,
    private val feedList: List<FeedContentResponse>,
    private var replyList: ArrayList<TotalReplyResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onImageItemClickListener: OnImageItemClickListener? = null

    fun setOnImageItemClickListener(onImageItemClickListener: OnImageItemClickListener) {
        this.onImageItemClickListener = onImageItemClickListener
    }

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

    private var iOnReplyClickContainer: IOnReplyClickListener? = null

    fun setIOnReplyClickListener(iOnLikeClickListener: IOnReplyClickListener) {
        this.iOnReplyClickContainer = iOnLikeClickListener
    }

    private var iOnLikeClickListener: IOnLikeClickListener? = null

    fun setIOnLikeReplyListener(iOnLikeClickListener: IOnLikeClickListener) {
        this.iOnLikeClickListener = iOnLikeClickListener
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
        val multiImage: NineGridImageView = view.findViewById(R.id.multiImage)
        val like: TextView = view.findViewById(R.id.like)
        val reply: TextView = view.findViewById(R.id.reply)
        val follow: Button = view.findViewById(R.id.follow)
        var id = ""
        var isLike = false
    }

    class FeedContentReplyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val reply: TextView = view.findViewById(R.id.reply)
        val replyLayout: MaterialCardView = view.findViewById(R.id.replyLayout)
        val totalReply: TextView = view.findViewById(R.id.totalReply)
        val multiImage: NineGridImageView = view.findViewById(R.id.multiImage)
        val linearAdapterLayout: LinearAdapterLayout = view.findViewById(R.id.linearAdapterLayout)
        var id = ""
        var uid = ""
        var isLike = false
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
                viewHolder.like.setOnClickListener {
                    if (PrefManager.isLogin) {
                        iOnLikeClickListener?.onPostLike(
                            "feed",
                            viewHolder.isLike,
                            viewHolder.id,
                            null
                        )
                    }
                }
                viewHolder.multiImage.apply {
                    onImageItemClickListener = this@FeedContentAdapter.onImageItemClickListener
                }
                viewHolder
            }

            TYPE_FOOTER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_rv_footer, parent, false)
                FootViewHolder(view)
            }

            TYPE_REPLY -> {
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
                    iOnReplyClickContainer?.onReply2Reply(
                        viewHolder.adapterPosition,
                        null,
                        viewHolder.id,
                        viewHolder.uid,
                        viewHolder.uname.text.toString(),
                        "reply"
                    )
                }
                viewHolder.itemView.setOnClickListener {
                    iOnReplyClickContainer?.onReply2Reply(
                        viewHolder.adapterPosition,
                        null,
                        viewHolder.id,
                        viewHolder.uid,
                        viewHolder.uname.text.toString(),
                        "reply"
                    )
                }
                viewHolder.like.setOnClickListener {
                    if (PrefManager.isLogin) {
                        iOnLikeClickListener?.onPostLike(
                            "reply",
                            viewHolder.isLike,
                            viewHolder.id,
                            viewHolder.adapterPosition - 1
                        )
                    }
                }
                viewHolder.multiImage.apply {
                    onImageItemClickListener = this@FeedContentAdapter.onImageItemClickListener
                }
                viewHolder
            }

            else -> throw IllegalArgumentException("type error")
        }
    }

    override fun getItemCount() = replyList.size + 2

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n", "RestrictedApi")
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
                    holder.id = feed.data.id
                    holder.isLike = feed.data.userAction?.like == 1
                    holder.uname.text = feed.data.username
                    ImageShowUtil.showAvatar(holder.avatar, feed.data.userAvatar)
                    if (feed.data.userAction?.followAuthor == 1) {
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
                    val drawableDate: Drawable = mContext.getDrawable(R.drawable.ic_date)!!
                    drawableDate.setBounds(
                        0,
                        0,
                        holder.pubDate.textSize.toInt(),
                        holder.pubDate.textSize.toInt()
                    )
                    holder.pubDate.setCompoundDrawables(drawableDate, null, null, null)


                    val drawableLike: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
                    drawableLike.setBounds(
                        0,
                        0,
                        holder.like.textSize.toInt(),
                        holder.like.textSize.toInt()
                    )
                    if (feed.data.userAction?.like == 1) {
                        DrawableCompat.setTint(
                            drawableLike,
                            ThemeUtils.getThemeAttrColor(
                                mContext,
                                rikka.preference.simplemenu.R.attr.colorPrimary
                            )
                        )
                        holder.like.setTextColor(
                            ThemeUtils.getThemeAttrColor(
                                mContext,
                                rikka.preference.simplemenu.R.attr.colorPrimary
                            )
                        )
                    } else {
                        DrawableCompat.setTint(drawableLike, mContext.getColor(R.color.gray_bd))
                        holder.like.setTextColor(mContext.getColor(R.color.gray_bd))
                    }
                    holder.like.text = feed.data.likenum
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
                    holder.message.text = SpannableStringBuilderUtil.setText(
                        mContext,
                        feed.data.message,
                        (holder.message.textSize * 1.3).toInt(),
                        null
                    )

                    if (!feed.data.picArr.isNullOrEmpty()) {
                        holder.multiImage.visibility = View.VISIBLE
                        if (feed.data.picArr.size == 1) {
                            val from = feed.data.pic.lastIndexOf("@")
                            val middle = feed.data.pic.lastIndexOf("x")
                            val end = feed.data.pic.lastIndexOf(".")
                            val width = feed.data.pic.substring(from + 1, middle).toInt()
                            val height = feed.data.pic.substring(middle + 1, end).toInt()
                            holder.multiImage.imgHeight = height
                            holder.multiImage.imgWidth = width
                        }
                        holder.multiImage.apply {
                            val urlList: MutableList<String> = ArrayList()
                            for (element in feed.data.picArr)
                                if (element.substring(element.length - 3, element.length) != "gif")
                                    urlList.add("$element.s.jpg")
                                else urlList.add(element)
                            setUrlList(urlList)
                        }
                    } else {
                        holder.multiImage.visibility = View.GONE
                    }
                }
            }

            is FeedContentReplyViewHolder -> {
                if (replyList.isNotEmpty()) {
                    val reply = replyList[position - 1]
                    holder.isLike = reply.userAction?.like == 1
                    holder.id = reply.id
                    holder.uid = reply.uid
                    holder.uname.text = reply.username
                    ImageShowUtil.showAvatar(holder.avatar, reply.userAvatar)

                    holder.message.movementMethod = LinkMovementMethod.getInstance()
                    holder.message.text = SpannableStringBuilderUtil.setText(
                        mContext,
                        reply.message,
                        (holder.message.textSize * 1.3).toInt(),
                        null
                    )

                    holder.pubDate.text = PubDateUtil.time(reply.dateline)
                    val drawableDate: Drawable = mContext.getDrawable(R.drawable.ic_date)!!
                    drawableDate.setBounds(
                        0,
                        0,
                        holder.pubDate.textSize.toInt(),
                        holder.pubDate.textSize.toInt()
                    )
                    holder.pubDate.setCompoundDrawables(drawableDate, null, null, null)

                    val drawableLike: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
                    drawableLike.setBounds(
                        0,
                        0,
                        holder.like.textSize.toInt(),
                        holder.like.textSize.toInt()
                    )
                    if (reply.userAction?.like == 1) {
                        DrawableCompat.setTint(
                            drawableLike,
                            ThemeUtils.getThemeAttrColor(
                                mContext,
                                rikka.preference.simplemenu.R.attr.colorPrimary
                            )
                        )
                        holder.like.setTextColor(
                            ThemeUtils.getThemeAttrColor(
                                mContext,
                                rikka.preference.simplemenu.R.attr.colorPrimary
                            )
                        )
                    } else {
                        DrawableCompat.setTint(drawableLike, mContext.getColor(R.color.gray_bd))
                        holder.like.setTextColor(mContext.getColor(R.color.gray_bd))
                    }
                    holder.like.text = reply.likenum
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

                    if (reply.replyRows.isNotEmpty()) {
                        holder.replyLayout.visibility = View.VISIBLE
                        holder.linearAdapterLayout.adapter = object : BaseAdapter() {
                            override fun getCount(): Int = reply.replyRows.size
                            override fun getItem(p0: Int): Any = 0
                            override fun getItemId(p0: Int): Long = 0
                            override fun getView(
                                position1: Int,
                                convertView: View?,
                                parent: ViewGroup?
                            ): View {
                                val view = LayoutInflater.from(mContext).inflate(
                                    R.layout.item_feed_content_reply_to_reply_item,
                                    parent,
                                    false
                                )
                                val replyData = reply.replyRows[position1]
                                val textView: TextView = view.findViewById(R.id.reply)
                                textView.highlightColor = Color.TRANSPARENT

                                val text =
                                    if (replyData.ruid == reply.uid) {
                                        if (replyData.pic == "")
                                            """<a class="feed-link-uname" href="/u/${replyData.username}">${replyData.username}</a>: ${replyData.message}"""
                                        else {
                                            if (replyData.message == "[图片]")
                                                """<a class="feed-link-uname" href="/u/${replyData.username}">${replyData.username}</a>: ${replyData.message} <a class=\"feed-forward-pic\" href=${replyData.pic}> 查看图片(${replyData.picArr?.size})</a> """
                                            else
                                                """<a class="feed-link-uname" href="/u/${replyData.username}">${replyData.username}</a>: ${replyData.message} <a class=\"feed-forward-pic\" href=${replyData.pic}> [图片] 查看图片(${replyData.picArr?.size})</a> """
                                        }
                                    } else {
                                        if (replyData.pic == "")
                                            """<a class="feed-link-uname" href="/u/${replyData.username}">${replyData.username}</a>回复<a class="feed-link-uname" href="/u/${replyData.rusername}">${replyData.rusername}</a>: ${replyData.message}"""
                                        else {
                                            if (replyData.message == "[图片]")
                                                """<a class="feed-link-uname" href="/u/${replyData.username}">${replyData.username}</a>回复<a class="feed-link-uname" href="/u/${replyData.rusername}">${replyData.rusername}</a>: ${replyData.message} <a class=\"feed-forward-pic\" href=${replyData.pic}> 查看图片(${replyData.picArr?.size})</a> """
                                            else
                                                """<a class="feed-link-uname" href="/u/${replyData.username}">${replyData.username}</a>回复<a class="feed-link-uname" href="/u/${replyData.rusername}">${replyData.rusername}</a>: ${replyData.message} <a class=\"feed-forward-pic\" href=${replyData.pic}> [图片] 查看图片(${replyData.picArr?.size})</a> """
                                        }
                                    }

                                textView.movementMethod = LinkMovementMethod.getInstance()
                                textView.text = SpannableStringBuilderUtil.setText(
                                    mContext,
                                    text,
                                    (textView.textSize * 1.3).toInt(),
                                    replyData.picArr
                                )

                                view.setOnClickListener {
                                    iOnReplyClickContainer?.onReply2Reply(
                                        holder.bindingAdapterPosition,
                                        null,
                                        replyData.id,
                                        replyData.uid,
                                        replyData.username,
                                        "reply"
                                    )
                                }

                                view.setOnLongClickListener {
                                    val intent = Intent(mContext, CopyActivity::class.java)
                                    intent.putExtra("text", replyData.message)
                                    mContext.startActivity(intent)
                                    true
                                }

                                return view
                            }
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
                        if (reply.picArr.size == 1) {
                            val from = reply.pic.lastIndexOf("@")
                            val middle = reply.pic.lastIndexOf("x")
                            val end = reply.pic.lastIndexOf(".")
                            val width = reply.pic.substring(from + 1, middle).toInt()
                            val height = reply.pic.substring(middle + 1, end).toInt()
                            holder.multiImage.imgHeight = height
                            holder.multiImage.imgWidth = width
                        }
                        holder.multiImage.apply {
                            val urlList: MutableList<String> = ArrayList()
                            for (element in reply.picArr)
                                if (element.substring(element.length - 3, element.length) != "gif")
                                    urlList.add("$element.s.jpg")
                                else urlList.add(element)
                            setUrlList(urlList)
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