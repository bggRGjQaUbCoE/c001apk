package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.ThemeUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.AppActivity
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.DyhActivity
import com.example.c001apk.ui.activity.FeedActivity
import com.example.c001apk.ui.activity.TopicActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.LinearItemDecoration1
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.PubDateUtil
import com.example.c001apk.util.SpacesItemDecoration
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.LinearAdapterLayout
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.CircularProgressIndicator


class AppAdapter(
    private val mContext: Context,
    private val dataList: ArrayList<HomeFeedResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onImageItemClickListener: OnImageItemClickListener? = null

    fun setOnImageItemClickListener(onImageItemClickListener: OnImageItemClickListener) {
        this.onImageItemClickListener = onImageItemClickListener
    }

    private var iOnLikeClickListener: IOnLikeClickListener? = null

    fun setIOnLikeReplyListener(iOnLikeClickListener: IOnLikeClickListener) {
        this.iOnLikeClickListener = iOnLikeClickListener
    }

    private var loadState = 2
    val LOADING = 1
    val LOADING_COMPLETE = 2
    val LOADING_END = 3

    @SuppressLint("NotifyDataSetChanged")
    fun setLoadState(loadState: Int) {
        this.loadState = loadState
        notifyDataSetChanged()
    }

    class ImageCarouselCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    class IconLinkGridCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val uname: TextView = view.findViewById(R.id.uname)
        val from: TextView = view.findViewById(R.id.from)
        val device: TextView = view.findViewById(R.id.device)
        val message: TextView = view.findViewById(R.id.message)
        val multiImage: NineGridImageView = view.findViewById(R.id.multiImage)
        var id = ""
        var uid = ""
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        var isLike = false
        val reply: TextView = view.findViewById(R.id.reply)
        val linearAdapterLayout: LinearAdapterLayout = view.findViewById(R.id.linearAdapterLayout)
    }

    class ImageTextScrollCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    class IconMiniScrollCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
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
        val avatar: ImageView = view.findViewById(R.id.avatar)
        var uid = ""
    }

    class TopicProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            -1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_rv_footer, parent, false)
                FootViewHolder(view)
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
                viewHolder.like.setOnClickListener {
                    if (PrefManager.isLogin) {
                        iOnLikeClickListener?.onPostLike(
                            null,
                            viewHolder.isLike,
                            viewHolder.id,
                            viewHolder.bindingAdapterPosition
                        )
                    }
                }
                viewHolder.multiImage.apply {
                    onImageItemClickListener = this@AppAdapter.onImageItemClickListener
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
                        .inflate(R.layout.item_home_image_text_scroll_card, parent, false)
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
                    val intent = Intent(parent.context, UserActivity::class.java)
                    intent.putExtra("id", viewHolder.uname.text)
                    parent.context.startActivity(intent)
                }
                viewHolder
            }

            7 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_topic, parent, false)
                val viewHolder = TopicProductViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    val intent = Intent(parent.context, TopicActivity::class.java)
                    intent.putExtra("type", viewHolder.entityType)
                    intent.putExtra("title", viewHolder.title.text)
                    intent.putExtra("url", viewHolder.url)
                    intent.putExtra("id", viewHolder.id)
                    parent.context.startActivity(intent)
                }
                viewHolder
            }

            8 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_topic, parent, false)
                val viewHolder = AppViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    val intent = Intent(parent.context, AppActivity::class.java)
                    intent.putExtra("id", viewHolder.apkName)
                    parent.context.startActivity(intent)
                }
                viewHolder
            }

            else -> throw IllegalArgumentException("entityType error")
        }

    }

    class FootViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerLayout: FrameLayout = view.findViewById(R.id.footerLayout)
        val indicator: CircularProgressIndicator = view.findViewById(R.id.indicator)
        val noMore: TextView = view.findViewById(R.id.noMore)
    }

    override fun getItemCount() = dataList.size + 1

    @SuppressLint("UseCompatLoadingForDrawables", "RestrictedApi", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {

            is AppViewHolder -> {
                val app = dataList[position]
                holder.apkName = app.apkname
                holder.title.text = app.title
                holder.commentNum.text = app.commentCount + "讨论"
                holder.hotNum.text = app.downCount + "下载"
                ImageShowUtil.showIMG(holder.logo, app.logo)
            }

            is TopicProductViewHolder -> {
                val topic = dataList[position]
                holder.title.text = topic.title
                holder.id = topic.id
                holder.url = topic.url
                holder.hotNum.text = topic.hotNumTxt + "热度"
                holder.commentNum.text =
                    if (topic.entityType == "topic") topic.commentnumTxt + "讨论"
                    else topic.feedCommentNumTxt + "讨论"
                ImageShowUtil.showIMG(holder.logo, topic.logo)
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
                    holder.act.text = PubDateUtil.time(user.userInfo.logintime) + "活跃"
                    ImageShowUtil.showAvatar(holder.avatar, user.userInfo.userAvatar)
                } else if (user.userInfo == null && user.fUserInfo != null) {
                    holder.uid = user.fUserInfo.uid
                    holder.uname.text = user.fUserInfo.username
                    holder.follow.text = "${user.fUserInfo.follow}关注"
                    holder.fans.text = "${user.fUserInfo.fans}粉丝"
                    holder.act.text = PubDateUtil.time(user.fUserInfo.logintime) + "活跃"
                    ImageShowUtil.showAvatar(holder.avatar, user.fUserInfo.userAvatar)
                } else if (user.userInfo != null && user.fUserInfo == null) {
                    holder.uid = user.uid
                    holder.uname.text = user.username
                    holder.follow.text = "${user.follow}关注"
                    holder.fans.text = "${user.fans}粉丝"
                    holder.act.text = PubDateUtil.time(user.logintime) + "活跃"
                    ImageShowUtil.showAvatar(holder.avatar, user.userAvatar)
                }
            }

            is RefreshCardViewHolder -> {
                holder.textView.text = dataList[position].title
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

                    else -> {}
                }
            }


            is ImageCarouselCardViewHolder -> {
                val imageCarouselCard = dataList[position].entities
                val mAdapter = ImageCarouselCardAdapter(imageCarouselCard)
                val mLayoutManager = LinearLayoutManager(mContext)
                mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                holder.recyclerView.onFlingListener = null
                PagerSnapHelper().attachToRecyclerView(holder.recyclerView)
                holder.recyclerView.apply {
                    adapter = mAdapter
                    layoutManager = mLayoutManager
                }
            }

            is IconLinkGridCardViewHolder -> {
                val iconLinkGridCard = dataList[position].entities
                val mAdapter = IconLinkGridCardAdapter(iconLinkGridCard)
                val mLayoutManager = GridLayoutManager(mContext, 5)
                val space = mContext.resources.getDimensionPixelSize(R.dimen.normal_space)
                val spaceValue = HashMap<String, Int>()
                spaceValue[SpacesItemDecoration.TOP_SPACE] = space
                spaceValue[SpacesItemDecoration.BOTTOM_SPACE] = space
                spaceValue[SpacesItemDecoration.LEFT_SPACE] = space
                spaceValue[SpacesItemDecoration.RIGHT_SPACE] = space
                holder.recyclerView.apply {
                    adapter = mAdapter
                    layoutManager = mLayoutManager
                    if (itemDecorationCount == 0)
                        addItemDecoration(SpacesItemDecoration(5, spaceValue, true))
                }
            }

            is FeedViewHolder -> {
                val feed = dataList[position]
                holder.id = feed.id
                holder.isLike = feed.userAction?.like == 1
                holder.uname.text = feed.username
                ImageShowUtil.showAvatar(holder.avatar, feed.userAvatar)
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
                    DrawableCompat.setTint(drawableLike, mContext.getColor(R.color.gray_bd))
                    holder.like.setTextColor(mContext.getColor(R.color.gray_bd))
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

                holder.message.movementMethod = LinkMovementMethod.getInstance()
                holder.message.text = SpannableStringBuilderUtil.setText(
                    mContext,
                    feed.message,
                    (holder.message.textSize * 1.3).toInt(),
                    null
                )

                if (!feed.picArr.isNullOrEmpty()) {
                    holder.multiImage.visibility = View.VISIBLE
                    if (feed.picArr.size == 1) {
                        val from = feed.pic.lastIndexOf("@")
                        val middle = feed.pic.lastIndexOf("x")
                        val end = feed.pic.lastIndexOf(".")
                        val width = feed.pic.substring(from + 1, middle).toInt()
                        val height = feed.pic.substring(middle + 1, end).toInt()
                        holder.multiImage.imgHeight = height
                        holder.multiImage.imgWidth = width
                    }
                    holder.multiImage.apply {
                        val urlList: MutableList<String> = ArrayList()
                        for (element in feed.picArr)
                            if (element.substring(element.length - 3, element.length) != "gif")
                                urlList.add("$element.s.jpg")
                            else urlList.add(element)
                        setUrlList(urlList)
                    }
                } else {
                    holder.multiImage.visibility = View.GONE
                }

                if (feed.targetRow?.id == null && feed.relationRows.isEmpty())
                    holder.linearAdapterLayout.visibility = View.GONE
                else {
                    holder.linearAdapterLayout.visibility = View.VISIBLE
                    holder.linearAdapterLayout.adapter = object : BaseAdapter() {
                        override fun getCount(): Int =
                            if (feed.targetRow?.id == null) feed.relationRows.size
                            else 1 + feed.relationRows.size

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
                            if (feed.targetRow?.id != null) {
                                if (position == 0) {
                                    type = feed.targetRow.targetType.toString()
                                    id = feed.targetRow.id
                                    url = feed.targetRow.url
                                    title.text = feed.targetRow.title
                                    ImageShowUtil.showIMG(logo, feed.targetRow.logo)
                                } else {
                                    val space =
                                        mContext.resources.getDimensionPixelSize(R.dimen.minor_space)
                                    val layoutParams = ConstraintLayout.LayoutParams(
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    layoutParams.setMargins(space, 0, 0, 0)
                                    view.layoutParams = layoutParams
                                    type = feed.relationRows[position - 1].entityType
                                    id = feed.relationRows[position - 1].id
                                    url = feed.relationRows[position - 1].url
                                    title.text = feed.relationRows[position - 1].title
                                    ImageShowUtil.showIMG(
                                        logo,
                                        feed.relationRows[position - 1].logo
                                    )
                                }
                            } else {
                                if (position == 0) {
                                    type = feed.relationRows[0].entityType
                                    id = feed.relationRows[0].id
                                    title.text = feed.relationRows[0].title
                                    url = feed.relationRows[0].url
                                    ImageShowUtil.showIMG(logo, feed.relationRows[0].logo)
                                } else {
                                    val space =
                                        mContext.resources.getDimensionPixelSize(R.dimen.minor_space)
                                    val layoutParams = ConstraintLayout.LayoutParams(
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    layoutParams.setMargins(space, 0, 0, 0)
                                    view.layoutParams = layoutParams
                                    type = feed.relationRows[position].entityType
                                    id = feed.relationRows[position].id
                                    url = feed.relationRows[position].url
                                    title.text = feed.relationRows[position].title
                                    ImageShowUtil.showIMG(
                                        logo,
                                        feed.relationRows[position].logo
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

            is ImageTextScrollCardViewHolder -> {
                val imageTextScrollCard = ArrayList<HomeFeedResponse.Entities>()
                for (element in dataList[position].entities) {
                    if (element.entityType == "feed")
                        imageTextScrollCard.add(element)
                }
                val mAdapter = ImageTextScrollCardAdapter(mContext, imageTextScrollCard)
                val mLayoutManager = LinearLayoutManager(mContext)
                mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                val space = mContext.resources.getDimensionPixelSize(R.dimen.normal_space)
                holder.title.text = dataList[position].title
                holder.title.setPadding(space, space, space, 0)
                val drawable: Drawable = mContext.getDrawable(R.drawable.ic_forward)!!
                drawable.setBounds(
                    0,
                    0,
                    holder.title.textSize.toInt(),
                    holder.title.textSize.toInt()
                )
                holder.title.setCompoundDrawables(null, null, drawable, null)
                holder.recyclerView.apply {
                    adapter = mAdapter
                    layoutManager = mLayoutManager
                    if (itemDecorationCount == 0)
                        addItemDecoration(LinearItemDecoration1(space))
                }
            }

            is IconMiniScrollCardViewHolder -> {
                val imageTextScrollCard = ArrayList<HomeFeedResponse.Entities>()
                for (element in dataList[position].entities) {
                    if (element.entityType == "topic" || element.entityType == "product")
                        imageTextScrollCard.add(element)
                }
                val mAdapter = IconMiniScrollCardAdapter(mContext, imageTextScrollCard)
                val mLayoutManager = LinearLayoutManager(mContext)
                mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                val space = mContext.resources.getDimensionPixelSize(R.dimen.normal_space)
                if (dataList[position].title == "")
                    holder.title.visibility = View.GONE
                else {
                    holder.title.text = dataList[position].title
                    holder.title.setPadding(space, space, space, 0)
                }
                val drawable: Drawable = mContext.getDrawable(R.drawable.ic_forward)!!
                drawable.setBounds(
                    0,
                    0,
                    holder.title.textSize.toInt(),
                    holder.title.textSize.toInt()
                )
                holder.title.setCompoundDrawables(null, null, drawable, null)
                holder.recyclerView.apply {
                    adapter = mAdapter
                    layoutManager = mLayoutManager
                    if (itemDecorationCount == 0)
                        addItemDecoration(LinearItemDecoration1(space))
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
                    else -> throw IllegalArgumentException("entityType error")
                }
            }

            "feed" -> 2

            "contacts" -> 6
            "user" -> 6

            "topic" -> 7
            "product" -> 7

            "apk" -> 8

            else -> throw IllegalArgumentException("entityType error")
        }
    }

}