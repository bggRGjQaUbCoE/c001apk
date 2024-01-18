package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.ThemeUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.c001apk.R
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.LinkTextView
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.google.android.material.imageview.ShapeableImageView


class Reply2ReplyTotalAdapter(
    private val mContext: Context,
    private val fuid: String,
    private val uid: String,
    private val position: Int,
    private val replyList: ArrayList<TotalReplyResponse.Data>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), PopupMenu.OnMenuItemClickListener {

    private var appListener: AppListener? = null

    fun setAppListener(appListener: AppListener) {
        this.appListener = appListener
    }

    private var rid = ""
    private var ruid = ""
    private var rposition = -1

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

    class ReplyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: LinkTextView = view.findViewById(R.id.uname)
        var id = ""
        var uid = ""
        var name = ""
        var isLike = false
        val message: LinkTextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
        val reply: TextView = view.findViewById(R.id.reply)
        val multiImage: NineGridImageView = view.findViewById(R.id.multiImage)
        val expand: ImageButton = view.findViewById(R.id.expand)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_reply_to_reply_item, parent, false)
                val viewHolder = ReplyViewHolder(view)
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
                    appListener?.onReply2Reply(
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
                            appListener?.onPostLike(
                                null,
                                viewHolder.isLike,
                                viewHolder.id,
                                viewHolder.bindingAdapterPosition
                            )
                        }
                    }
                }
                viewHolder.multiImage.apply {
                    appListener = this@Reply2ReplyTotalAdapter.appListener
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
                    popup.menu.findItem(R.id.report).isVisible = PrefManager.isLogin
                    popup.setOnMenuItemClickListener(this@Reply2ReplyTotalAdapter)
                    popup.show()
                }
                viewHolder
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_rv_footer, parent, false)
                val viewHolder = AppAdapter.FootViewHolder(view)
                viewHolder.retry.setOnClickListener {
                    appListener?.onReload()
                }
                viewHolder
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
                    (holder as ReplyViewHolder).like.text = replyList[position].likenum
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

            is ReplyViewHolder -> {
                val reply = replyList[position]

                holder.itemView.also {
                    if (it.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                        if (position == 0) {
                            it.setBackgroundColor(Color.TRANSPARENT)
                        } else {
                            it.background = mContext.getDrawable(R.drawable.text_card_bg)
                        }
                        it.foreground = mContext.getDrawable(R.drawable.selector_bg_12_trans)
                    } else {
                        if (position == 0) {
                            it.setBackgroundColor(Color.TRANSPARENT)
                        } else {
                            it.setBackgroundColor(mContext.getColor(R.color.home_card_background_color))
                        }
                        it.foreground = mContext.getDrawable(R.drawable.selector_bg_trans)
                    }
                }

                holder.id = reply.id
                holder.uid = reply.uid
                holder.name = reply.username
                holder.isLike = reply.userAction?.like == 1
                ImageUtil.showIMG(holder.avatar, reply.userAvatar)

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
                        """<a class="feed-link-uname" href="/u/${reply.uid}">${reply.username}$replyTag</a>""" + "\u3000"
                    else
                        """<a class="feed-link-uname" href="/u/${reply.uid}">${reply.username}$replyTag</a>回复<a class="feed-link-uname" href="/u/${reply.rusername}">${reply.rusername}$rReplyTag</a>""" + "\u3000"


                holder.uname.text = SpannableStringBuilderUtil.setReply(
                    mContext,
                    text,
                    holder.uname.textSize.toInt(),
                    null
                )
                holder.uname.movementMethod = LinkTextView.LocalLinkMovementMethod.getInstance()

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
                            urlList.add("$element.s.jpg")
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
                IntentUtil.startActivity<WebViewActivity>(mContext) {
                    putExtra(
                        "url",
                        "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed_reply&id=$rid"
                    )
                }
            }

            R.id.delete -> {
                appListener?.onDeleteFeedReply(rid, rposition, null)
            }

            R.id.show -> {
                appListener?.onShowTotalReply(rposition, ruid, rid, null)
            }
        }
        return false
    }

}