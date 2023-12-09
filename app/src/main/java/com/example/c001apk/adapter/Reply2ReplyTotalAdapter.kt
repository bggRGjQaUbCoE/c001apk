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
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.ThemeUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnReplyClickListener
import com.example.c001apk.ui.fragment.minterface.IOnReplyDeleteClickListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.LinkTextView
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.google.android.material.progressindicator.CircularProgressIndicator


class Reply2ReplyTotalAdapter(
    private val mContext: Context,
    private val fuid: String,
    private val uid: String,
    private val position: Int,
    private val replyList: ArrayList<TotalReplyResponse.Data>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), PopupMenu.OnMenuItemClickListener {

    private var iOnReplyDeleteClickListener: IOnReplyDeleteClickListener? = null

    fun setIOnReplyDeleteClickListener(iOnReplyDeleteClickListener: IOnReplyDeleteClickListener) {
        this.iOnReplyDeleteClickListener = iOnReplyDeleteClickListener
    }

    private var rid = ""
    private var ruid = ""
    private var rposition = -1

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
        //notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        var id = ""
        var uid = ""
        var name = ""
        var isLike = false
        val message: LinkTextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val reply: TextView = view.findViewById(R.id.reply)
        val multiImage: NineGridImageView = view.findViewById(R.id.multiImage)
        val expand: ImageButton = view.findViewById(R.id.expand)
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
                    iOnReplyClickListener.onReply2Reply(
                        position,
                        viewHolder.bindingAdapterPosition,
                        viewHolder.id,
                        viewHolder.uid,
                        viewHolder.name,
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
                                null,
                                viewHolder.isLike,
                                viewHolder.id,
                                viewHolder.bindingAdapterPosition
                            )
                        }
                    }
                }
                viewHolder.multiImage.apply {
                    onImageItemClickListener = this@Reply2ReplyTotalAdapter.onImageItemClickListener
                }
                viewHolder.expand.setOnClickListener {
                    rid = viewHolder.id
                    ruid = viewHolder.uid
                    rposition = viewHolder.bindingAdapterPosition
                    val popup = PopupMenu(mContext, it)
                    val inflater = popup.menuInflater
                    inflater.inflate(R.menu.feed_reply_menu, popup.menu)
                    popup.menu.findItem(R.id.copy).isVisible = false
                    popup.menu.findItem(R.id.delete).isVisible = PrefManager.uid == viewHolder.uid
                    popup.setOnMenuItemClickListener(this@Reply2ReplyTotalAdapter)
                    popup.show()
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

            if (viewType == 0) {
                if (payloads[0] == "like") {
                    (holder as ViewHolder).like.text = replyList[position].likenum
                    holder.isLike = replyList[position].userAction?.like == 1
                    val drawableLike: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
                    drawableLike.setBounds(
                        0,
                        0,
                        holder.like.textSize.toInt(),
                        holder.like.textSize.toInt()
                    )
                    if (replyList[position].userAction?.like == 1) {
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
                ImageUtil.showAvatar(holder.avatar, reply.userAvatar)


                val replyTag =
                    when (reply.uid) {
                        fuid -> " [楼主] "
                        uid -> " [层主] "
                        else -> ""
                    }

                val rReplyTag =
                    when (reply.ruid) {
                        fuid -> " [楼主] "
                        uid -> " [层主] "
                        else -> ""
                    }

                val text =
                    if (reply.ruid == "0")
                        """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}$replyTag </a>"""
                    else
                        """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}$replyTag</a>回复<a class="feed-link-uname" href="/u/${reply.rusername}">${reply.rusername}$rReplyTag </a>"""


                holder.uname.text = SpannableStringBuilderUtil.setReply(
                    mContext,
                    text,
                    holder.uname.textSize.toInt(),
                    null
                )
                holder.uname.movementMethod = LinkMovementMethod.getInstance()

                holder.message.movementMethod =
                    LinkTextView.LocalLinkMovementMethod.getInstance()
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

    override fun onMenuItemClick(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.block -> {
                BlackListUtil.saveUid(ruid)
                replyList.removeAt(rposition)
                notifyItemRemoved(rposition)
            }

            R.id.report -> {
                val intent = Intent(mContext, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed_reply&id=$rid"
                )
                mContext.startActivity(intent)
            }

            R.id.delete -> {
                iOnReplyDeleteClickListener?.onDeleteReply(rid, rposition, null)
            }
        }
        return false
    }

}