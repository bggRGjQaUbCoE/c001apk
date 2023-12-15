package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.ThemeUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.c001apk.R
import com.example.c001apk.logic.model.FeedArticleContentBean
import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.activity.AppActivity
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.DyhActivity
import com.example.c001apk.ui.activity.TopicActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.LinearAdapterLayout
import com.example.c001apk.view.LinkMovementClickMethod
import com.example.c001apk.view.LinkTextView
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson


class FeedContentAdapter(
    private val mContext: Context,
    private val feedList: List<FeedContentResponse>,
    private var replyList: ArrayList<TotalReplyResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), PopupMenu.OnMenuItemClickListener {

    private var extraKey = ""

    fun setExtraKey(extraKey: String) {
        this.extraKey = extraKey
    }

    private var haveTop = false

    fun setHaveTop(haveTop: Boolean) {
        this.haveTop = haveTop
    }

    private var text = ""
    private var uid = ""
    private var id = ""
    private var ruid = ""
    private var position = -1
    private var rPosition: Int? = null

    private var listType = "lastupdate_desc"

    fun setListType(listType: String) {
        this.listType = listType
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
        // notifyDataSetChanged()
    }

    private var appListener: AppListener? = null

    fun setAppListener(appListener: AppListener) {
        this.appListener = appListener
    }

    class FeedContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val uname: LinkTextView = view.findViewById(R.id.uname)
        val device: TextView = view.findViewById(R.id.device)
        val messageTitle: TextView = view.findViewById(R.id.messageTitle)
        val message: LinkTextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val ip: TextView = view.findViewById(R.id.ip)
        val multiImage: NineGridImageView = view.findViewById(R.id.multiImage)
        val like: TextView = view.findViewById(R.id.like)
        val reply: TextView = view.findViewById(R.id.reply)
        var id = ""
        var isLike = false
        val follow: TextView = view.findViewById(R.id.follow)
        var isFollow = false
        var uid = ""
        val dyhLayout: HorizontalScrollView = view.findViewById(R.id.dyhLayout)
        val linearAdapterLayout: LinearAdapterLayout = view.findViewById(R.id.linearAdapterLayout)
        val articleMessage: LinearAdapterLayout = view.findViewById(R.id.articleMessage)
        val twoOptionsLayout: LinearLayout = view.findViewById(R.id.twoOptionsLayout)
        val leftOption: Button = view.findViewById(R.id.leftOption)
        val rightOption: Button = view.findViewById(R.id.rightOption)
        val voteOptions: LinearAdapterLayout = view.findViewById(R.id.voteOptions)
        val extraUrlLayout: ConstraintLayout = view.findViewById(R.id.extraUrlLayout)
        val extraTitle: TextView = view.findViewById(R.id.extraTitle)
        val extraUrl: TextView = view.findViewById(R.id.extraUrl)
    }

    class FeedContentReplyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var notiId = ""
        val uname: LinkTextView = view.findViewById(R.id.uname)
        val message: LinkTextView = view.findViewById(R.id.message)
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
        var rid = ""
        var uid = ""
        var isLike = false
        var type = ""
        var url = ""
        var vote: TextView = view.findViewById(R.id.vote)
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
                    appListener?.onRefreshReply("lastupdate_desc")
                }
                viewHolder.dateLine.setOnClickListener {
                    appListener?.onRefreshReply("dateline_desc")
                }
                viewHolder.popular.setOnClickListener {
                    appListener?.onRefreshReply("popular")
                }
                viewHolder.author.setOnClickListener {
                    appListener?.onRefreshReply("")
                }
                viewHolder
            }

            TYPE_CONTENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feed_content, parent, false)
                val viewHolder = FeedContentViewHolder(view)
                viewHolder.itemView.setOnLongClickListener {
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
                viewHolder.like.setOnClickListener {
                    if (PrefManager.isLogin) {
                        if (PrefManager.SZLMID == "") {
                            Toast.makeText(mContext, "数字联盟ID不能为空", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            appListener?.onPostLike(
                                "feed",
                                viewHolder.isLike,
                                viewHolder.id,
                                null
                            )
                        }
                    }
                }
                viewHolder.multiImage.apply {
                    appListener = this@FeedContentAdapter.appListener
                }
                viewHolder.follow.setOnClickListener {
                    appListener?.onPostFollow(
                        viewHolder.isFollow,
                        viewHolder.uid,
                        0
                    )
                }
                viewHolder.extraUrlLayout.setOnClickListener {
                    val url = viewHolder.extraUrl.text.toString()
                    if (url.startsWith("/game/")) {
                        val intent = Intent(parent.context, AppActivity::class.java)
                        intent.putExtra("id", url.replace("/game/", ""))
                        parent.context.startActivity(intent)
                    } else if (url.startsWith("/apk/")) {
                        val intent = Intent(parent.context, AppActivity::class.java)
                        intent.putExtra("id", url.replace("/apk/", ""))
                        parent.context.startActivity(intent)
                    } else if (PrefManager.isOpenLinkOutside) {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.data = Uri.parse(viewHolder.extraUrl.text.toString())
                        try {
                            parent.context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(parent.context, "打开失败", Toast.LENGTH_SHORT).show()
                            Log.w("error", "Activity was not found for intent, $intent")
                        }
                    } else {
                        val intent = Intent(parent.context, WebViewActivity::class.java)
                        intent.putExtra("url", viewHolder.extraUrl.text)
                        parent.context.startActivity(intent)
                    }
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
                    appListener?.onShowTotalReply(
                        viewHolder.bindingAdapterPosition,
                        viewHolder.uid,
                        viewHolder.id,
                        null
                    )
                }
                viewHolder.avatar.setOnClickListener {
                    val intent = Intent(parent.context, UserActivity::class.java)
                    intent.putExtra("id", viewHolder.uid)
                    parent.context.startActivity(intent)
                }
                viewHolder.itemView.setOnLongClickListener {
                    val intent = Intent(parent.context, CopyActivity::class.java)
                    intent.putExtra("text", viewHolder.message.text.toString())
                    parent.context.startActivity(intent)
                    true
                }
                viewHolder.itemView.setOnClickListener {
                    appListener?.onReply2Reply(
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
                            appListener?.onPostLike(
                                "reply",
                                viewHolder.isLike,
                                viewHolder.id,
                                viewHolder.bindingAdapterPosition - 1
                            )
                        }
                    }
                }
                viewHolder.multiImage.apply {
                    appListener = this@FeedContentAdapter.appListener
                }
                viewHolder.expand.setOnClickListener {
                    rPosition = null
                    id = viewHolder.id
                    uid = viewHolder.uid
                    ruid = viewHolder.uid
                    position = viewHolder.bindingAdapterPosition
                    val popup = PopupMenu(mContext, it)
                    val inflater = popup.menuInflater
                    inflater.inflate(R.menu.feed_reply_menu, popup.menu)
                    popup.menu.findItem(R.id.copy).isVisible = false
                    popup.menu.findItem(R.id.delete).isVisible = PrefManager.uid == viewHolder.uid
                    popup.setOnMenuItemClickListener(this@FeedContentAdapter)
                    popup.show()
                }
                viewHolder
            }

            else -> throw IllegalArgumentException("type error")
        }
    }

    override fun getItemCount() = replyList.size + 3

    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            when (getItemViewType(position)) {
                TYPE_CONTENT -> {
                    if (payloads[0] == "like") {
                        (holder as FeedContentViewHolder).like.text = feedList[0].data?.likenum
                        holder.isLike = feedList[0].data?.userAction?.like == 1
                        val drawableLike: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
                        drawableLike.setBounds(
                            0,
                            0,
                            holder.like.textSize.toInt(),
                            holder.like.textSize.toInt()
                        )
                        if (feedList[0].data?.userAction?.like == 1) {
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
                        holder.like.setCompoundDrawables(drawableLike, null, null, null)
                    }
                }

                TYPE_REPLY -> {
                    if (payloads[0] == "like") {
                        (holder as FeedContentReplyViewHolder).like.text =
                            replyList[position - 2].likenum
                        holder.isLike = replyList[position - 2].userAction?.like == 1
                        val drawableLike: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
                        drawableLike.setBounds(
                            0,
                            0,
                            holder.like.textSize.toInt(),
                            holder.like.textSize.toInt()
                        )
                        if (replyList[position - 2].userAction?.like == 1) {
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
                        holder.like.setCompoundDrawables(drawableLike, null, null, null)
                    }
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n", "RestrictedApi")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {

            is TopViewHolder -> {
                if (feedList.isNotEmpty()) {
                    val lp = holder.itemView.layoutParams
                    if (feedList[0].data!!.feedType == "vote"
                        && lp is StaggeredGridLayoutManager.LayoutParams
                    ) {
                        lp.isFullSpan = true
                        holder.replyCount.text = "观点"
                        holder.replyCount.textSize = 16f
                        holder.buttonToggle.visibility = View.GONE
                    } else if (feedList.isNotEmpty()) {
                        holder.replyCount.text = "共${feedList[0].data?.replynum}回复"
                        holder.buttonToggle.visibility = View.VISIBLE
                        when (listType) {
                            "lastupdate_desc" -> holder.buttonToggle.check(R.id.lastUpdate)
                            "dateline_desc" -> holder.buttonToggle.check(R.id.dateLine)
                            "popular" -> holder.buttonToggle.check(R.id.popular)
                            "" -> holder.buttonToggle.check(R.id.author)
                        }
                    }
                }
            }

            is FootViewHolder -> {
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
                    val name =
                        """<a class="feed-link-uname" href="/u/${feed.data?.userInfo?.uid}">${feed.data?.userInfo?.username}</a>""" + "\u3000"
                    SpannableStringBuilderUtil.isColor = true
                    holder.uname.text = SpannableStringBuilderUtil.setReply(
                        mContext,
                        name,
                        holder.uname.textSize.toInt(),
                        null
                    )
                    holder.uname.movementMethod = LinkTextView.LocalLinkMovementMethod.getInstance()
                    SpannableStringBuilderUtil.isColor = false
                    if (!feed.data?.ipLocation.isNullOrEmpty())
                        holder.ip.text = "发布于 ${feed.data?.ipLocation}"
                    ImageUtil.showAvatar(holder.avatar, feed.data?.userAvatar)
                    holder.isFollow = feed.data?.userAction?.followAuthor == 1

                    if (feed.data?.userAction?.followAuthor == 0) { //follow
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

                    if (feed.data?.deviceTitle != "") { //deviceTitle
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

                    if (feedList[0].data!!.feedType != "vote") {  //like
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

                        holder.reply.text = feed.data?.replynum //reply
                        val drawableReply: Drawable = mContext.getDrawable(R.drawable.ic_message)!!
                        drawableReply.setBounds(
                            0,
                            0,
                            holder.like.textSize.toInt(),
                            holder.like.textSize.toInt()
                        )
                        holder.reply.setCompoundDrawables(drawableReply, null, null, null)
                    }

                    if (!feed.data?.extraUrl.isNullOrEmpty()) {
                        if (feed.data?.extraUrl!!.startsWith("/goods/")) {
                            holder.extraUrlLayout.visibility = View.GONE
                        } else {
                            holder.extraUrlLayout.visibility = View.VISIBLE
                            holder.extraUrl.text = feed.data.extraUrl
                            feed.data.extraTitle?.let {
                                holder.extraTitle.text = it
                            }
                        }
                    } else holder.extraUrlLayout.visibility = View.GONE

                    if (feedList[0].data!!.feedType == "feedArticle") {
                        holder.articleMessage.visibility = View.VISIBLE
                        val articleList = ArrayList<FeedArticleContentBean.Data>()

                        if (articleList.isEmpty()) {
                            val feedRaw = """{"data":${feed.data?.messageRawOutput}}"""
                            val feedJson: FeedArticleContentBean = Gson().fromJson(
                                feedRaw,
                                FeedArticleContentBean::class.java
                            )

                            for (element in feedJson.data) {
                                if (element.type == "text" || element.type == "image" || element.type == "shareUrl")
                                    articleList.add(element)
                            }
                            holder.articleMessage.adapter = object : BaseAdapter() {
                                override fun getCount() = articleList.size + 2
                                override fun getItem(p0: Int): Any = 0
                                override fun getItemId(p0: Int): Long = 0
                                override fun getView(
                                    position1: Int,
                                    convertView: View?,
                                    parent: ViewGroup?
                                ): View {
                                    val view = LayoutInflater.from(mContext)
                                        .inflate(
                                            R.layout.item_feed_article_content_item,
                                            parent,
                                            false
                                        )
                                    val textView: TextView = view.findViewById(R.id.textView)
                                    val imageView: NineGridImageView =
                                        view.findViewById(R.id.imageView)
                                    val description: TextView = view.findViewById(R.id.description)
                                    val shareUrl: MaterialCardView =
                                        view.findViewById(R.id.shareUrl)
                                    val urlTitle: TextView = view.findViewById(R.id.urlTitle)
                                    if (position1 == 0) {
                                        textView.visibility = View.GONE
                                        shareUrl.visibility = View.GONE
                                        description.visibility = View.GONE
                                        if (feed.data?.messageCover.isNullOrEmpty())
                                            imageView.visibility = View.GONE
                                        else {
                                            imageView.visibility = View.VISIBLE
                                            val urlList = ArrayList<String>()
                                            urlList.add("${feed.data?.messageCover}.s2x.jpg")
                                            val from = feed.data?.messageCover!!.lastIndexOf("@")
                                            val middle = feed.data.messageCover.lastIndexOf("x")
                                            val end = feed.data.messageCover.lastIndexOf(".")
                                            if (from != -1 && middle != -1 && end != -1) {
                                                val width =
                                                    feed.data.messageCover.substring(
                                                        from + 1,
                                                        middle
                                                    )
                                                        .toInt()
                                                val height =
                                                    feed.data.messageCover.substring(
                                                        middle + 1,
                                                        end
                                                    )
                                                        .toInt()
                                                imageView.imgHeight = height
                                                imageView.imgWidth = width
                                            }
                                            imageView.setUrlList(urlList)
                                            imageView.apply {
                                                appListener =
                                                    this@FeedContentAdapter.appListener
                                            }
                                        }
                                        return view
                                    } else if (position1 == 1) {
                                        textView.visibility = View.VISIBLE
                                        imageView.visibility = View.GONE
                                        description.visibility = View.GONE
                                        shareUrl.visibility = View.GONE
                                        textView.movementMethod = LinkMovementMethod.getInstance()
                                        textView.text = SpannableStringBuilderUtil.setText(
                                            mContext,
                                            feed.data?.messageTitle.toString(),
                                            textView.textSize.toInt(),
                                            null
                                        )
                                        textView.paint.isFakeBoldText = true
                                        textView.setOnLongClickListener {
                                            val intent = Intent(mContext, CopyActivity::class.java)
                                            intent.putExtra(
                                                "text",
                                                textView.text.toString()
                                            )
                                            mContext.startActivity(intent)
                                            true
                                        }
                                        return view
                                    } else when (articleList[position1 - 2].type) {
                                        "text" -> {
                                            textView.visibility = View.VISIBLE
                                            imageView.visibility = View.GONE
                                            description.visibility = View.GONE
                                            shareUrl.visibility = View.GONE
                                            textView.movementMethod =
                                                LinkMovementMethod.getInstance()
                                            textView.text = SpannableStringBuilderUtil.setText(
                                                mContext,
                                                articleList[position1 - 2].message.toString(),
                                                textView.textSize.toInt(),
                                                null
                                            )
                                            textView.setOnLongClickListener {
                                                val intent =
                                                    Intent(mContext, CopyActivity::class.java)
                                                intent.putExtra(
                                                    "text",
                                                    textView.text.toString()
                                                )
                                                mContext.startActivity(intent)
                                                true
                                            }
                                            return view
                                        }

                                        "image" -> {
                                            textView.visibility = View.GONE
                                            imageView.visibility = View.VISIBLE
                                            shareUrl.visibility = View.GONE
                                            val urlList = ArrayList<String>()
                                            urlList.add("${articleList[position1 - 2].url}.s2x.jpg")
                                            val from =
                                                articleList[position1 - 2].url!!.lastIndexOf("@")
                                            val middle =
                                                articleList[position1 - 2].url!!.lastIndexOf("x")
                                            val end =
                                                articleList[position1 - 2].url!!.lastIndexOf(".")
                                            if (from != -1 && middle != -1 && end != -1) {
                                                val width =
                                                    articleList[position1 - 2].url?.substring(
                                                        from + 1,
                                                        middle
                                                    )?.toInt()
                                                val height =
                                                    articleList[position1 - 2].url?.substring(
                                                        middle + 1,
                                                        end
                                                    )
                                                        ?.toInt()
                                                imageView.imgHeight = height!!
                                                imageView.imgWidth = width!!
                                            }
                                            imageView.isCompress = true
                                            imageView.setUrlList(urlList)
                                            imageView.apply {
                                                appListener =
                                                    this@FeedContentAdapter.appListener
                                            }
                                            if (articleList[position1 - 2].description == "")
                                                description.visibility = View.GONE
                                            else
                                                description.visibility = View.VISIBLE
                                            description.text = SpannableStringBuilderUtil.setText(
                                                mContext,
                                                articleList[position1 - 2].description.toString(),
                                                description.textSize.toInt(),
                                                null
                                            )
                                            description.setOnLongClickListener {
                                                val intent =
                                                    Intent(mContext, CopyActivity::class.java)
                                                intent.putExtra(
                                                    "text",
                                                    description.text.toString()
                                                )
                                                mContext.startActivity(intent)
                                                true
                                            }
                                            return view
                                        }

                                        "shareUrl" -> {
                                            textView.visibility = View.GONE
                                            imageView.visibility = View.GONE
                                            description.visibility = View.GONE
                                            shareUrl.visibility = View.VISIBLE
                                            urlTitle.text =
                                                articleList[position1 - 2].title.toString()
                                            shareUrl.setOnClickListener {

                                                if (PrefManager.isOpenLinkOutside) {
                                                    val intent = Intent()
                                                    intent.action = Intent.ACTION_VIEW
                                                    intent.data =
                                                        Uri.parse(articleList[position1 - 2].url.toString())
                                                    try {
                                                        mContext.startActivity(intent)
                                                    } catch (e: ActivityNotFoundException) {
                                                        Toast.makeText(
                                                            mContext,
                                                            "打开失败",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        Log.w(
                                                            "error",
                                                            "Activity was not found for intent, $intent"
                                                        )
                                                    }
                                                } else {
                                                    val intent =
                                                        Intent(
                                                            mContext,
                                                            WebViewActivity::class.java
                                                        )
                                                    intent.putExtra(
                                                        "url",
                                                        articleList[position1 - 2].url.toString()
                                                    )
                                                    mContext.startActivity(intent)
                                                }
                                            }
                                            return view
                                        }

                                        else -> throw IllegalArgumentException("error feed article type: ${articleList[position1 - 2].type}")
                                    }

                                }
                            }
                            holder.linearAdapterLayout.notifyDataSetChange()
                        }
                    } else if (feedList[0].data!!.feedType == "vote") {
                        val lp = holder.itemView.layoutParams
                        if (lp is StaggeredGridLayoutManager.LayoutParams) {
                            lp.isFullSpan = true
                        }

                        if (!feed.data?.messageTitle.isNullOrEmpty()) {
                            holder.messageTitle.visibility = View.VISIBLE
                            holder.messageTitle.text = feed.data?.messageTitle
                        }

                        if (!feed.data?.message.isNullOrEmpty()) {
                            holder.message.visibility = View.VISIBLE
                            holder.message.movementMethod =
                                LinkTextView.LocalLinkMovementMethod.getInstance()
                            holder.message.text = SpannableStringBuilderUtil.setText(
                                mContext,
                                feed.data?.message.toString(),
                                (holder.message.textSize * 1.3).toInt(),
                                null
                            )
                        } else holder.message.visibility = View.GONE

                        holder.reply.text =
                            "${feed.data!!.vote!!.totalVoteNum}人投票 · ${feed.data.vote!!.totalCommentNum}个观点"
                        holder.like.text = "${DateUtils.getDate(feed.data.vote.endTime)}截止"

                        if (feed.data.vote.totalOptionNum == 2) {
                            holder.twoOptionsLayout.visibility = View.VISIBLE
                            if (System.currentTimeMillis() / 1000 > feed.data.vote.endTime) {
                                val percent: Int =
                                    ((feed.data.vote.options[0].totalSelectNum.toFloat() / (feed.data.vote.options[0].totalSelectNum.toFloat() + feed.data.vote.options[1].totalSelectNum.toFloat())) * 100).toInt()
                                holder.leftOption.text =
                                    "${feed.data.vote.options[0].title} $percent%"
                                holder.rightOption.text =
                                    "${feed.data.vote.options[1].title} ${100 - percent}%"
                            } else {
                                holder.leftOption.text = feed.data.vote.options[0].title
                                holder.rightOption.text = feed.data.vote.options[1].title
                            }
                        } else {
                            holder.voteOptions.visibility = View.VISIBLE

                            holder.voteOptions.adapter = object : BaseAdapter() {
                                override fun getCount() = feed.data.vote.options.size
                                override fun getItem(p0: Int): Any = 0
                                override fun getItemId(p0: Int): Long = 0
                                override fun getView(
                                    position1: Int,
                                    convertView: View?,
                                    parent: ViewGroup?
                                ): View {
                                    val view = LayoutInflater.from(mContext)
                                        .inflate(R.layout.item_feed_vote_item, parent, false)
                                    val title: TextView = view.findViewById(R.id.title)
                                    title.text = feed.data.vote.options[position1].title
                                    if (position1 != 0) {
                                        val space =
                                            mContext.resources.getDimensionPixelSize(R.dimen.minor_space)
                                        val layoutParams = ConstraintLayout.LayoutParams(
                                            ConstraintLayout.LayoutParams.MATCH_PARENT,
                                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                                        )
                                        layoutParams.setMargins(0, space, 0, 0)
                                        view.layoutParams = layoutParams
                                    }
                                    return view
                                }
                            }
                        }


                    } else {
                        holder.message.visibility = View.VISIBLE

                        if (!feed.data?.messageTitle.isNullOrEmpty()) {
                            holder.messageTitle.visibility = View.VISIBLE
                            holder.messageTitle.text = feed.data?.messageTitle
                        }

                        holder.message.movementMethod =
                            LinkTextView.LocalLinkMovementMethod.getInstance()
                        holder.message.text = SpannableStringBuilderUtil.setText(
                            mContext,
                            feed.data?.message.toString(),
                            (holder.message.textSize * 1.3).toInt(),
                            null
                        )
                    }

                    if (feedList[0].data!!.feedType != "feedArticle" && !feed.data?.picArr.isNullOrEmpty()) {
                        holder.multiImage.visibility = View.VISIBLE
                        if (feed.data?.picArr?.size == 1) {
                            val from = feed.data.pic.lastIndexOf("@")
                            val middle = feed.data.pic.lastIndexOf("x")
                            val end = feed.data.pic.lastIndexOf(".")
                            if (from != -1 && middle != -1 && end != -1) {
                                val width = feed.data.pic.substring(from + 1, middle).toInt()
                                val height = feed.data.pic.substring(middle + 1, end).toInt()
                                holder.multiImage.imgHeight = height
                                holder.multiImage.imgWidth = width
                            }
                        }
                        holder.multiImage.apply {
                            val urlList: MutableList<String> = ArrayList()
                            for (element in feed.data?.picArr!!)
                                if (element.endsWith("gif"))
                                    urlList.add(element)
                                else urlList.add("$element.s.jpg")
                            setUrlList(urlList)
                        }
                    } else {
                        holder.multiImage.visibility = View.GONE
                    }
                    if (feed.data?.targetRow?.id == null && feed.data?.relationRows.isNullOrEmpty())
                        holder.dyhLayout.visibility = View.GONE
                    else {
                        holder.dyhLayout.visibility = View.VISIBLE
                        holder.linearAdapterLayout.adapter = object : BaseAdapter() {
                            override fun getCount(): Int =
                                if (feed.data?.targetRow?.id == null) feed.data?.relationRows!!.size
                                else 1 + feed.data.relationRows!!.size

                            override fun getItem(p0: Int): Any = 0

                            override fun getItemId(p0: Int): Long = 0

                            override fun getView(
                                position: Int,
                                convertView: View?,
                                parent: ViewGroup?
                            ): View {
                                val view = LayoutInflater.from(mContext).inflate(
                                    R.layout.item_feed_tag,
                                    parent,
                                    false
                                )
                                val logo: ImageView = view.findViewById(R.id.iconMiniScrollCard)
                                val title: TextView = view.findViewById(R.id.title)
                                val type: String
                                val id: String
                                val url: String
                                if (feed.data?.targetRow?.id != null) {
                                    if (position == 0) {
                                        type = feed.data.targetRow.targetType.toString()
                                        id = feed.data.targetRow.id
                                        url = feed.data.targetRow.url
                                        title.text = feed.data.targetRow.title
                                        ImageUtil.showIMG(logo, feed.data.targetRow.logo)
                                    } else {
                                        val space =
                                            mContext.resources.getDimensionPixelSize(R.dimen.minor_space)
                                        val layoutParams = ConstraintLayout.LayoutParams(
                                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                                        )
                                        layoutParams.setMargins(space, 0, 0, 0)
                                        view.layoutParams = layoutParams
                                        type = feed.data.relationRows!![position - 1].entityType
                                        id = feed.data.relationRows[position - 1].id
                                        url = feed.data.relationRows[position - 1].url
                                        title.text = feed.data.relationRows[position - 1].title
                                        ImageUtil.showIMG(
                                            logo,
                                            feed.data.relationRows[position - 1].logo
                                        )
                                    }
                                } else {
                                    if (position == 0) {
                                        type = feed.data?.relationRows!![0].entityType
                                        id = feed.data.relationRows[0].id
                                        title.text = feed.data.relationRows[0].title
                                        url = feed.data.relationRows[0].url
                                        ImageUtil.showIMG(logo, feed.data.relationRows[0].logo)
                                    } else {
                                        val space =
                                            mContext.resources.getDimensionPixelSize(R.dimen.minor_space)
                                        val layoutParams = ConstraintLayout.LayoutParams(
                                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                                        )
                                        layoutParams.setMargins(space, 0, 0, 0)
                                        view.layoutParams = layoutParams
                                        type = feed.data?.relationRows!![position].entityType
                                        id = feed.data.relationRows[position].id
                                        url = feed.data.relationRows[position].url
                                        title.text = feed.data.relationRows[position].title
                                        ImageUtil.showIMG(
                                            logo,
                                            feed.data.relationRows[position].logo
                                        )
                                    }
                                }
                                view.setOnClickListener {
                                    if (url.contains("/apk/")) {
                                        val intent = Intent(mContext, AppActivity::class.java)
                                        intent.putExtra("id", url.replace("/apk/", ""))
                                        mContext.startActivity(intent)
                                    } else if (url.contains("/game/")) {
                                        val intent = Intent(mContext, AppActivity::class.java)
                                        intent.putExtra("id", url.replace("/game/", ""))
                                        mContext.startActivity(intent)
                                    } else if (type == "feedRelation") {
                                        val intent = Intent(mContext, DyhActivity::class.java)
                                        intent.putExtra("id", id)
                                        intent.putExtra("title", title.text)
                                        mContext.startActivity(intent)
                                    } else if (type == "topic" || type == "product") {
                                        val intent = Intent(mContext, TopicActivity::class.java)
                                        intent.putExtra("type", type)
                                        intent.putExtra("title", title.text)
                                        intent.putExtra("url", url)
                                        intent.putExtra("id", id)
                                        mContext.startActivity(intent)
                                    }
                                }
                                return view
                            }
                        }
                    }
                }
            }

            is FeedContentReplyViewHolder -> {
                if (replyList.isNotEmpty()) {

                    val reply = replyList[position - 2]

                    if (feedList[0].data!!.feedType == "vote" && extraKey != "") {
                        holder.vote.visibility = View.VISIBLE

                        if (reply.extraKey == extraKey) {
                            holder.vote.text = "正方"
                        } else {
                            holder.vote.text = "反方"
                        }
                    } else
                        holder.vote.visibility = View.GONE

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
                    val name =
                        """<a class="feed-link-uname" href="/u/${reply.uid}">${reply.username}$unameTag$replyTag</a>""" + "\u3000"
                    SpannableStringBuilderUtil.isColor = true
                    holder.uname.text = SpannableStringBuilderUtil.setReply(
                        mContext,
                        name,
                        holder.uname.textSize.toInt(),
                        null
                    )
                    holder.uname.movementMethod = LinkTextView.LocalLinkMovementMethod.getInstance()

                    ImageUtil.showAvatar(holder.avatar, reply.userAvatar)

                    SpannableStringBuilderUtil.isColor = false
                    if (reply.message == "[图片]") {
                        holder.message.text = "[图片]"
                        holder.message.visibility = View.GONE
                    } else {
                        holder.message.visibility = View.VISIBLE
                        holder.message.movementMethod =
                            LinkTextView.LocalLinkMovementMethod.getInstance()
                        holder.message.text = SpannableStringBuilderUtil.setText(
                            mContext,
                            reply.message,
                            (holder.message.textSize * 1.3).toInt(),
                            null
                        )
                    }

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

                    if (!reply.replyRows.isNullOrEmpty()) {
                        val sortedList = ArrayList<TotalReplyResponse.Data>()
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

                                    val replyTag1 =
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
                                            else -> """<a class="feed-link-uname" href="/u/${replyData.ruid}">${replyData.rusername}${rReplyTag}</a>"""
                                        }

                                    val replyPic =
                                        when (replyData.pic) {
                                            "" -> ""
                                            else -> """ <a class=\"feed-forward-pic\" href=${replyData.pic}>查看图片(${replyData.picArr?.size})</a>"""
                                        }

                                    val mess =
                                        """<a class="feed-link-uname" href="/u/${replyData.uid}">${replyData.username}${replyTag1}</a>回复${rReplyUser}: ${replyData.message}${replyPic}"""

                                    textView.movementMethod = LinkMovementClickMethod.getInstance()

                                    textView.text = SpannableStringBuilderUtil.setReply(
                                        mContext,
                                        mess,
                                        textView.textSize.toInt(),
                                        replyData.picArr
                                    )

                                    SpannableStringBuilderUtil.setData(
                                        position1 + 1,
                                        reply.uid
                                    )

                                    view.setOnClickListener {
                                        appListener?.onReply2Reply(
                                            holder.bindingAdapterPosition,
                                            null,
                                            replyData.id,
                                            replyData.uid,
                                            replyData.username,
                                            "reply"
                                        )
                                    }

                                    view.setOnLongClickListener {
                                        this@FeedContentAdapter.text = textView.text.toString()
                                        id = replyData.id
                                        uid = replyData.uid
                                        ruid = reply.uid
                                        this@FeedContentAdapter.position =
                                            holder.bindingAdapterPosition
                                        this@FeedContentAdapter.rPosition = position1
                                        val popup = PopupMenu(mContext, it)
                                        val inflater = popup.menuInflater
                                        inflater.inflate(R.menu.feed_reply_menu, popup.menu)
                                        popup.menu.findItem(R.id.copy).isVisible = true
                                        popup.menu.findItem(R.id.delete).isVisible =
                                            PrefManager.uid == replyData.uid
                                        popup.setOnMenuItemClickListener(this@FeedContentAdapter)
                                        popup.show()
                                        true
                                    }

                                    return view
                                }
                            }
                        } else holder.replyLayout.visibility = View.GONE
                    } else holder.replyLayout.visibility = View.GONE

                    if (reply.replyRowsMore != 0) {
                        holder.totalReply.visibility = View.VISIBLE
                        val count = reply.replyRowsMore + reply.replyRows?.size!!
                        holder.totalReply.text = "查看更多回复($count)"
                    } else
                        holder.totalReply.visibility = View.GONE

                    if (!reply.picArr.isNullOrEmpty()) {
                        holder.multiImage.visibility = View.VISIBLE
                        if (reply.picArr.size == 1) {
                            val from = reply.pic.lastIndexOf("@")
                            val middle = reply.pic.lastIndexOf("x")
                            val end = reply.pic.lastIndexOf(".")
                            if (from != -1 && middle != -1 && end != -1) {
                                val width = reply.pic.substring(from + 1, middle).toInt()
                                val height = reply.pic.substring(middle + 1, end).toInt()
                                holder.multiImage.imgHeight = height
                                holder.multiImage.imgWidth = width
                            }
                        }
                        holder.multiImage.apply {
                            val urlList: MutableList<String> = ArrayList()
                            for (element in reply.picArr)
                                if (element.endsWith("gif"))
                                    urlList.add(element)
                                else urlList.add("$element.s.jpg")
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

            itemCount - 1 -> TYPE_FOOTER

            0 -> TYPE_CONTENT

            1 -> TYPE_FIX

            else -> TYPE_REPLY
        }
    }

    override fun onMenuItemClick(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.block -> {
                BlackListUtil.saveUid(uid)
                if (rPosition == null) {
                    replyList.removeAt(position - 2)
                    notifyItemRemoved(position)
                } else {
                    replyList[position - 2].replyRows?.removeAt(rPosition!!)
                    notifyItemChanged(position)
                }
            }

            R.id.report -> {
                val intent = Intent(mContext, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed_reply&id=$id"
                )
                mContext.startActivity(intent)
            }

            R.id.delete -> {
                appListener?.onDeleteFeedReply(id, position, rPosition)
            }

            R.id.copy -> {
                val intent = Intent(mContext, CopyActivity::class.java)
                intent.putExtra("text", text)
                mContext.startActivity(intent)
            }

            R.id.show -> {
                appListener?.onShowTotalReply(position, ruid, id, rPosition)
            }
        }
        return false
    }

}