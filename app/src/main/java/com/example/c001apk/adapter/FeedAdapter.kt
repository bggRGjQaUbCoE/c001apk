package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
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
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.logic.model.FeedArticleContentBean
import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.NetWorkUtil
import com.example.c001apk.util.NetWorkUtil.openLink
import com.example.c001apk.util.NetWorkUtil.openLinkDyh
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.LinearAdapterLayout
import com.example.c001apk.view.LinkMovementClickMethod
import com.example.c001apk.view.LinkTextView
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson

class FeedAdapter(
    private val mContext: Context,
    private val feedList: List<FeedContentResponse>,
    private var replyList: ArrayList<TotalReplyResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), PopupMenu.OnMenuItemClickListener {

    private lateinit var articleList: ArrayList<FeedArticleContentBean.Data>

    private var extraKey: String? = null

    fun setExtraKey(extraKey: String?) {
        this.extraKey = extraKey
    }

    private var haveTop = false

    fun setHaveTop(haveTop: Boolean) {
        this.haveTop = haveTop
    }

    private var loadState = 2
    val LOADING = 1
    val LOADING_COMPLETE = 2
    val LOADING_END = 3
    val LOADING_ERROR = 4
    val LOADING_REPLY = 5
    private var errorMessage: String? = null

    fun setLoadState(loadState: Int, errorMessage: String?) {
        this.loadState = loadState
        this.errorMessage = errorMessage
    }

    private var text = ""
    private var uid = ""
    private var id = ""
    private var ruid = ""
    private var position = -1
    private var rPosition: Int? = null

    private var appListener: AppListener? = null

    fun setAppListener(appListener: AppListener?) {
        this.appListener = appListener
    }

    private var listType = "lastupdate_desc"

    fun setListType(listType: String) {
        this.listType = listType
    }

    class TextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
    }

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: NineGridImageView =
            view.findViewById(R.id.imageView)
        val description: TextView = view.findViewById(R.id.description)
    }

    class UrlViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var url = ""
        val shareUrl: MaterialCardView =
            view.findViewById(R.id.shareUrl)
        val urlTitle: TextView = view.findViewById(R.id.urlTitle)
    }

    class FootViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerLayout: FrameLayout = view.findViewById(R.id.footerLayout)
        val indicator: CircularProgressIndicator = view.findViewById(R.id.indicator)
        val noMore: TextView = view.findViewById(R.id.noMore)
        val retry: Button = view.findViewById(R.id.retry)
    }

    class FeedContentReplyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: LinkTextView = view.findViewById(R.id.uname)
        val message: LinkTextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
        val reply: TextView = view.findViewById(R.id.reply)
        val replyLayout: MaterialCardView = view.findViewById(R.id.replyLayout)
        val totalReply: TextView = view.findViewById(R.id.totalReply)
        val multiImage: NineGridImageView = view.findViewById(R.id.multiImage)
        val linearAdapterLayout: LinearAdapterLayout = view.findViewById(R.id.linearAdapterLayout)
        val expand: ImageButton = view.findViewById(R.id.expand)
        var id = ""
        var notiId = ""
        var rid = ""
        var uid = ""
        var isLike = false
        var type = ""
        var url = ""
        var vote: TextView = view.findViewById(R.id.vote)
    }

    class FeedContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
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
        val twoOptionsLayout: LinearLayout = view.findViewById(R.id.twoOptionsLayout)
        val leftOption: Button = view.findViewById(R.id.leftOption)
        val rightOption: Button = view.findViewById(R.id.rightOption)
        val voteOptions: LinearAdapterLayout = view.findViewById(R.id.voteOptions)
        val extraUrlLayout: ConstraintLayout = view.findViewById(R.id.extraUrlLayout)
        val extraTitle: TextView = view.findViewById(R.id.extraTitle)
        val extraUrl: TextView = view.findViewById(R.id.extraUrl)
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

            -1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_rv_footer, parent, false)
                val viewHolder = FootViewHolder(view)
                viewHolder.retry.setOnClickListener {
                    appListener?.onReload()
                }
                viewHolder
            }

            0 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_feed_article_content_item, parent, false)
                val viewHolder = TextViewHolder(view)
                viewHolder.textView.setOnLongClickListener {
                    val intent = Intent(parent.context, CopyActivity::class.java)
                    intent.putExtra("text", viewHolder.textView.text.toString())
                    parent.context.startActivity(intent)
                    true
                }
                viewHolder
            }

            1 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_feed_article_content_item, parent, false)
                val viewHolder = ImageViewHolder(view)
                viewHolder.imageView.apply {
                    appListener = this@FeedAdapter.appListener
                }
                viewHolder
            }

            2 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_feed_article_content_item, parent, false)
                val viewHolder = UrlViewHolder(view)
                viewHolder.shareUrl.setOnClickListener {
                    openLink(parent.context, viewHolder.url, viewHolder.urlTitle.text.toString())
                }
                viewHolder
            }

            3 -> {
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
                    appListener = this@FeedAdapter.appListener
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
                    popup.menu.findItem(R.id.report).isVisible = PrefManager.isLogin
                    popup.setOnMenuItemClickListener(this@FeedAdapter)
                    popup.show()
                }
                viewHolder
            }

            4 -> {
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

            5 -> {
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
                    appListener = this@FeedAdapter.appListener
                }
                viewHolder.follow.visibility =
                    if (PrefManager.isLogin) View.VISIBLE
                    else View.GONE
                viewHolder.follow.setOnClickListener {
                    if (PrefManager.isLogin)
                        appListener?.onPostFollow(
                            viewHolder.isFollow,
                            viewHolder.uid,
                            0
                        )
                }
                viewHolder.extraUrlLayout.setOnClickListener {
                    openLink(
                        parent.context,
                        viewHolder.extraUrl.text.toString(),
                        viewHolder.extraTitle.text.toString()
                    )
                }
                viewHolder
            }

            else -> throw IllegalArgumentException("viewType error: $viewType")

        }

    }

    override fun getItemCount(): Int {
        return if (feedList.isEmpty()) 0
        else {
            if (feedList[0].data?.feedType == "feedArticle") {
                articleList = ArrayList()

                if (feedList[0].data?.messageCover?.isNotEmpty() == true) {
                    articleList.add(
                        FeedArticleContentBean.Data(
                            "image",
                            null,
                            feedList[0].data?.messageCover,
                            null,
                            null,
                            null,
                            null
                        )
                    )
                }

                if (feedList[0].data?.messageTitle?.isNotEmpty() == true) {
                    articleList.add(
                        FeedArticleContentBean.Data(
                            "text",
                            feedList[0].data?.messageTitle,
                            null,
                            null,
                            "true",
                            null,
                            null
                        )
                    )
                }

                val feedRaw = """{"data":${feedList[0].data?.messageRawOutput}}"""
                val feedJson: FeedArticleContentBean = Gson().fromJson(
                    feedRaw,
                    FeedArticleContentBean::class.java
                )

                for (element in feedJson.data) {
                    if (element.type == "text" || element.type == "image" || element.type == "shareUrl")
                        articleList.add(element)
                }

                articleList.size + replyList.size + 2
            } else replyList.size + 3
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (feedList[0].data?.feedType == "feedArticle") {
            when (position) {
                itemCount - 1 -> -1
                in 0..<articleList.size -> {
                    when (articleList[position].type) {
                        "text" -> 0
                        "image" -> 1
                        "shareUrl" -> 2
                        else -> throw IllegalArgumentException()
                    }
                }

                articleList.size -> 4 //fix
                else -> 3 //reply
            }
        } else {
            when (position) {
                itemCount - 1 -> -1
                0 -> 5
                1 -> 4
                else -> 3
            }
        }
    }

    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {

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
                    ImageUtil.showIMG(holder.avatar, feed.data?.userAvatar)
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

                    if (feedList[0].data!!.feedType == "vote") {
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
                                        val layoutParams = ConstraintLayout.LayoutParams(
                                            ConstraintLayout.LayoutParams.MATCH_PARENT,
                                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                                        )
                                        layoutParams.setMargins(0, 5.dp, 0, 0)
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

                    if (!feed.data?.picArr.isNullOrEmpty()) {
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
                                if ((PrefManager.imageQuality == "origin" ||
                                            (PrefManager.imageQuality == "auto" && NetWorkUtil.isWifiConnected()))
                                    && element.endsWith("gif")
                                )
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
                                    R.layout.item_home_icon_mini_scroll_card_item,
                                    parent,
                                    false
                                )
                                val parentLayout: ConstraintLayout =
                                    view.findViewById(R.id.parentLayout)
                                parentLayout.background =
                                    mContext.getDrawable(R.drawable.selector_bg_20_feed)
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
                                        val layoutParams = ConstraintLayout.LayoutParams(
                                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                                        )
                                        layoutParams.setMargins(5.dp, 0, 0, 0)
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
                                        val layoutParams = ConstraintLayout.LayoutParams(
                                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                                        )
                                        layoutParams.setMargins(5.dp, 0, 0, 0)
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
                                    openLinkDyh(type, mContext, url, id, title.text.toString())
                                }
                                return view
                            }
                        }
                    }
                }
            }

            is FootViewHolder -> {
                val lp = holder.itemView.layoutParams
                if (lp is StaggeredGridLayoutManager.LayoutParams) {
                    lp.isFullSpan = true
                } else {
                    holder.footerLayout.layoutParams =
                        if (loadState == LOADING_REPLY)
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        else
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            )
                }

                when (loadState) {

                    LOADING_REPLY -> {
                        holder.footerLayout.visibility = View.VISIBLE
                        holder.indicator.visibility = View.VISIBLE
                        holder.indicator.isIndeterminate = true
                        holder.noMore.visibility = View.GONE
                        holder.retry.visibility = View.GONE

                    }

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

                }
            }

            is TopViewHolder -> {
                if (feedList.isNotEmpty()) {
                    if (feedList[0].data?.feedType == "vote") {
                        (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan =
                            true
                        holder.replyCount.text = "观点"
                        holder.buttonToggle.visibility = View.GONE
                    } else {
                        holder.replyCount.text = "共 ${feedList[0].data?.replynum} 回复"
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

            is TextViewHolder -> {
                val item = articleList[position]
                holder.textView.visibility = View.VISIBLE
                holder.textView.movementMethod =
                    LinkMovementMethod.getInstance()
                holder.textView.text = SpannableStringBuilderUtil.setText(
                    mContext,
                    item.message.toString(),
                    holder.textView.textSize.toInt(),
                    null
                )
                holder.textView.paint.isFakeBoldText =
                    (position == 0 || position == 1) && item.title == "true"
            }

            is ImageViewHolder -> {
                val item = articleList[position]
                holder.imageView.visibility = View.VISIBLE
                val urlList = ArrayList<String>()
                urlList.add("${item.url}.s.jpg")
                val from =
                    item.url!!.lastIndexOf("@")
                val middle =
                    item.url.lastIndexOf("x")
                val end =
                    item.url.lastIndexOf(".")
                if (from != -1 && middle != -1 && end != -1) {
                    val width =
                        item.url.substring(from + 1, middle).toInt()
                    val height =
                        item.url.substring(middle + 1, end).toInt()
                    holder.imageView.imgHeight = height
                    holder.imageView.imgWidth = width
                }
                holder.imageView.isCompress = true
                holder.imageView.setUrlList(urlList)
                if (item.description.isNullOrEmpty())
                    holder.description.visibility = View.GONE
                else
                    holder.description.visibility = View.VISIBLE
                holder.description.text = SpannableStringBuilderUtil.setText(
                    mContext,
                    item.description.toString(),
                    holder.description.textSize.toInt(),
                    null
                )
            }

            is UrlViewHolder -> {
                val item = articleList[position]
                holder.shareUrl.visibility = View.VISIBLE
                holder.urlTitle.text = item.title.toString()
                holder.url = item.url.toString()
            }

            is FeedContentReplyViewHolder -> {
                if (replyList.isNotEmpty()) {

                    holder.itemView.also {
                        if (it.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                            it.background = mContext.getDrawable(R.drawable.text_card_bg)
                            it.foreground = mContext.getDrawable(R.drawable.selector_bg_12_carousel)
                            holder.replyLayout.setCardBackgroundColor(mContext.getColor(R.color.reply2reply_card_background_color))
                        } else {
                            it.foreground = mContext.getDrawable(R.drawable.selector_bg_carousel)
                            holder.replyLayout.setCardBackgroundColor(mContext.getColor(R.color.home_card_background_color))
                        }
                    }

                    val reply = if (feedList[0].data?.feedType == "feedArticle")
                        replyList[position - articleList.size - 1]
                    else replyList[position - 2]

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

                    ImageUtil.showIMG(holder.avatar, reply.userAvatar)

                    SpannableStringBuilderUtil.isColor = false
                    if (reply.message == "[图片]") {
                        holder.message.text = "[图片]"
                        holder.message.visibility = View.GONE
                    } else {
                        holder.message.visibility = View.VISIBLE
                        holder.message.movementMethod =
                            LinkTextView.LocalLinkMovementMethod.getInstance()
                        holder.message.highlightColor = ColorUtils.setAlphaComponent(
                            ThemeUtils.getThemeAttrColor(
                                mContext,
                                rikka.preference.simplemenu.R.attr.colorPrimaryDark
                            ), 128
                        )
                        holder.message.text = SpannableStringBuilderUtil.setText(
                            mContext,
                            reply.message,
                            (holder.message.textSize * 1.3).toInt(),
                            null
                        )
                    }

                    if (feedList[0].data?.feedType != "vote") {
                        holder.pubDate.text = DateUtils.fromToday(reply.dateline)
                        val drawableDate: Drawable = mContext.getDrawable(R.drawable.ic_date)!!
                        drawableDate.setBounds(
                            0,
                            0,
                            holder.pubDate.textSize.toInt(),
                            holder.pubDate.textSize.toInt()
                        )
                        holder.pubDate.setCompoundDrawables(drawableDate, null, null, null)
                    } else holder.pubDate.visibility = View.INVISIBLE

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
                                        this@FeedAdapter.text = textView.text.toString()
                                        id = replyData.id
                                        uid = replyData.uid
                                        ruid = reply.uid
                                        this@FeedAdapter.position =
                                            holder.bindingAdapterPosition
                                        this@FeedAdapter.rPosition = position1
                                        val popup = PopupMenu(mContext, it)
                                        val inflater = popup.menuInflater
                                        inflater.inflate(R.menu.feed_reply_menu, popup.menu)
                                        popup.menu.findItem(R.id.copy).isVisible = true
                                        popup.menu.findItem(R.id.delete).isVisible =
                                            PrefManager.uid == replyData.uid
                                        popup.menu.findItem(R.id.report).isVisible =
                                            PrefManager.isLogin
                                        popup.setOnMenuItemClickListener(this@FeedAdapter)
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
                                if ((PrefManager.imageQuality == "origin" ||
                                            (PrefManager.imageQuality == "auto" && NetWorkUtil.isWifiConnected()))
                                    && element.endsWith("gif")
                                )
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