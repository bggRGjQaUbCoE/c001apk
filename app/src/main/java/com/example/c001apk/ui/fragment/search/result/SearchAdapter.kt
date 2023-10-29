package com.example.c001apk.ui.fragment.search.result

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.ThemeUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.ImagePreview
import cc.shinichi.library.bean.ImageInfo
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.app.AppActivity
import com.example.c001apk.ui.activity.feed.FeedActivity
import com.example.c001apk.ui.activity.topic.TopicActivity
import com.example.c001apk.ui.activity.user.UserActivity
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.PubDateUtil
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.NineImageView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.CircularProgressIndicator

class SearchAdapter(
    private val mContext: Context,
    private val type: String,
    private val searchList: List<HomeFeedResponse.Data>
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

    class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val reply: TextView = view.findViewById(R.id.reply)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val device: TextView = view.findViewById(R.id.device)
        var id = ""
        var uid = ""
        var isLike = false
        val multiImage: NineImageView = view.findViewById(R.id.multiImage)
    }

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val hotNum: TextView = view.findViewById(R.id.hotNum)
        val commentNum: TextView = view.findViewById(R.id.commentNum)
        val logo: ShapeableImageView = view.findViewById(R.id.logo)
        var entityType = ""
        var apkName = ""
    }

    class ProductTopicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val hotNum: TextView = view.findViewById(R.id.hotNum)
        val commentNum: TextView = view.findViewById(R.id.commentNum)
        val logo: ShapeableImageView = view.findViewById(R.id.logo)
        var entityType = ""
        var aliasTitle = ""
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        val follow: TextView = view.findViewById(R.id.follow)
        val fans: TextView = view.findViewById(R.id.fans)
        val act: TextView = view.findViewById(R.id.act)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        var uid = ""
    }

    class FootViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerLayout: FrameLayout = view.findViewById(R.id.footerLayout)
        val indicator: CircularProgressIndicator = view.findViewById(R.id.indicator)
        val noMore: TextView = view.findViewById(R.id.noMore)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            0 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_feed, parent, false)
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
                            viewHolder.isLike,
                            viewHolder.id,
                            viewHolder.adapterPosition
                        )
                    }
                }
                viewHolder
            }

            1 -> {
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

            3 -> {
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

            2 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_topic, parent, false)
                val viewHolder = ProductTopicViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    val intent = Intent(parent.context, TopicActivity::class.java)
                    intent.putExtra(
                        "title",
                        if (viewHolder.entityType == "product")
                            viewHolder.aliasTitle
                        else viewHolder.title.text
                    )
                    parent.context.startActivity(intent)
                }
                viewHolder
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_rv_footer, parent, false)
                FootViewHolder(view)
            }

        }

    }

    override fun getItemCount() = searchList.size + 1

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

            is FeedViewHolder -> {
                val feed = searchList[position]
                holder.id = feed.id
                holder.uid = feed.uid
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

                holder.message.movementMethod = LinkMovementMethod.getInstance()
                holder.message.text = SpannableStringBuilderUtil.setText(
                    mContext,
                    feed.message,
                    (holder.message.textSize * 1.3).toInt()
                )

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
                ImageShowUtil.showAvatar(holder.avatar, feed.userAvatar)

                if (!feed.picArr.isNullOrEmpty()) {
                    holder.multiImage.visibility = View.VISIBLE
                    val imageUrls = ArrayList<String>()
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
            }

            is AppViewHolder -> {
                val app = searchList[position]
                holder.apkName = app.apkname
                holder.title.text = app.title
                holder.commentNum.text = app.commentCount + "讨论"
                holder.hotNum.text = app.downCount + "下载"
                ImageShowUtil.showIMG(holder.logo, app.logo)
            }

            is UserViewHolder -> {
                val user = searchList[position]
                holder.uid = user.uid
                holder.uname.text = user.username
                holder.follow.text = "${user.follow}关注"
                holder.fans.text = "${user.fans}粉丝"
                holder.act.text = PubDateUtil.time(user.logintime) + "活跃"
                ImageShowUtil.showAvatar(holder.avatar, user.userAvatar)
            }

            is ProductTopicViewHolder -> {
                val topic = searchList[position]
                holder.title.text = topic.title
                holder.hotNum.text = topic.hotNumTxt + "热度"
                holder.commentNum.text =
                    if (topic.entityType == "topic") topic.commentnumTxt + "讨论"
                    else topic.feedCommentNumTxt + "讨论"
                ImageShowUtil.showIMG(holder.logo, topic.logo)
                if (topic.entityType == "product")
                    holder.aliasTitle = topic.aliasTitle
                holder.entityType = topic.entityType
            }
        }


    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) -1
        else when (type) {
            "feed" -> 0
            "apk" -> 1
            "product" -> 2
            "user" -> 3
            else -> 2 // "topic"
        }
    }

}