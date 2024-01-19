package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.ThemeUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.c001apk.R
import com.example.c001apk.logic.model.MessageResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.FeedActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.LinkTextView
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView


class MessageContentAdapter(
    private val mContext: Context,
    private val type: String,
    private val dataList: List<MessageResponse.Data>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var appListener: AppListener? = null

    fun setAppListener(appListener: AppListener) {
        this.appListener = appListener
    }

    private var loadState = 2
    val LOADING = 1
    val LOADING_COMPLETE = 2
    val LOADING_END = 3
    val LOADING_ERROR = 4
    private var errorMessage: String? = null

    fun setLoadState(loadState: Int, errorMessage: String?) {
        this.loadState = loadState
        this.errorMessage = errorMessage
    }

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        var id = ""
        var uid = ""
        var entityType = ""
        var isLike = false
        val message: LinkTextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
        val reply: TextView = view.findViewById(R.id.reply)
        val device: TextView = view.findViewById(R.id.device)
        val multiImage: NineGridImageView = view.findViewById(R.id.multiImage)
        val forward: MaterialCardView = view.findViewById(R.id.forward)
        var forwardEntityType = ""
        var forwardId = ""
        var forwardUid = ""
        var forwardUname = ""
        val forwardTitle: LinkTextView = view.findViewById(R.id.forwardTitle)
        val forwardMessage: LinkTextView = view.findViewById(R.id.forwardMessage)
        val forward1: MaterialCardView = view.findViewById(R.id.forward1)
        var forwardId1 = ""
        var forwardUid1 = ""
        var forwardUname1: TextView = view.findViewById(R.id.forwardUname1)
        val forwardMessage1: TextView = view.findViewById(R.id.forwardMessage1)
        val forward1Pic: ShapeableImageView = view.findViewById(R.id.forward1Pic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_content, parent, false)
                val viewHolder = MessageViewHolder(view)
                viewHolder.uname.setOnClickListener {
                    IntentUtil.startActivity<UserActivity>(parent.context) {
                        putExtra("id", viewHolder.uid)
                    }
                }
                viewHolder.avatar.setOnClickListener {
                    IntentUtil.startActivity<UserActivity>(parent.context) {
                        putExtra("id", viewHolder.uid)
                    }
                }
                viewHolder.itemView.setOnLongClickListener {
                    IntentUtil.startActivity<CopyActivity>(parent.context) {
                        putExtra("text", viewHolder.message.text.toString())
                    }
                    true
                }
                viewHolder.itemView.setOnClickListener {
                    if (viewHolder.entityType == "feed") {
                        IntentUtil.startActivity<FeedActivity>(parent.context) {
                            putExtra("id", viewHolder.id)
                        }
                    }
                }
                viewHolder.forward.setOnClickListener {
                    if (viewHolder.forwardEntityType == "feed") {
                        IntentUtil.startActivity<FeedActivity>(parent.context) {
                            putExtra("id", viewHolder.forwardId)
                        }
                    }
                }
                viewHolder.forwardMessage.setOnClickListener {
                    if (viewHolder.forwardEntityType == "feed") {
                        IntentUtil.startActivity<FeedActivity>(parent.context) {
                            putExtra("id", viewHolder.forwardId)
                        }
                    }
                }
                viewHolder.forwardMessage.setOnLongClickListener {
                    IntentUtil.startActivity<CopyActivity>(parent.context) {
                        putExtra("text", viewHolder.forwardMessage.text.toString())
                    }
                    true
                }
                viewHolder.like.setOnClickListener {
                    if (PrefManager.isLogin) {
                        appListener?.onPostLike(
                            null,
                            viewHolder.isLike,
                            viewHolder.id,
                            viewHolder.bindingAdapterPosition
                        )
                    }
                }
                viewHolder.multiImage.apply {
                    appListener = this@MessageContentAdapter.appListener
                }
                viewHolder.forward1.setOnClickListener {
                    IntentUtil.startActivity<FeedActivity>(parent.context) {
                        putExtra("id", viewHolder.forwardId1)
                    }
                }
                viewHolder
            }

            -1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_rv_footer, parent, false)
                val viewHolder = AppAdapter.FootViewHolder(view)
                viewHolder.retry.setOnClickListener {
                    appListener?.onReload()
                }
                viewHolder
            }

            1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_user, parent, false)
                val viewHolder = AppAdapter.UserViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    IntentUtil.startActivity<UserActivity>(parent.context) {
                        putExtra("id", viewHolder.uid)
                    }
                }
                viewHolder
            }

            else -> throw IllegalArgumentException("invalid type")
        }

    }

    override fun getItemCount() = dataList.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) -1
        else when (type) {
            "atMe" -> 0
            "atCommentMe" -> 0
            "feedLike" -> 0
            "contactsFollow" -> 1
            "list" -> 1
            else -> throw IllegalArgumentException("invalid type")
        }
    }

    @SuppressLint("SetTextI18n", "RestrictedApi", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {

            is AppAdapter.UserViewHolder -> {
                val message = dataList[position]
                holder.uid = message.fromuid
                holder.uname.text = message.fromusername
                holder.follow.text = DateUtils.fromToday(message.dateline)
                if (type == "contactsFollow")
                    holder.fans.text = "关注了你"
                ImageUtil.showIMG(holder.avatar, message.fromUserAvatar)
            }

            is AppAdapter.FootViewHolder -> {
                val lp = holder.itemView.layoutParams
                if (lp is StaggeredGridLayoutManager.LayoutParams) {
                    lp.isFullSpan = true
                }

                when (loadState) {
                    LOADING -> {
                        holder.footerLayout.visibility = View.VISIBLE
                        holder.indicator.visibility = View.VISIBLE
                        holder.indicator.isIndeterminate = true
                        holder.noMore.visibility = View.GONE
                        holder.retry.visibility = View.GONE

                    }

                    LOADING_COMPLETE -> {
                        holder.footerLayout.visibility = View.GONE
                        holder.indicator.visibility = View.GONE
                        holder.indicator.isIndeterminate = false
                        holder.noMore.visibility = View.GONE
                        holder.retry.visibility = View.GONE
                    }

                    LOADING_END -> {
                        holder.footerLayout.visibility = View.VISIBLE
                        holder.indicator.visibility = View.GONE
                        holder.indicator.isIndeterminate = false
                        holder.noMore.visibility = View.VISIBLE
                        holder.noMore.text = mContext.getString(R.string.no_more)
                        holder.retry.visibility = View.GONE
                    }

                    LOADING_ERROR -> {
                        holder.footerLayout.visibility = View.VISIBLE
                        holder.indicator.visibility = View.GONE
                        holder.indicator.isIndeterminate = false
                        holder.noMore.text = errorMessage
                        holder.noMore.visibility = View.VISIBLE
                        holder.retry.visibility = View.VISIBLE
                    }

                    else -> {}
                }

            }

            is MessageViewHolder -> {
                val message = dataList[position]
                if (type == "atMe" || type == "atCommentMe") {
                    holder.id = message.id
                    holder.uid = message.uid
                    holder.entityType = message.entityType
                    holder.uname.text = message.username
                    ImageUtil.showIMG(holder.avatar, message.userAvatar)
                    if (message.deviceTitle != "") {
                        holder.device.text = message.deviceTitle
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
                        holder.device.visibility = View.INVISIBLE
                    }
                    holder.pubDate.text = DateUtils.fromToday(message.dateline)
                    val drawable1: Drawable = mContext.getDrawable(R.drawable.ic_date)!!
                    drawable1.setBounds(
                        0,
                        0,
                        holder.pubDate.textSize.toInt(),
                        holder.pubDate.textSize.toInt()
                    )
                    holder.pubDate.setCompoundDrawables(drawable1, null, null, null)

                    val drawableLike: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
                    drawableLike.setBounds(
                        0,
                        0,
                        holder.like.textSize.toInt(),
                        holder.like.textSize.toInt()
                    )
                    if (message.userAction?.like == 1) {
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
                        DrawableCompat.setTint(
                            drawableLike,
                            mContext.getColor(android.R.color.darker_gray)
                        )
                        holder.like.setTextColor(mContext.getColor(android.R.color.darker_gray))
                    }
                    holder.like.text = message.likenum
                    holder.like.setCompoundDrawables(drawableLike, null, null, null)

                    holder.reply.text = message.replynum
                    val drawableReply: Drawable = mContext.getDrawable(R.drawable.ic_message)!!
                    drawableReply.setBounds(
                        0,
                        0,
                        holder.like.textSize.toInt(),
                        holder.like.textSize.toInt()
                    )
                    holder.reply.setCompoundDrawables(drawableReply, null, null, null)

                    holder.message.movementMethod =
                        LinkTextView.LocalLinkMovementMethod.getInstance()
                    holder.message.text = SpannableStringBuilderUtil.setText(
                        mContext,
                        message.message,
                        (holder.message.textSize * 1.3).toInt(),
                        null
                    )
                } else if (type == "feedLike") {
                    holder.entityType = message.entityType
                    holder.uname.text = message.likeUsername
                    holder.uid = message.likeUid
                    ImageUtil.showIMG(holder.avatar, message.likeAvatar)
                    holder.pubDate.text = DateUtils.fromToday(message.likeTime)
                    val drawable1: Drawable = mContext.getDrawable(R.drawable.ic_date)!!
                    drawable1.setBounds(
                        0,
                        0,
                        holder.pubDate.textSize.toInt(),
                        holder.pubDate.textSize.toInt()
                    )
                    holder.pubDate.setCompoundDrawables(drawable1, null, null, null)
                    holder.message.text = "赞了你的${message.infoHtml}"
                }


                if (message.forwardSourceFeed != null) {
                    holder.forward.visibility = View.VISIBLE
                    holder.forwardEntityType = message.forwardSourceFeed.entityType
                    holder.forwardId = message.forwardSourceFeed.id
                    holder.forwardUid = message.forwardSourceFeed.uid
                    holder.forwardUname = message.forwardSourceFeed.username
                    val title =
                        """<a class="feed-link-uname" href="/u/${message.forwardSourceFeed.uid}">@${message.forwardSourceFeed.username}: </a>${message.forwardSourceFeed.messageTitle}"""
                    holder.forwardTitle.movementMethod =
                        LinkTextView.LocalLinkMovementMethod.getInstance()
                    holder.forwardTitle.text = SpannableStringBuilderUtil.setText(
                        mContext,
                        title,
                        (holder.forwardMessage.textSize * 1.3).toInt(),
                        null
                    )
                    holder.forwardMessage.movementMethod =
                        LinkTextView.LocalLinkMovementMethod.getInstance()
                    holder.forwardMessage.text = SpannableStringBuilderUtil.setText(
                        mContext,
                        message.forwardSourceFeed.message,
                        (holder.forwardMessage.textSize * 1.3).toInt(),
                        null
                    )
                    if (!message.forwardSourceFeed.picArr.isNullOrEmpty()) {
                        holder.multiImage.visibility = View.VISIBLE
                        if (message.forwardSourceFeed.picArr.size == 1) {
                            val from = message.forwardSourceFeed.pic.lastIndexOf("@")
                            val middle = message.forwardSourceFeed.pic.lastIndexOf("x")
                            val end = message.forwardSourceFeed.pic.lastIndexOf(".")
                            if (from != -1 && middle != -1 && end != -1) {
                                val width =
                                    message.forwardSourceFeed.pic.substring(from + 1, middle)
                                        .toInt()
                                val height =
                                    message.forwardSourceFeed.pic.substring(middle + 1, end).toInt()
                                holder.multiImage.imgHeight = height
                                holder.multiImage.imgWidth = width
                            }
                        }
                        holder.multiImage.apply {
                            val urlList: MutableList<String> = ArrayList()
                            for (element in message.forwardSourceFeed.picArr)
                                urlList.add("$element.s.jpg")
                            setUrlList(urlList)
                        }
                    } else {
                        holder.multiImage.visibility = View.GONE
                    }
                } else {
                    holder.forward.visibility = View.GONE
                }

                if (message.feed != null) {
                    holder.forward1.visibility = View.VISIBLE
                    if (type == "atCommentMe") {
                        holder.forwardId1 = message.feed.id
                        holder.forwardUid1 = message.feed.uid
                        holder.forwardUname1.text = "@${message.feed.username}"
                        holder.forwardMessage1.text = SpannableStringBuilderUtil.setText(
                            mContext,
                            message.feed.message,
                            (holder.forwardMessage.textSize * 1.3).toInt(),
                            null
                        )
                        if (message.feed.pic.isNullOrEmpty())
                            holder.forward1Pic.visibility = View.GONE
                        else
                            ImageUtil.showIMG(holder.forward1Pic, message.feed.pic)
                    } else if (type == "feedLike") {
                        holder.forwardId1 = message.fid
                        holder.forward1Pic.visibility = View.GONE
                        holder.forwardUname1.text = "@${message.username}"
                        holder.forwardMessage1.text = SpannableStringBuilderUtil.setText(
                            mContext,
                            message.message,
                            (holder.forwardMessage.textSize * 1.3).toInt(),
                            null
                        )
                    }
                } else {
                    holder.forward1.visibility = View.GONE
                }
            }
        }
    }

}