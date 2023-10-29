package com.example.c001apk.ui.fragment.home.feed

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.ThemeUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.ImagePreview
import cc.shinichi.library.bean.ImageInfo
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.feed.FeedActivity
import com.example.c001apk.ui.activity.user.UserActivity
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.LinearItemDecoration1
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.PubDateUtil
import com.example.c001apk.util.SpacesItemDecoration
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.NineImageView
import com.google.android.material.progressindicator.CircularProgressIndicator


class HomeFeedAdapter(
    private val mContext: Context,
    private val homeFeedList: ArrayList<HomeFeedResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        val multiImage :NineImageView = view.findViewById(R.id.multiImage)
        var id = ""
        var uid = ""
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        var isLike = false
        val reply: TextView = view.findViewById(R.id.reply)
    }

    class ImageTextScrollCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    class IconMiniScrollCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
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
                    if (PrefManager.isLogin){
                        iOnLikeClickListener?.onPostLike(
                            viewHolder.isLike,
                            viewHolder.id,
                            viewHolder.adapterPosition
                        )
                    }
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

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_rv_footer, parent, false)
                FootViewHolder(view)
            }
        }

    }

    class FootViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerLayout: FrameLayout = view.findViewById(R.id.footerLayout)
        val indicator: CircularProgressIndicator = view.findViewById(R.id.indicator)
        val noMore: TextView = view.findViewById(R.id.noMore)
    }

    override fun getItemCount() = homeFeedList.size + 1

    @SuppressLint("UseCompatLoadingForDrawables", "RestrictedApi")
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


            is ImageCarouselCardViewHolder -> {
                val imageCarouselCard = homeFeedList[position].entities
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
                val iconLinkGridCard = homeFeedList[position].entities
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
                val feed = homeFeedList[position]
                holder.id = feed.id
                holder.isLike = feed.userAction?.like == 1
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
                holder.message.text = SpannableStringBuilderUtil.setText(mContext, feed.message, (holder.message.textSize*1.3).toInt())


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

            is ImageTextScrollCardViewHolder -> {
                val imageTextScrollCard = ArrayList<HomeFeedResponse.Entities>()
                for (element in homeFeedList[position].entities) {
                    if (element.entityType == "feed")
                        imageTextScrollCard.add(element)
                }
                val mAdapter = ImageTextScrollCardAdapter(mContext, imageTextScrollCard)
                val mLayoutManager = LinearLayoutManager(mContext)
                mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                val space = mContext.resources.getDimensionPixelSize(R.dimen.normal_space)
                holder.title.text = homeFeedList[position].title
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
                for (element in homeFeedList[position].entities) {
                    if (element.entityType == "topic" || element.entityType == "product")
                        imageTextScrollCard.add(element)
                }
                val mAdapter = IconMiniScrollCardAdapter(mContext, imageTextScrollCard)
                val mLayoutManager = LinearLayoutManager(mContext)
                mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                val space = mContext.resources.getDimensionPixelSize(R.dimen.normal_space)
                if (homeFeedList[position].title == "")
                    holder.title.visibility = View.GONE
                else {
                    holder.title.text = homeFeedList[position].title
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
        else when (homeFeedList[position].entityTemplate) {
            "imageCarouselCard_1" -> 0
            "iconLinkGridCard" -> 1
            "feed" -> 2
            "imageTextScrollCard" -> 3
            else -> 4 //"iconMiniScrollCard"
        }

        /*return when (position) {
            itemCount - 1 -> 5
            else -> 2
        }*/
    }

}