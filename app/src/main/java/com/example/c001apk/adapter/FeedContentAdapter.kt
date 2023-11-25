package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.ThemeUtils
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnListTypeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnReplyClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTotalReplyClickListener
import com.example.c001apk.ui.fragment.minterface.OnPostFollowListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.LinearAdapterLayout
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator


class FeedContentAdapter(
    private val mContext: Context,
    private val feedList: List<FeedContentResponse>,
    private var replyList: ArrayList<TotalReplyResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), PopupMenu.OnMenuItemClickListener {


    private var haveTop = false

    fun setHaveTop(haveTop: Boolean) {
        this.haveTop = haveTop
    }


    private var uid = ""
    private var id = ""
    private var position = -1

    private var onPostFollowListener: OnPostFollowListener? = null

    fun setOnPostFollowListener(onPostFollowListener: OnPostFollowListener) {
        this.onPostFollowListener = onPostFollowListener
    }

    private var iOnListTypeClickListener: IOnListTypeClickListener? = null

    fun setIOnListTypeClickListener(iOnListTypeClickListener: IOnListTypeClickListener) {
        this.iOnListTypeClickListener = iOnListTypeClickListener
    }

    private var listType = "lastupdate_desc"

    fun setListType(listType: String) {
        this.listType = listType
    }

    private var onImageItemClickListener: OnImageItemClickListener? = null

    fun setOnImageItemClickListener(onImageItemClickListener: OnImageItemClickListener) {
        this.onImageItemClickListener = onImageItemClickListener
    }

    private val TYPE_CONTENT = 0
    private val TYPE_FOOTER = 1
    private val TYPE_REPLY = 2
    private val TYPE_FIX = 3
    private var loadState = 2
    val LOADING = 1
    val LOADING_COMPLETE = 2
    val LOADING_END = 3
    val LOADING_ERROR = 4
    private var errorMessage: String? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setLoadState(loadState: Int, errorMessage: String?) {
        this.loadState = loadState
        this.errorMessage = errorMessage
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
        var id = ""
        var isLike = false
        val follow: TextView = view.findViewById(R.id.follow)
        var isFollow = false
        var uid = ""
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
        val expand: ImageButton = view.findViewById(R.id.expand)
        var id = ""
        var uid = ""
        var isLike = false
    }

    class FootViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerLayout: FrameLayout = view.findViewById(R.id.footerLayout)
        val indicator: CircularProgressIndicator = view.findViewById(R.id.indicator)
        val noMore: TextView = view.findViewById(R.id.noMore)
    }

    class TopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val replyCount: TextView = view.findViewById(R.id.replyCount)
        val buttonToggle: MaterialButtonToggleGroup = view.findViewById(R.id.buttonToggle)
        val lastUpdate: Button = view.findViewById(R.id.lastUpdate)
        val dateLine: Button = view.findViewById(R.id.dateLine)
        val popular: Button = view.findViewById(R.id.popular)
        val author: Button = view.findViewById(R.id.author)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            TYPE_FIX -> {
                val view: View =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_top, parent, false)
                val viewHolder = TopViewHolder(view)
                viewHolder.lastUpdate.setOnClickListener {
                    iOnListTypeClickListener?.onRefreshReply("lastupdate_desc")
                }
                viewHolder.dateLine.setOnClickListener {
                    iOnListTypeClickListener?.onRefreshReply("dateline_desc")
                }
                viewHolder.popular.setOnClickListener {
                    iOnListTypeClickListener?.onRefreshReply("popular")
                }
                viewHolder.author.setOnClickListener {
                    iOnListTypeClickListener?.onRefreshReply("")
                }
                viewHolder
            }

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
                    intent.putExtra("id", viewHolder.uid)
                    parent.context.startActivity(intent)
                }
                viewHolder.uname.setOnClickListener {
                    val intent = Intent(parent.context, UserActivity::class.java)
                    intent.putExtra("id", viewHolder.uid)
                    parent.context.startActivity(intent)
                }
                viewHolder.like.setOnClickListener {
                    if (PrefManager.isLogin) {
                        if (PrefManager.SZLMID == "") {
                            Toast.makeText(mContext, "数字联盟ID不能为空", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            iOnLikeClickListener?.onPostLike(
                                "feed",
                                viewHolder.isLike,
                                viewHolder.id,
                                null
                            )
                        }
                    }
                }
                viewHolder.multiImage.apply {
                    onImageItemClickListener = this@FeedContentAdapter.onImageItemClickListener
                }
                viewHolder.follow.setOnClickListener {
                    onPostFollowListener?.onPostFollow(
                        viewHolder.isFollow,
                        viewHolder.uid,
                        0
                    )
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
                        viewHolder.bindingAdapterPosition,
                        viewHolder.uid,
                        viewHolder.id
                    )
                }
                viewHolder.avatar.setOnClickListener {
                    val intent = Intent(parent.context, UserActivity::class.java)
                    intent.putExtra("id", viewHolder.uid)
                    parent.context.startActivity(intent)
                }
                viewHolder.uname.setOnClickListener {
                    val intent = Intent(parent.context, UserActivity::class.java)
                    intent.putExtra("id", viewHolder.uid)
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
                        viewHolder.bindingAdapterPosition,
                        null,
                        viewHolder.id,
                        viewHolder.uid,
                        viewHolder.uname.text.toString(),
                        "reply"
                    )
                }
                viewHolder.itemView.setOnClickListener {
                    iOnReplyClickContainer?.onReply2Reply(
                        viewHolder.bindingAdapterPosition,
                        null,
                        viewHolder.id,
                        viewHolder.uid,
                        viewHolder.uname.text.toString(),
                        "reply"
                    )
                }
                viewHolder.like.setOnClickListener {
                    if (PrefManager.isLogin) {
                        if (PrefManager.SZLMID == "") {
                            Toast.makeText(mContext, "数字联盟ID不能为空", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            iOnLikeClickListener?.onPostLike(
                                "reply",
                                viewHolder.isLike,
                                viewHolder.id,
                                viewHolder.bindingAdapterPosition - 1
                            )
                        }
                    }
                }
                viewHolder.multiImage.apply {
                    onImageItemClickListener = this@FeedContentAdapter.onImageItemClickListener
                }
                viewHolder.expand.setOnClickListener {
                    id = viewHolder.id
                    uid = viewHolder.uid
                    position = viewHolder.bindingAdapterPosition
                    val popup = PopupMenu(mContext, it)
                    val inflater = popup.menuInflater
                    inflater.inflate(R.menu.feed_reply_menu, popup.menu)
                    popup.setOnMenuItemClickListener(this@FeedContentAdapter)
                    popup.show()
                }
                viewHolder
            }

            else -> throw IllegalArgumentException("type error")
        }
    }

    override fun getItemCount() = replyList.size + 3

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n", "RestrictedApi")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {

            is TopViewHolder -> {
                if (feedList.isNotEmpty()) {
                    holder.replyCount.text = "共${feedList[0].data?.replynum}回复"
                    when (listType) {
                        "lastupdate_desc" -> holder.buttonToggle.check(R.id.lastUpdate)
                        "dateline_desc" -> holder.buttonToggle.check(R.id.dateLine)
                        "popular" -> holder.buttonToggle.check(R.id.popular)
                        "" -> holder.buttonToggle.check(R.id.author)
                    }
                }
            }

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

                    LOADING_ERROR -> {
                        holder.footerLayout.visibility = View.VISIBLE
                        holder.indicator.visibility = View.GONE
                        holder.indicator.isIndeterminate = false
                        holder.noMore.text = errorMessage
                        holder.noMore.visibility = View.VISIBLE
                    }

                    else -> {}
                }
            }

            is FeedContentViewHolder -> {
                if (feedList.isNotEmpty()) {
                    val feed = feedList[position]
                    holder.id = feed.data?.id.toString()
                    holder.uid = feed.data?.uid.toString()
                    holder.isLike = feed.data?.userAction?.like == 1
                    holder.uname.text = feed.data?.userInfo?.username
                    ImageUtil.showAvatar(holder.avatar, feed.data?.userAvatar)
                    holder.isFollow = feed.data?.userAction?.followAuthor == 1
                    if (feed.data?.userAction?.followAuthor == 0) {
                        holder.follow.text = "关注"
                        holder.follow.setTextColor(
                            ThemeUtils.getThemeAttrColor(
                                mContext,
                                rikka.preference.simplemenu.R.attr.colorPrimary
                            )
                        )
                    } else {
                        holder.follow.text = "已关注"
                        holder.follow.setTextColor(mContext.getColor(android.R.color.darker_gray))
                    }
                    if (feed.data?.deviceTitle != "") {
                        holder.device.text = feed.data?.deviceTitle
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

                    holder.pubDate.text = DateUtils.fromToday(feed.data?.dateline)
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
                    if (feed.data?.userAction?.like == 1) {
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
                    holder.like.text = feed.data?.likenum
                    holder.like.setCompoundDrawables(drawableLike, null, null, null)

                    holder.reply.text = feed.data?.replynum
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
                        feed.data?.message.toString(),
                        (holder.message.textSize * 1.3).toInt(),
                        null
                    )

                    if (!feed.data?.picArr.isNullOrEmpty()) {
                        holder.multiImage.visibility = View.VISIBLE
                        if (feed.data?.picArr?.size == 1) {
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
                            if (PrefManager.isFullImageQuality) {
                                setUrlList(feed.data?.picArr)
                            } else {
                                for (element in feed.data?.picArr!!)
                                    if (element.substring(
                                            element.length - 3,
                                            element.length
                                        ) != "gif"
                                    )
                                        urlList.add("$element.s.jpg")
                                    else urlList.add(element)
                                setUrlList(urlList)
                            }
                        }
                    } else {
                        holder.multiImage.visibility = View.GONE
                    }
                }
            }

            is FeedContentReplyViewHolder -> {
                if (replyList.isNotEmpty()) {
                    val reply = replyList[position - 2]
                    holder.isLike = reply.userAction?.like == 1
                    holder.id = reply.id
                    holder.uid = reply.uid

                    val unameTag =
                        when (reply.uid) {
                            reply.feedUid -> " [楼主]"
                            else -> ""
                        }
                    val replyTag =
                        when (haveTop) {
                            true -> {
                                if (position == 2)
                                    " [置顶]"
                                else ""
                            }
                            else -> ""
                        }
                    holder.uname.text = SpannableStringBuilderUtil.setReply(
                        mContext,
                        "${reply.username}$unameTag$replyTag",
                        holder.uname.textSize.toInt(),
                        null
                    )

                    ImageUtil.showAvatar(holder.avatar, reply.userAvatar)

                    holder.message.movementMethod = LinkMovementMethod.getInstance()
                    holder.message.text = SpannableStringBuilderUtil.setText(
                        mContext,
                        reply.message,
                        (holder.message.textSize * 1.3).toInt(),
                        null
                    )

                    holder.pubDate.text = DateUtils.fromToday(reply.dateline)
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
                        DrawableCompat.setTint(
                            drawableLike,
                            mContext.getColor(android.R.color.darker_gray)
                        )
                        holder.like.setTextColor(mContext.getColor(android.R.color.darker_gray))
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
                        val sortedList = ArrayList<HomeFeedResponse.ReplyRows>()
                        for (element in reply.replyRows) {
                            if (!BlackListUtil.checkUid(element.uid))
                                sortedList.add(element)
                        }
                        if (sortedList.isNotEmpty()) {
                            holder.replyLayout.visibility = View.VISIBLE
                            holder.linearAdapterLayout.adapter = object : BaseAdapter() {
                                override fun getCount(): Int = sortedList.size
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

                                    val replyData = sortedList[position1]
                                    val textView: TextView = view.findViewById(R.id.reply)
                                    //textView.highlightColor = Color.TRANSPARENT
                                    textView.highlightColor = ColorUtils.setAlphaComponent(
                                        ThemeUtils.getThemeAttrColor(
                                            mContext,
                                            rikka.preference.simplemenu.R.attr.colorPrimaryDark
                                        ), 128
                                    )

                                    val replyTag =
                                        when (replyData.uid) {
                                            replyData.feedUid -> " [楼主] "
                                            reply.uid -> " [层主] "
                                            else -> ""
                                        }

                                    val rReplyTag =
                                        when (replyData.ruid) {
                                            replyData.feedUid -> " [楼主] "
                                            reply.uid -> " [层主] "
                                            else -> ""
                                        }

                                    val rReplyUser =
                                        when (replyData.ruid) {
                                            reply.uid -> ""
                                            else -> """<a class="feed-link-uname" href="/u/${replyData.rusername}">${replyData.rusername}${rReplyTag}</a>"""
                                        }

                                    val replyPic =
                                        when (replyData.pic) {
                                            "" -> ""
                                            else -> """ <a class=\"feed-forward-pic\" href=${replyData.pic}>查看图片(${replyData.picArr?.size})</a>"""
                                        }

                                    val mess =
                                        """<a class="feed-link-uname" href="/u/${replyData.uid}">${replyData.username}${replyTag}</a>回复${rReplyUser}: ${replyData.message}${replyPic}"""

                                    textView.movementMethod = LinkMovementMethod.getInstance()

                                    textView.text = SpannableStringBuilderUtil.setReply(
                                        mContext,
                                        mess,
                                        textView.textSize.toInt(),
                                        replyData.picArr
                                    )

                                    SpannableStringBuilderUtil.setData(position1, replyData.uid)

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
                                        val message = Html.fromHtml(
                                            replyData.message,
                                            Html.FROM_HTML_MODE_COMPACT
                                        )
                                        val intent = Intent(mContext, CopyActivity::class.java)
                                        intent.putExtra("text", message.toString())
                                        mContext.startActivity(intent)
                                        true
                                    }

                                    return view
                                }
                            }
                        } else holder.replyLayout.visibility = View.GONE
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
                            if (PrefManager.isFullImageQuality) {
                                setUrlList(reply.picArr)
                            } else {
                                for (element in reply.picArr)
                                    if (element.substring(
                                            element.length - 3,
                                            element.length
                                        ) != "gif"
                                    )
                                        urlList.add("$element.s.jpg")
                                    else urlList.add(element)
                                setUrlList(urlList)
                            }
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
            1 -> TYPE_FIX
            itemCount - 1 -> TYPE_FOOTER
            else -> TYPE_REPLY
        }
    }

    override fun onMenuItemClick(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.block -> {
                BlackListUtil.saveUid(uid)
                replyList.removeAt(position - 2)
                notifyItemRemoved(position)
            }

            R.id.report -> {
                val intent = Intent(mContext, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed_reply&id=$id"
                )
                mContext.startActivity(intent)
            }
        }
        return false
    }

}