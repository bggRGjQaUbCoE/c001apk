package com.example.c001apk.adapter

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
import com.example.c001apk.R
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnReplyClickListener
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.google.android.material.progressindicator.CircularProgressIndicator


class Reply2ReplyTotalAdapter(
    private val mContext: Context,
    private val uid: String,
    private val position: Int,
    private val replyList: List<TotalReplyResponse.Data>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onImageItemClickListener: OnImageItemClickListener? = null

    fun setOnImageItemClickListener(onImageItemClickListener: OnImageItemClickListener) {
        this.onImageItemClickListener = onImageItemClickListener
    }

    private var iOnLikeClickListener: IOnLikeClickListener? = null

    fun setIOnLikeReplyListener(iOnLikeClickListener: IOnLikeClickListener) {
        this.iOnLikeClickListener = iOnLikeClickListener
    }

    private lateinit var iOnReplyClickListener: IOnReplyClickListener

    fun setIOnReplyClickListener(iOnReplyClickListener: IOnReplyClickListener) {
        this.iOnReplyClickListener = iOnReplyClickListener
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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        var id = ""
        var uid = ""
        var name = ""
        var isLike = false
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val reply: TextView = view.findViewById(R.id.reply)
        val multiImage: NineGridImageView = view.findViewById(R.id.multiImage)
    }

    class FootViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerLayout: FrameLayout = view.findViewById(R.id.footerLayout)
        val indicator: CircularProgressIndicator = view.findViewById(R.id.indicator)
        val noMore: TextView = view.findViewById(R.id.noMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_reply_to_reply_item, parent, false)
                val viewHolder = ViewHolder(view)
                viewHolder.avatar.setOnClickListener {
                    val intent = Intent(parent.context, UserActivity::class.java)
                    intent.putExtra("id", viewHolder.name)
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
                viewHolder.itemView.setOnClickListener {
                    iOnReplyClickListener.onReply2Reply(
                        position,
                        viewHolder.adapterPosition,
                        viewHolder.id,
                        viewHolder.uid,
                        viewHolder.name,
                        "reply"
                    )
                }
                viewHolder.message.setOnClickListener {
                    iOnReplyClickListener.onReply2Reply(
                        position,
                        viewHolder.adapterPosition,
                        viewHolder.id,
                        viewHolder.uid,
                        viewHolder.name,
                        "reply"
                    )
                }
                viewHolder.like.setOnClickListener {
                    if (PrefManager.isLogin) {
                        iOnLikeClickListener?.onPostLike(
                            null,
                            viewHolder.isLike,
                            viewHolder.id,
                            viewHolder.adapterPosition
                        )
                    }
                }
                viewHolder.multiImage.apply {
                    onImageItemClickListener = this@Reply2ReplyTotalAdapter.onImageItemClickListener
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

    override fun getItemCount() = replyList.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) -1
        else 0
    }

    @SuppressLint("SetTextI18n", "RestrictedApi", "UseCompatLoadingForDrawables")
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

            is ViewHolder -> {

                val reply = replyList[position]
                holder.id = reply.id
                holder.uid = reply.uid
                holder.name = reply.username
                holder.isLike = reply.userAction?.like == 1
                ImageShowUtil.showAvatar(holder.avatar, reply.userAvatar)

                val text =
                    if (uid == reply.ruid)
                        """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>"""
                    else
                        """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>回复<a class="feed-link-uname" href="/u/${reply.rusername}">${reply.rusername}</a>"""

                holder.uname.text = SpannableStringBuilderUtil.setText(
                    mContext,
                    text,
                    (holder.uname.textSize * 1.3).toInt(),
                    null
                )
                holder.uname.movementMethod = LinkMovementMethod.getInstance()

                holder.message.movementMethod = LinkMovementMethod.getInstance()
                holder.message.text = SpannableStringBuilderUtil.setText(
                    mContext,
                    reply.message,
                    (holder.message.textSize * 1.3).toInt(),
                    null
                )


                holder.pubDate.text = DateUtils.fromToday(reply.dateline)
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
                        if (PrefManager.isFullImageQuality){
                            setUrlList(reply.picArr)
                        } else{
                            for (element in reply.picArr)
                                if (element.substring(element.length - 3, element.length) != "gif")
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