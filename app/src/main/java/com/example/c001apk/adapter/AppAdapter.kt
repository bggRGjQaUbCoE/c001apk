package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.ThemeUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.IconLinkGridCardBean
import com.example.c001apk.ui.activity.AppActivity
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.FeedActivity
import com.example.c001apk.ui.activity.TopicActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.HistoryUtil
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.NetWorkUtil.openLink
import com.example.c001apk.util.NetWorkUtil.openLinkDyh
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.view.LinearAdapterLayout
import com.example.c001apk.view.LinearItemDecoration1
import com.example.c001apk.view.LinkTextView
import com.example.c001apk.view.circleindicator.CircleIndicator3
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.CircularProgressIndicator


class AppAdapter(
    private val mContext: Context,
    private val dataList: ArrayList<HomeFeedResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), PopupMenu.OnMenuItemClickListener {

    private var entityType = ""
    private var fid = ""
    private var uid = ""
    private var position = -1

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

    class ImageCarouselCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)
    }

    class IconLinkGridCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)
        val indicator: CircleIndicator3 = view.findViewById(R.id.indicator)
    }

    class FeedVoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
        val uname: LinkTextView = view.findViewById(R.id.uname)
        val expand: ImageButton = view.findViewById(R.id.expand)
        val device: TextView = view.findViewById(R.id.device)
        val message: LinkTextView = view.findViewById(R.id.message)
        val messageTitle: TextView = view.findViewById(R.id.messageTitle)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val dyhLayout: HorizontalScrollView = view.findViewById(R.id.dyhLayout)
        val linearAdapterLayout: LinearAdapterLayout = view.findViewById(R.id.linearAdapterLayout)
        val voteNum: TextView = view.findViewById(R.id.voteNum)
        val optionNum: TextView = view.findViewById(R.id.optionNum)
        val twoOptionsLayout: LinearLayout = view.findViewById(R.id.twoOptionsLayout)
        val leftOption: Button = view.findViewById(R.id.leftOption)
        val rightOption: Button = view.findViewById(R.id.rightOption)
        val voteOptions: LinearAdapterLayout = view.findViewById(R.id.voteOptions)
        var feedType = ""
        var avatarUrl = ""
        var id = ""
        var uid = ""
        var pubDataRaw = ""
    }

    class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var feedType = ""
        val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
        var avatarUrl = ""
        val uname: LinkTextView = view.findViewById(R.id.uname)
        val from: TextView = view.findViewById(R.id.from)
        val device: TextView = view.findViewById(R.id.device)
        val message: LinkTextView = view.findViewById(R.id.message)
        val messageTitle: TextView = view.findViewById(R.id.messageTitle)
        val multiImage: NineGridImageView = view.findViewById(R.id.multiImage)
        var id = ""
        var uid = ""
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        var pubDataRaw = ""
        val like: TextView = view.findViewById(R.id.like)
        var isLike = false
        val reply: TextView = view.findViewById(R.id.reply)
        val dyhLayout: HorizontalScrollView = view.findViewById(R.id.dyhLayout)
        val linearAdapterLayout: LinearAdapterLayout = view.findViewById(R.id.linearAdapterLayout)
        val expand: ImageButton = view.findViewById(R.id.expand)
        val hotReply: TextView = view.findViewById(R.id.hotReply)
        val forwarded: LinearLayout = view.findViewById(R.id.forwarded)
        val forwardedMess: LinkTextView = view.findViewById(R.id.forwardedMess)
        val forwardedPic: NineGridImageView = view.findViewById(R.id.forwardedPic)
        var forwardedId: String? = null
    }

    class ImageTextScrollCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    class ImageSquareScrollCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    class IconMiniScrollCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    class RefreshCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        val follow: TextView = view.findViewById(R.id.follow)
        val fans: TextView = view.findViewById(R.id.fans)
        val act: TextView = view.findViewById(R.id.act)
        val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
        val followBtn: TextView = view.findViewById(R.id.followBtn)
        var uid = ""
        var isFollow = false
    }

    class RecentHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        val follow: TextView = view.findViewById(R.id.follow)
        val fans: TextView = view.findViewById(R.id.fans)
        val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
        val followBtn: TextView = view.findViewById(R.id.followBtn)
        var targetType = ""
        var id = ""
        var url = ""
    }

    class TopicProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val parent: MaterialCardView = view.findViewById(R.id.parent)
        val title: TextView = view.findViewById(R.id.title)
        val hotNum: TextView = view.findViewById(R.id.hotNum)
        val commentNum: TextView = view.findViewById(R.id.commentNum)
        val logo: ShapeableImageView = view.findViewById(R.id.logo)
        var entityType = ""
        var aliasTitle = ""
        var id = ""
        var url = ""
    }

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val hotNum: TextView = view.findViewById(R.id.hotNum)
        val commentNum: TextView = view.findViewById(R.id.commentNum)
        val logo: ShapeableImageView = view.findViewById(R.id.logo)
        var entityType = ""
        var apkName = ""
    }

    class FeedReplyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
        var avatarUrl = ""
        val uname: TextView = view.findViewById(R.id.uname)
        val device: TextView = view.findViewById(R.id.device)
        val message: LinkTextView = view.findViewById(R.id.message)
        val messageTitle: TextView = view.findViewById(R.id.messageTitle)
        var id = ""
        var feedId = ""
        var uid = ""
        var feedUid = ""
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        var pubDataRaw = ""
        val like: TextView = view.findViewById(R.id.like)
        var isLike = false
        val reply: TextView = view.findViewById(R.id.reply)
        val expand: ImageButton = view.findViewById(R.id.expand)
        val feedUname: TextView = view.findViewById(R.id.feedUname)
        val feedMessage: LinkTextView = view.findViewById(R.id.feedMessage)
        val feed: ConstraintLayout = view.findViewById(R.id.feed)
        var entityType = ""
    }

    class CollectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var id = ""
        var uid = ""
        val cover: ShapeableImageView = view.findViewById(R.id.cover)
        val title: TextView = view.findViewById(R.id.title)
        val description: TextView = view.findViewById(R.id.description)
        val mode: TextView = view.findViewById(R.id.mode)
        val followNum: TextView = view.findViewById(R.id.followNum)
        val contentNum: TextView = view.findViewById(R.id.contentNum)
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
                        .inflate(R.layout.item_home_image_carousel_card, parent, false)
                ImageCarouselCardViewHolder(view)
            }

            1 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_home_icon_link_grid_card, parent, false)
                IconLinkGridCardViewHolder(view)
            }

            2 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_home_feed, parent, false)
                val viewHolder = FeedViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    IntentUtil.startActivity<FeedActivity>(parent.context) {
                        putExtra("id", viewHolder.id)
                    }
                    if (PrefManager.isRecordHistory)
                        HistoryUtil.saveHistory(
                            viewHolder.id,
                            viewHolder.uid,
                            viewHolder.uname.text.toString(),
                            viewHolder.avatarUrl,
                            viewHolder.device.text.toString(),
                            viewHolder.message.text.toString(),
                            viewHolder.pubDataRaw
                        )
                }
                viewHolder.reply.setOnClickListener {
                    IntentUtil.startActivity<FeedActivity>(parent.context) {
                        putExtra("id", viewHolder.id)
                        putExtra("viewReply", true)
                    }
                    if (PrefManager.isRecordHistory)
                        HistoryUtil.saveHistory(
                            viewHolder.id,
                            viewHolder.uid,
                            viewHolder.uname.text.toString(),
                            viewHolder.avatarUrl,
                            viewHolder.device.text.toString(),
                            viewHolder.message.text.toString(),
                            viewHolder.pubDataRaw
                        )
                }
                viewHolder.hotReply.setOnClickListener {
                    IntentUtil.startActivity<FeedActivity>(parent.context) {
                        putExtra("id", viewHolder.id)
                        putExtra("viewReply", true)
                    }
                    if (PrefManager.isRecordHistory)
                        HistoryUtil.saveHistory(
                            viewHolder.id,
                            viewHolder.uid,
                            viewHolder.uname.text.toString(),
                            viewHolder.avatarUrl,
                            viewHolder.device.text.toString(),
                            viewHolder.message.text.toString(),
                            viewHolder.pubDataRaw
                        )
                }
                viewHolder.hotReply.setOnLongClickListener {
                    IntentUtil.startActivity<CopyActivity>(parent.context) {
                        putExtra("text", viewHolder.hotReply.text.toString())
                    }
                    true
                }
                viewHolder.itemView.setOnLongClickListener {
                    IntentUtil.startActivity<CopyActivity>(parent.context) {
                        putExtra("text", viewHolder.message.text.toString())
                    }
                    true
                }
                viewHolder.avatar.setOnClickListener {
                    IntentUtil.startActivity<UserActivity>(parent.context) {
                        putExtra("id", viewHolder.uid)
                    }
                }
                viewHolder.like.setOnClickListener {
                    if (PrefManager.isLogin) {
                        if (PrefManager.SZLMID == "") {
                            Toast.makeText(mContext, "数字联盟ID不能为空", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            appListener?.onPostLike(
                                null,
                                viewHolder.isLike,
                                viewHolder.id,
                                viewHolder.bindingAdapterPosition
                            )
                        }
                    }
                }

                viewHolder.multiImage.appListener = this@AppAdapter.appListener

                viewHolder.forwardedPic.appListener = this@AppAdapter.appListener

                viewHolder.forwarded.setOnClickListener {
                    IntentUtil.startActivity<FeedActivity>(parent.context) {
                        putExtra("id", viewHolder.forwardedId)
                    }
                }

                viewHolder.forwarded.setOnLongClickListener {
                    IntentUtil.startActivity<CopyActivity>(parent.context) {
                        putExtra("text", viewHolder.forwardedMess.text.toString())
                    }
                    true
                }

                viewHolder.expand.setOnClickListener {
                    entityType = "feed"
                    fid = viewHolder.id
                    uid = viewHolder.uid
                    position = viewHolder.bindingAdapterPosition
                    val popup = PopupMenu(mContext, it)
                    val inflater = popup.menuInflater
                    inflater.inflate(R.menu.feed_reply_menu, popup.menu)
                    popup.menu.findItem(R.id.copy).isVisible = false
                    popup.menu.findItem(R.id.delete).isVisible = PrefManager.uid == viewHolder.uid
                    popup.menu.findItem(R.id.show).isVisible = false
                    popup.menu.findItem(R.id.report).isVisible = PrefManager.isLogin
                    popup.setOnMenuItemClickListener(this@AppAdapter)
                    popup.show()
                }
                viewHolder
            }

            3 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_home_image_text_scroll_card, parent, false)
                ImageTextScrollCardViewHolder(view)
            }

            4 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_home_icon_mini_scroll_card, parent, false)
                IconMiniScrollCardViewHolder(view)
            }

            5 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_home_feed_refresh_card, parent, false)
                RefreshCardViewHolder(view)
            }

            6 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_user, parent, false)
                val viewHolder = UserViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    IntentUtil.startActivity<UserActivity>(parent.context) {
                        putExtra("id", viewHolder.uid)
                    }
                }
                viewHolder.followBtn.setOnClickListener {
                    if (PrefManager.isLogin)
                        appListener?.onPostFollow(
                            viewHolder.isFollow,
                            viewHolder.uid,
                            viewHolder.bindingAdapterPosition
                        )
                }
                viewHolder
            }

            7 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_topic, parent, false)
                val viewHolder = TopicProductViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    IntentUtil.startActivity<TopicActivity>(parent.context) {
                        putExtra("type", viewHolder.entityType)
                        putExtra("title", viewHolder.title.text)
                        putExtra("url", viewHolder.url)
                        putExtra("id", viewHolder.id)
                    }
                }
                viewHolder
            }

            8 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_topic, parent, false)
                val viewHolder = AppViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    IntentUtil.startActivity<AppActivity>(parent.context) {
                        putExtra("id", viewHolder.apkName)
                    }
                }
                viewHolder
            }

            9 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_feed_vote, parent, false)
                val viewHolder = FeedVoteViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    IntentUtil.startActivity<FeedActivity>(parent.context) {
                        putExtra("id", viewHolder.id)
                    }
                    if (PrefManager.isRecordHistory)
                        HistoryUtil.saveHistory(
                            viewHolder.id,
                            viewHolder.uid,
                            viewHolder.uname.text.toString(),
                            viewHolder.avatarUrl,
                            viewHolder.device.text.toString(),
                            viewHolder.message.text.toString(),
                            viewHolder.pubDataRaw
                        )
                }
                viewHolder.itemView.setOnLongClickListener {
                    IntentUtil.startActivity<CopyActivity>(parent.context) {
                        putExtra("text", viewHolder.message.text.toString())
                    }
                    true
                }
                viewHolder.avatar.setOnClickListener {
                    IntentUtil.startActivity<UserActivity>(parent.context) {
                        putExtra("id", viewHolder.uid)
                    }
                }
                viewHolder.expand.setOnClickListener {
                    entityType = "feed"
                    fid = viewHolder.id
                    uid = viewHolder.uid
                    position = viewHolder.bindingAdapterPosition
                    val popup = PopupMenu(mContext, it)
                    val inflater = popup.menuInflater
                    inflater.inflate(R.menu.feed_reply_menu, popup.menu)
                    popup.menu.findItem(R.id.copy).isVisible = false
                    popup.menu.findItem(R.id.delete).isVisible = false
                    popup.menu.findItem(R.id.show).isVisible = false
                    popup.menu.findItem(R.id.report).isVisible = PrefManager.isLogin
                    popup.setOnMenuItemClickListener(this@AppAdapter)
                    popup.show()
                }
                viewHolder
            }

            10 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_feed_reply, parent, false)
                val viewHolder = FeedReplyViewHolder(view)
                viewHolder.itemView.setOnLongClickListener {
                    IntentUtil.startActivity<CopyActivity>(parent.context) {
                        putExtra("text", viewHolder.message.text.toString())
                    }
                    true
                }
                viewHolder.avatar.setOnClickListener {
                    IntentUtil.startActivity<UserActivity>(parent.context) {
                        putExtra("id", viewHolder.uid)
                    }
                }
                viewHolder.uname.setOnClickListener {
                    IntentUtil.startActivity<UserActivity>(parent.context) {
                        putExtra("id", viewHolder.uid)
                    }
                }
                viewHolder.feed.setOnClickListener {
                    IntentUtil.startActivity<FeedActivity>(parent.context) {
                        putExtra("id", viewHolder.feedId)
                    }
                    if (PrefManager.isRecordHistory)
                        HistoryUtil.saveHistory(
                            viewHolder.id,
                            viewHolder.uid,
                            viewHolder.uname.text.toString(),
                            viewHolder.avatarUrl,
                            viewHolder.device.text.toString(),
                            viewHolder.message.text.toString(),
                            viewHolder.pubDataRaw
                        )
                }
                viewHolder.like.setOnClickListener {
                    if (PrefManager.SZLMID == "") {
                        Toast.makeText(mContext, "数字联盟ID不能为空", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        appListener?.onPostLike(
                            viewHolder.entityType,
                            viewHolder.isLike,
                            viewHolder.id,
                            viewHolder.bindingAdapterPosition
                        )
                    }
                }
                viewHolder.expand.setOnClickListener {
                    entityType = viewHolder.entityType
                    fid = viewHolder.id
                    uid = viewHolder.uid
                    position = viewHolder.bindingAdapterPosition
                    val popup = PopupMenu(mContext, it)
                    val inflater = popup.menuInflater
                    inflater.inflate(R.menu.feed_reply_menu, popup.menu)
                    popup.menu.findItem(R.id.copy).isVisible = false
                    popup.menu.findItem(R.id.delete).isVisible = PrefManager.uid == viewHolder.uid
                    popup.menu.findItem(R.id.show).isVisible = false
                    popup.menu.findItem(R.id.report).isVisible = PrefManager.isLogin
                    popup.setOnMenuItemClickListener(this@AppAdapter)
                    popup.show()
                }
                viewHolder
            }

            11 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_collection_list_item, parent, false)
                val viewHolder = CollectionViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    appListener?.onShowCollection(
                        viewHolder.id,
                        viewHolder.title.text.toString()
                    )
                }
                viewHolder
            }

            12 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_user, parent, false)
                val viewHolder = RecentHistoryViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    openLink(
                        parent.context,
                        viewHolder.url,
                        viewHolder.uname.text.toString().replace("话题: ", "").replace("数码: ", "")
                    )
                }
                viewHolder
            }

            13 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_home_image_square_scroll_card, parent, false)
                ImageSquareScrollCardViewHolder(view)
            }

            else -> throw IllegalArgumentException("entityType error: $entityType")
        }

    }

    class FootViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerLayout: FrameLayout = view.findViewById(R.id.footerLayout)
        val indicator: CircularProgressIndicator = view.findViewById(R.id.indicator)
        val noMore: TextView = view.findViewById(R.id.noMore)
        val retry: Button = view.findViewById(R.id.retry)
    }

    override fun getItemCount() = dataList.size + 1


    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {

            val viewType = getItemViewType(position)

            if (viewType == 2) {
                if (payloads[0] == "like") {
                    (holder as FeedViewHolder).like.text = dataList[position].likenum
                    holder.isLike = dataList[position].userAction?.like == 1
                    val drawableLike: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
                    drawableLike.setBounds(
                        0,
                        0,
                        holder.like.textSize.toInt(),
                        holder.like.textSize.toInt()
                    )
                    if (dataList[position].userAction?.like == 1) {
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
            } else if (viewType == 10) {
                if (payloads[0] == "like") {
                    (holder as FeedReplyViewHolder).like.text = dataList[position].likenum
                    holder.isLike = dataList[position].userAction?.like == 1
                    val drawableLike: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
                    drawableLike.setBounds(
                        0,
                        0,
                        holder.like.textSize.toInt(),
                        holder.like.textSize.toInt()
                    )
                    if (dataList[position].userAction?.like == 1) {
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

    @SuppressLint("UseCompatLoadingForDrawables", "RestrictedApi", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {

            is ImageSquareScrollCardViewHolder -> {
                val imageTextScrollCard = ArrayList<HomeFeedResponse.Entities>()
                for (element in dataList[position].entities) {
                    if (element.entityType == "picCategory")
                        imageTextScrollCard.add(element)
                }
                val mAdapter = ImageSquareScrollCardAdapter(mContext, imageTextScrollCard)
                val mLayoutManager = LinearLayoutManager(mContext)
                mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                holder.recyclerView.apply {
                    adapter = mAdapter
                    layoutManager = mLayoutManager
                    if (itemDecorationCount == 0)
                        addItemDecoration(LinearItemDecoration1(10.dp))
                }
            }

            is RecentHistoryViewHolder -> {
                val data = dataList[position]
                holder.targetType = data.targetType.toString()
                holder.url = data.url
                holder.id = data.id
                holder.uname.text = "${data.targetTypeTitle}: ${data.title}"
                holder.followBtn.visibility = View.GONE
                holder.follow.text = "${data.followNum}关注"
                holder.fans.text = if (data.targetType == "user") "${data.fansNum}关注"
                else "${data.commentNum}讨论"
                ImageUtil.showIMG(holder.avatar, data.logo)
            }

            is CollectionViewHolder -> {
                val data = dataList[position]

                holder.id = data.id
                holder.uid = data.uid
                holder.title.text = data.title
                if (!data.description.isNullOrEmpty()) {
                    holder.description.visibility = View.VISIBLE
                    holder.description.text = data.description
                } else holder.description.visibility = View.GONE
                holder.mode.text = if (data.isOpen == 0) "私密"
                else "公开"
                holder.followNum.text = "${data.followNum}人关注"
                holder.contentNum.text = "${data.itemNum}个内容"
                if (!data.coverPic.isNullOrEmpty()) {
                    holder.cover.visibility = View.VISIBLE
                    ImageUtil.showIMG(holder.cover, data.coverPic)
                } else holder.cover.visibility = View.GONE

            }

            is FeedReplyViewHolder -> {
                val feed = dataList[position]

                holder.entityType = feed.entityType
                holder.id = feed.id
                holder.uid = feed.uid
                holder.avatarUrl = feed.userAvatar
                holder.pubDataRaw = feed.dateline.toString()
                holder.uname.text = feed.userInfo?.username
                ImageUtil.showIMG(holder.avatar, feed.userAvatar)

                if (!feed.messageTitle.isNullOrEmpty()) {
                    holder.messageTitle.visibility = View.VISIBLE
                    holder.messageTitle.text = feed.messageTitle
                } else
                    holder.messageTitle.visibility = View.GONE
                if (!feed.deviceTitle.isNullOrEmpty()) {
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
                    holder.device.visibility = View.INVISIBLE
                }

                if (feed.message == "") {
                    holder.message.visibility = View.GONE
                } else {
                    holder.message.visibility = View.VISIBLE
                    holder.message.movementMethod =
                        LinkTextView.LocalLinkMovementMethod.getInstance()
                    holder.message.text = SpannableStringBuilderUtil.setText(
                        mContext,
                        feed.message,
                        (holder.message.textSize * 1.3).toInt(),
                        null
                    )
                }

                if (feed.feed != null) {
                    holder.feed.visibility = View.VISIBLE

                    val feedUserName =
                        """<a class="feed-link-uname" href="/u/${feed.feed.uid}">@${feed.feed.username} </a>"""
                    holder.feedUname.movementMethod = LinkMovementMethod.getInstance()
                    holder.feedUname.text = SpannableStringBuilderUtil.setText(
                        mContext,
                        feedUserName,
                        holder.feedUname.textSize.toInt(),
                        null
                    )
                    holder.feedId = feed.feed.id
                    holder.feedUid = feed.feed.uid
                    if (feed.feed.message == "") {
                        holder.feedMessage.visibility = View.GONE
                    } else {
                        holder.feedMessage.visibility = View.VISIBLE
                        holder.feedMessage.movementMethod =
                            LinkTextView.LocalLinkMovementMethod.getInstance()
                        holder.feedMessage.text = SpannableStringBuilderUtil.setText(
                            mContext,
                            feed.feed.message,
                            (holder.feedMessage.textSize * 1.3).toInt(),
                            null
                        )
                    }
                } else holder.feed.visibility = View.GONE

                val drawable1: Drawable = mContext.getDrawable(R.drawable.ic_date)!!
                drawable1.setBounds(
                    0,
                    0,
                    holder.pubDate.textSize.toInt(),
                    holder.pubDate.textSize.toInt()
                )
                holder.pubDate.setCompoundDrawables(drawable1, null, null, null)
                if (feed.likeTime == null)
                    holder.pubDate.text = DateUtils.fromToday(feed.dateline)
                else
                    holder.pubDate.text = DateUtils.fromToday(feed.likeTime)

                holder.isLike = feed.userAction?.like == 1
                val drawableLike

                        : Drawable = mContext.getDrawable(R.drawable.ic_like)!!
                drawableLike.setBounds(
                    0,
                    0,
                    holder.like.textSize.toInt(),
                    holder.like.textSize.toInt()
                )
                if (feed.userAction?.like == 1) {
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
                holder.like.text = feed.likenum
                holder.like.setCompoundDrawables(drawableLike, null, null, null)

                holder.reply.text = feed.replynum

                val drawableReply
                        : Drawable = mContext.getDrawable(R.drawable.ic_message)!!
                drawableReply.setBounds(
                    0,
                    0,
                    holder.like.textSize.toInt(),
                    holder.like.textSize.toInt()
                )
                holder.reply.setCompoundDrawables(drawableReply, null, null, null)


            }

            is FeedVoteViewHolder -> {
                val feed = dataList[position]

                holder.feedType = feed.feedType
                holder.id = feed.id
                holder.uid = feed.uid
                holder.avatarUrl = feed.userAvatar
                holder.pubDataRaw = feed.dateline.toString()
                val name =
                    """<a class="feed-link-uname" href="/u/${feed.userInfo?.uid}">${feed.userInfo?.username}</a>""" + "\u3000"
                SpannableStringBuilderUtil.isColor = true
                holder.uname.text = SpannableStringBuilderUtil.setReply(
                    mContext,
                    name,
                    holder.uname.textSize.toInt(),
                    null
                )
                holder.uname.movementMethod = LinkTextView.LocalLinkMovementMethod.getInstance()
                SpannableStringBuilderUtil.isColor = false
                ImageUtil.showIMG(holder.avatar, feed.userAvatar)
                if (feed.vote?.totalOptionNum == 2) {
                    holder.optionNum.visibility = View.GONE
                    holder.voteNum.text =
                        "${feed.vote.totalVoteNum}人投票 · ${feed.vote.totalCommentNum}个观点"
                } else {
                    holder.optionNum.visibility = View.VISIBLE
                    holder.voteNum.text =
                        "${feed.vote!!.totalVoteNum}人投票 · ${feed.vote.totalCommentNum}个观点"
                    holder.optionNum.text = "共${feed.vote.totalOptionNum}个选项"
                }
                if (!feed.messageTitle.isNullOrEmpty()) {
                    holder.messageTitle.visibility = View.VISIBLE
                    holder.messageTitle.text = feed.messageTitle
                } else
                    holder.messageTitle.visibility = View.GONE
                if (!feed.deviceTitle.isNullOrEmpty()) {
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
                    holder.device.visibility = View.INVISIBLE
                }
                holder.pubDate.text = DateUtils.fromToday(feed.dateline)

                if (feed.message == "") {
                    holder.message.visibility = View.GONE
                } else {
                    holder.message.visibility = View.VISIBLE
                    holder.message.movementMethod =
                        LinkTextView.LocalLinkMovementMethod.getInstance()
                    holder.message.text = SpannableStringBuilderUtil.setText(
                        mContext,
                        feed.message,
                        (holder.message.textSize * 1.3).toInt(),
                        null
                    )
                }

                if (feed.vote.totalOptionNum == 2) {
                    holder.twoOptionsLayout.visibility = View.VISIBLE
                    holder.voteOptions.visibility = View.GONE
                    holder.leftOption.text = feed.vote.options[0].title
                    holder.rightOption.text = feed.vote.options[1].title
                } else {
                    holder.twoOptionsLayout.visibility = View.GONE
                    holder.voteOptions.visibility = View.VISIBLE

                    val optionList = ArrayList<String>()
                    repeat(3) {
                        optionList.add(feed.vote.options[0].title)
                        optionList.add(feed.vote.options[1].title)
                        optionList.add(feed.vote.options[2].title)
                    }
                    holder.voteOptions.adapter = object : BaseAdapter() {
                        override fun getCount() = 3
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
                            title.text = optionList[position1]
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

                if (feed.targetRow?.id == null && feed.relationRows.isNullOrEmpty())
                    holder.dyhLayout.visibility = View.GONE
                else {
                    holder.dyhLayout.visibility = View.VISIBLE
                    holder.linearAdapterLayout.adapter = object : BaseAdapter() {
                        override fun getCount(): Int =
                            if (feed.targetRow?.id == null) feed.relationRows!!.size
                            else 1 + feed.relationRows!!.size

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
                            val logo: ImageView = view.findViewById(R.id.iconMiniScrollCard)
                            val title: TextView = view.findViewById(R.id.title)
                            val type: String
                            val id: String
                            val url: String
                            if (feed.targetRow?.id != null) {
                                if (position == 0) {
                                    type = feed.targetRow.targetType.toString()
                                    id = feed.targetRow.id
                                    url = feed.targetRow.url
                                    title.text = feed.targetRow.title
                                    ImageUtil.showIMG(logo, feed.targetRow.logo)
                                } else {
                                    val layoutParams = ConstraintLayout.LayoutParams(
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    layoutParams.setMargins(5.dp, 0, 0, 0)
                                    view.layoutParams = layoutParams
                                    type = feed.relationRows!![position - 1].entityType
                                    id = feed.relationRows[position - 1].id
                                    url = feed.relationRows[position - 1].url
                                    title.text = feed.relationRows[position - 1].title
                                    ImageUtil.showIMG(
                                        logo,
                                        feed.relationRows[position - 1].logo
                                    )
                                }
                            } else {
                                if (position == 0) {
                                    type = feed.relationRows!![0].entityType
                                    id = feed.relationRows[0].id
                                    title.text = feed.relationRows[0].title
                                    url = feed.relationRows[0].url
                                    ImageUtil.showIMG(logo, feed.relationRows[0].logo)
                                } else {
                                    val layoutParams = ConstraintLayout.LayoutParams(
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    layoutParams.setMargins(5.dp, 0, 0, 0)
                                    view.layoutParams = layoutParams
                                    type = feed.relationRows!![position].entityType
                                    id = feed.relationRows[position].id
                                    url = feed.relationRows[position].url
                                    title.text = feed.relationRows[position].title
                                    ImageUtil.showIMG(
                                        logo,
                                        feed.relationRows[position].logo
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

            is AppViewHolder -> {
                val app = dataList[position]
                holder.apkName = app.apkname
                holder.title.text = app.title
                holder.commentNum.text = app.commentCount + "讨论"
                holder.hotNum.text = app.downCount + "下载"
                ImageUtil.showIMG(holder.logo, app.logo)
            }

            is TopicProductViewHolder -> {
                val topic = dataList[position]
                if (topic.description == "home") {
                    holder.parent.setCardBackgroundColor(
                        ThemeUtils.getThemeAttrColor(
                            mContext,
                            android.R.attr.windowBackground
                        )
                    )
                } else {
                    holder.parent.setCardBackgroundColor(
                        mContext.getColor(R.color.home_card_background_color)
                    )
                }
                holder.title.text = topic.title
                holder.id = topic.id
                holder.url = topic.url
                holder.hotNum.text = topic.hotNumTxt + "热度"
                holder.commentNum.text =
                    if (topic.entityType == "topic") topic.commentnumTxt + "讨论"
                    else topic.feedCommentNumTxt + "讨论"
                ImageUtil.showIMG(holder.logo, topic.logo)
                if (topic.entityType == "product")
                    holder.aliasTitle = topic.aliasTitle
                holder.entityType = topic.entityType
            }

            is UserViewHolder -> {
                val user = dataList[position]
                if (user.userInfo != null && user.fUserInfo != null) {
                    holder.uid = user.userInfo.uid
                    holder.uname.text = user.userInfo.username
                    holder.follow.text = "${user.userInfo.follow}关注"
                    holder.fans.text = "${user.userInfo.fans}粉丝"
                    holder.act.text = DateUtils.fromToday(user.userInfo.logintime) + "活跃"
                    ImageUtil.showIMG(holder.avatar, user.userInfo.userAvatar)
                } else if (user.userInfo == null && user.fUserInfo != null) {
                    holder.uid = user.fUserInfo.uid
                    holder.uname.text = user.fUserInfo.username
                    holder.follow.text = "${user.fUserInfo.follow}关注"
                    holder.fans.text = "${user.fUserInfo.fans}粉丝"
                    holder.act.text = DateUtils.fromToday(user.fUserInfo.logintime) + "活跃"
                    ImageUtil.showIMG(holder.avatar, user.fUserInfo.userAvatar)
                } else if (user.userInfo != null) {
                    holder.uid = user.uid
                    holder.uname.text = user.username
                    holder.follow.text = "${user.follow}关注"
                    holder.fans.text = "${user.fans}粉丝"
                    holder.act.text = DateUtils.fromToday(user.logintime) + "活跃"
                    holder.isFollow = user.isFollow == 1
                    if (user.isFollow == 0) {
                        holder.followBtn.text = "关注"
                        holder.followBtn.setTextColor(
                            ThemeUtils.getThemeAttrColor(
                                mContext,
                                rikka.preference.simplemenu.R.attr.colorPrimary
                            )
                        )
                    } else {
                        holder.followBtn.text = "已关注"
                        holder.followBtn.setTextColor(mContext.getColor(android.R.color.darker_gray))
                    }
                    holder.followBtn.visibility = if (PrefManager.isLogin) View.VISIBLE
                    else View.GONE
                    ImageUtil.showIMG(holder.avatar, user.userAvatar)
                }
            }

            is RefreshCardViewHolder -> {
                holder.textView.text = dataList[position].title
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

            is ImageCarouselCardViewHolder -> {
                val imageCarouselCard: MutableList<HomeFeedResponse.Entities> = ArrayList()
                for (element in dataList[position].entities) {
                    if (element.url.contains("http"))
                        continue
                    else
                        imageCarouselCard.add(element)
                }
                val data: MutableList<IconLinkGridCardBean> = ArrayList()
                data.add(
                    IconLinkGridCardBean(
                        imageCarouselCard[imageCarouselCard.size - 1].title,
                        imageCarouselCard[imageCarouselCard.size - 1].pic,
                        imageCarouselCard[imageCarouselCard.size - 1].url
                    )
                )
                for (element in imageCarouselCard) {
                    data.add(
                        IconLinkGridCardBean(
                            element.title,
                            element.pic,
                            element.url
                        )
                    )
                }
                data.add(
                    IconLinkGridCardBean(
                        imageCarouselCard[0].title,
                        imageCarouselCard[0].pic,
                        imageCarouselCard[0].url
                    )
                )
                val adapter = ImageCarouselCardAdapter(data)
                var currentPosition = 0
                holder.viewPager.adapter = adapter
                holder.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        currentPosition = position
                    }

                    override fun onPageScrollStateChanged(state: Int) {
                        if (state == ViewPager2.SCROLL_STATE_IDLE) {
                            if (currentPosition == 0) {
                                holder.viewPager.setCurrentItem(data.size - 2, false)
                            } else if (currentPosition == data.size - 1) {
                                holder.viewPager.setCurrentItem(1, false)
                            }
                        }
                    }
                })
                holder.viewPager.setCurrentItem(1, false)
            }

            is IconLinkGridCardViewHolder -> {
                val iconLinkGridCardList = dataList[position].entities
                val data: MutableList<IconLinkGridCardBean> = ArrayList()
                val maps: MutableList<List<IconLinkGridCardBean>> = ArrayList()
                for (element in iconLinkGridCardList) {
                    data.add(IconLinkGridCardBean(element.title, element.pic, element.url))
                }
                val page = iconLinkGridCardList.size / 5
                var index = 0
                repeat(page) {
                    maps.add(data.subList(index * 5, (index + 1) * 5))
                    index++
                }
                val adapter = IconLinkGridCardAdapter(mContext, maps)
                holder.viewPager.adapter = adapter
                if (page < 2) holder.indicator.visibility = View.GONE
                else {
                    holder.indicator.visibility = View.VISIBLE
                    holder.indicator.setViewPager(holder.viewPager)
                }

            }

            is FeedViewHolder -> {
                val feed = dataList[position]
                holder.feedType = feed.feedType
                holder.id = feed.id
                holder.uid = feed.uid
                holder.isLike = feed.userAction?.like == 1
                holder.avatarUrl = feed.userAvatar
                holder.pubDataRaw = feed.dateline.toString()
                val name =
                    """<a class="feed-link-uname" href="/u/${feed.userInfo?.uid}">${feed.userInfo?.username}</a>""" + "\u3000"
                SpannableStringBuilderUtil.isColor = true
                holder.uname.text = SpannableStringBuilderUtil.setReply(
                    mContext,
                    name,
                    holder.uname.textSize.toInt(),
                    null
                )
                holder.uname.movementMethod = LinkTextView.LocalLinkMovementMethod.getInstance()
                SpannableStringBuilderUtil.isColor = false
                ImageUtil.showIMG(holder.avatar, feed.userAvatar)
                if (!feed.messageTitle.isNullOrEmpty()) {
                    holder.messageTitle.visibility = View.VISIBLE
                    holder.messageTitle.text = feed.messageTitle
                } else
                    holder.messageTitle.visibility = View.GONE
                if (!feed.deviceTitle.isNullOrEmpty()) {
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
                    holder.device.visibility = View.INVISIBLE
                }
                holder.pubDate.text = DateUtils.fromToday(feed.dateline)
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
                if (feed.userAction?.like == 1) {
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
                holder.like.text = feed.likenum
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

                holder.message.movementMethod = LinkTextView.LocalLinkMovementMethod.getInstance()
                holder.message.text = SpannableStringBuilderUtil.setText(
                    mContext,
                    feed.message,
                    (holder.message.textSize * 1.3).toInt(),
                    null
                )

                if (!feed.picArr.isNullOrEmpty()) {
                    holder.multiImage.visibility = View.VISIBLE
                    if (feed.picArr.size == 1 || feed.feedType == "feedArticle") {
                        val from = feed.pic.lastIndexOf("@")
                        val middle = feed.pic.lastIndexOf("x")
                        val end = feed.pic.lastIndexOf(".")
                        if (from != -1 && middle != -1 && end != -1) {
                            val width = feed.pic.substring(from + 1, middle).toInt()
                            val height = feed.pic.substring(middle + 1, end).toInt()
                            holder.multiImage.imgHeight = height
                            holder.multiImage.imgWidth = width
                        }
                    }
                    holder.multiImage.apply {
                        val urlList: MutableList<String> = ArrayList()
                        if (feed.feedType == "feedArticle" && imgWidth > imgHeight)
                            urlList.add("${feed.pic}.s.jpg")
                        else
                            for (element in feed.picArr)
                                urlList.add("$element.s.jpg")
                        setUrlList(urlList)
                    }
                } else {
                    holder.multiImage.visibility = View.GONE
                }

                if (!feed.replyRows.isNullOrEmpty()) {
                    if (BlackListUtil.checkUid(feed.replyRows[0].uid)) {
                        holder.hotReply.visibility = View.GONE
                        return
                    }
                    holder.hotReply.visibility = View.VISIBLE
                    holder.hotReply.highlightColor = Color.TRANSPARENT
                    val mess =
                        if (feed.replyRows[0].picArr.isNullOrEmpty())
                            "<a class=\"feed-link-uname\" href=\"/u/${feed.replyRows[0].uid}\">${feed.replyRows[0].userInfo?.username}</a>: ${feed.replyRows[0].message}"
                        else if (feed.replyRows[0].message == "[图片]")
                            "<a class=\"feed-link-uname\" href=\"/u/${feed.replyRows[0].uid}\">${feed.replyRows[0].userInfo?.username}</a>: ${feed.replyRows[0].message} <a class=\"feed-forward-pic\" href=${feed.replyRows[0].pic}>查看图片(${feed.replyRows[0].picArr?.size})</a>"
                        else
                            "<a class=\"feed-link-uname\" href=\"/u/${feed.replyRows[0].uid}\">${feed.replyRows[0].userInfo?.username}</a>: ${feed.replyRows[0].message} [图片] <a class=\"feed-forward-pic\" href=${feed.replyRows[0].pic}>查看图片(${feed.replyRows[0].picArr?.size})</a>"
                    holder.hotReply.movementMethod = LinkMovementMethod.getInstance()
                    holder.hotReply.text = SpannableStringBuilderUtil.setText(
                        mContext,
                        mess,
                        (holder.hotReply.textSize * 1.3).toInt(),
                        feed.replyRows[0].picArr
                    )
                    SpannableStringBuilderUtil.isReturn = true
                } else
                    holder.hotReply.visibility = View.GONE

                if (feed.forwardSourceFeed != null) {
                    holder.forwardedId = feed.forwardSourceFeed.id
                    holder.forwarded.visibility = View.VISIBLE
                    val forwardedMess =
                        "<a class=\"feed-link-uname\" href=\"/u/${feed.forwardSourceFeed.uid}\">@${feed.forwardSourceFeed.username}</a>: ${feed.forwardSourceFeed.message}"
                    holder.forwardedMess.movementMethod =
                        LinkTextView.LocalLinkMovementMethod.getInstance()
                    holder.forwardedMess.text = SpannableStringBuilderUtil.setText(
                        mContext,
                        forwardedMess,
                        (holder.forwardedMess.textSize * 1.3).toInt(),
                        feed.forwardSourceFeed.picArr
                    )
                    if (!feed.forwardSourceFeed.picArr.isNullOrEmpty()) {
                        holder.forwardedPic.visibility = View.VISIBLE
                        if (feed.forwardSourceFeed.picArr.size == 1 || feed.forwardSourceFeed.feedType == "feedArticle") {
                            val from = feed.forwardSourceFeed.pic.lastIndexOf("@")
                            val middle = feed.forwardSourceFeed.pic.lastIndexOf("x")
                            val end = feed.forwardSourceFeed.pic.lastIndexOf(".")
                            if (from != -1 && middle != -1 && end != -1) {
                                val width =
                                    feed.forwardSourceFeed.pic.substring(from + 1, middle).toInt()
                                val height =
                                    feed.forwardSourceFeed.pic.substring(middle + 1, end).toInt()
                                holder.forwardedPic.imgHeight = height
                                holder.forwardedPic.imgWidth = width
                            }
                        }
                        holder.forwardedPic.apply {
                            val urlList: MutableList<String> = ArrayList()
                            if (feed.forwardSourceFeed.feedType == "feedArticle" && imgWidth > imgHeight)
                                urlList.add("${feed.forwardSourceFeed.pic}.s.jpg")
                            else
                                for (element in feed.forwardSourceFeed.picArr)
                                    urlList.add("$element.s.jpg")
                            setUrlList(urlList)
                        }
                    } else
                        holder.forwardedPic.visibility = View.GONE
                } else
                    holder.forwarded.visibility = View.GONE

                if (feed.targetRow?.id == null && feed.relationRows.isNullOrEmpty())
                    holder.dyhLayout.visibility = View.GONE
                else {
                    holder.dyhLayout.visibility = View.VISIBLE
                    holder.linearAdapterLayout.adapter = object : BaseAdapter() {
                        override fun getCount(): Int =
                            if (feed.targetRow?.id == null) feed.relationRows!!.size
                            else 1 + feed.relationRows!!.size

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
                            val logo: ImageView = view.findViewById(R.id.iconMiniScrollCard)
                            val title: TextView = view.findViewById(R.id.title)
                            val type: String
                            val id: String
                            val url: String
                            if (feed.targetRow?.id != null) {
                                if (position == 0) {
                                    type = feed.targetRow.targetType.toString()
                                    id = feed.targetRow.id
                                    url = feed.targetRow.url
                                    title.text = feed.targetRow.title
                                    ImageUtil.showIMG(logo, feed.targetRow.logo)
                                } else {
                                    val layoutParams = ConstraintLayout.LayoutParams(
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    layoutParams.setMargins(5.dp, 0, 0, 0)
                                    view.layoutParams = layoutParams
                                    type = feed.relationRows!![position - 1].entityType
                                    id = feed.relationRows[position - 1].id
                                    url = feed.relationRows[position - 1].url
                                    title.text = feed.relationRows[position - 1].title
                                    ImageUtil.showIMG(
                                        logo,
                                        feed.relationRows[position - 1].logo
                                    )
                                }
                            } else {
                                if (position == 0) {
                                    type = feed.relationRows!![0].entityType
                                    id = feed.relationRows[0].id
                                    title.text = feed.relationRows[0].title
                                    url = feed.relationRows[0].url
                                    ImageUtil.showIMG(logo, feed.relationRows[0].logo)
                                } else {
                                    val layoutParams = ConstraintLayout.LayoutParams(
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    layoutParams.setMargins(5.dp, 0, 0, 0)
                                    view.layoutParams = layoutParams
                                    type = feed.relationRows!![position].entityType
                                    id = feed.relationRows[position].id
                                    url = feed.relationRows[position].url
                                    title.text = feed.relationRows[position].title
                                    ImageUtil.showIMG(
                                        logo,
                                        feed.relationRows[position].logo
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

            is ImageTextScrollCardViewHolder -> {
                val imageTextScrollCard = ArrayList<HomeFeedResponse.Entities>()
                for (element in dataList[position].entities) {
                    if (element.entityType == "feed")
                        imageTextScrollCard.add(element)
                }
                val mAdapter = ImageTextScrollCardAdapter(mContext, imageTextScrollCard)
                val mLayoutManager = LinearLayoutManager(mContext)
                mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                holder.title.text = dataList[position].title
                holder.title.setPadding(10.dp, 10.dp, 10.dp, 0)
                /*val drawable: Drawable = mContext.getDrawable(R.drawable.ic_forward)!!
                drawable.setBounds(
                    0,
                    0,
                    holder.title.textSize.toInt(),
                    holder.title.textSize.toInt()
                )
                holder.title.setCompoundDrawables(null, null, drawable, null)*/
                holder.recyclerView.apply {
                    adapter = mAdapter
                    layoutManager = mLayoutManager
                    if (itemDecorationCount == 0)
                        addItemDecoration(LinearItemDecoration1(10.dp))
                }
            }

            is IconMiniScrollCardViewHolder -> {
                val imageTextScrollCard = ArrayList<HomeFeedResponse.Entities>()
                for (element in dataList[position].entities) {
                    if ((element.entityType == "topic" || element.entityType == "product")
                        && !TopicBlackListUtil.checkTopic(element.title)
                    )
                        imageTextScrollCard.add(element)
                }
                val mAdapter = IconMiniScrollCardAdapter(imageTextScrollCard)
                val mLayoutManager = LinearLayoutManager(mContext)
                mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                holder.recyclerView.apply {
                    adapter = mAdapter
                    layoutManager = mLayoutManager
                    if (itemDecorationCount == 0)
                        addItemDecoration(LinearItemDecoration1(10.dp))
                }
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) -1
        else when (dataList[position].entityType) {
            "card" -> {
                when (dataList[position].entityTemplate) {
                    "imageCarouselCard_1" -> 0
                    "iconLinkGridCard" -> 1
                    "imageTextScrollCard" -> 3

                    "iconMiniScrollCard" -> 4
                    "iconMiniGridCard" -> 4

                    "refreshCard" -> 5

                    "imageSquareScrollCard" -> 13

                    else -> throw IllegalArgumentException("entityType error: ${dataList[position].entityTemplate}")
                }
            }

            "feed" -> when (dataList[position].feedType) {
                "vote" -> 9
                else -> 2
            }

            "contacts" -> 6
            "user" -> 6

            "topic" -> 7
            "product" -> 7

            "apk" -> 8

            "feed_reply" -> 10

            "collection" -> 11

            "recentHistory" -> 12

            // max 13
            else -> throw IllegalArgumentException("entityType error: ${dataList[position].entityType}")
        }
    }

    override fun onMenuItemClick(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.block -> {
                BlackListUtil.saveUid(uid)
                dataList.removeAt(position)
                notifyItemRemoved(position)
            }

            R.id.report -> {
                IntentUtil.startActivity<WebViewActivity>(mContext) {
                    putExtra(
                        "url",
                        if (entityType == "feed_reply")
                            "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed_reply&id=$fid"
                        else
                            "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed&id=$fid"
                    )
                }
            }

            R.id.delete -> {
                appListener?.onDeleteFeedReply(fid, position, null)
            }
        }
        return false
    }

}