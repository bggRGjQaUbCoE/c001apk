package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.MessageResponse
import com.example.c001apk.ui.activity.FFFListActivity
import com.example.c001apk.ui.activity.FeedActivity
import com.example.c001apk.ui.activity.MessageActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.CookieUtil.atcommentme
import com.example.c001apk.util.CookieUtil.atme
import com.example.c001apk.util.CookieUtil.contacts_follow
import com.example.c001apk.util.CookieUtil.feedlike
import com.example.c001apk.util.CookieUtil.message
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.google.android.material.imageview.ShapeableImageView
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements


class MessageAdapter(
    private val mContext: Context,
    private val countList: List<String>,
    private val messCountList: List<Int>,
    private val notiList: ArrayList<MessageResponse.Data>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), PopupMenu.OnMenuItemClickListener {

    private var uid = ""

    private var loadState = 2
    val LOADING = 1
    val LOADING_COMPLETE = 2
    val LOADING_END = 3

    @SuppressLint("NotifyDataSetChanged")
    fun setLoadState(loadState: Int) {
        this.loadState = loadState
        //notifyDataSetChanged()
    }

    private val messTitle = ArrayList<String>()
    private val fffTitle = ArrayList<String>()
    private val logoList = ArrayList<Int>()
    private val logoColorList = ArrayList<String>()

    init {
        fffTitle.apply {
            add("动态")
            add("关注")
            add("粉丝")
        }

        messTitle.apply {
            add("@我的动态")
            add("@我的评论")
            add("我收到的赞")
            add("好友关注")
            add("私信")
        }

        logoColorList.apply {
            add("#2196f3")
            add("#00bcd4")
            add("#4caf50")
            add("#f44336")
            add("#ff9800")
        }

        logoList.apply {
            add(R.drawable.ic_at)
            add(R.drawable.ic_comment)
            add(R.drawable.ic_thumb)
            add(R.drawable.ic_add)
            add(R.drawable.ic_message1)
        }

    }

    class FFFViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val feedCount: TextView = view.findViewById(R.id.feedCount)
        val feedTitle: TextView = view.findViewById(R.id.feedTitle)
        val feedLayout: LinearLayout = view.findViewById(R.id.feedLayout)
        val followCount: TextView = view.findViewById(R.id.followCount)
        val followTitle: TextView = view.findViewById(R.id.followTitle)
        val followLayout: LinearLayout = view.findViewById(R.id.followLayout)
        val fansCount: TextView = view.findViewById(R.id.fansCount)
        val fansTitle: TextView = view.findViewById(R.id.fansTitle)
        val fansLayout: LinearLayout = view.findViewById(R.id.fansLayout)
    }

    class MessViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val logo: ShapeableImageView = view.findViewById(R.id.logo)
        val logoCover: ShapeableImageView = view.findViewById(R.id.logoCover)
        val badge: TextView = view.findViewById(R.id.badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            -1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_rv_footer, parent, false)
                AppAdapter.FootViewHolder(view)
            }

            0 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_fff, parent, false)
                val viewHolder = FFFViewHolder(view)
                if (PrefManager.isLogin) {
                    val intent = Intent(parent.context, FFFListActivity::class.java)
                    intent.putExtra("uid", PrefManager.uid)
                    viewHolder.feedLayout.setOnClickListener {
                        intent.putExtra("type", "feed")
                        parent.context.startActivity(intent)
                    }
                    viewHolder.followLayout.setOnClickListener {
                        intent.putExtra("type", "follow")
                        parent.context.startActivity(intent)
                    }
                    viewHolder.fansLayout.setOnClickListener {
                        intent.putExtra("type", "fans")
                        parent.context.startActivity(intent)
                    }
                }
                viewHolder
            }

            1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_mess, parent, false)
                val viewHolder = MessViewHolder(view)
                viewHolder.itemView.setOnClickListener {
                    if (PrefManager.isLogin) {
                        val intent = Intent(parent.context, MessageActivity::class.java)
                        when (viewHolder.title.text) {
                            "@我的动态" -> {
                                atme = 0
                                intent.putExtra("type", "atMe")
                            }

                            "@我的评论" -> {
                                atcommentme = 0
                                intent.putExtra("type", "atCommentMe")
                            }

                            "我收到的赞" -> {
                                feedlike = 0
                                intent.putExtra("type", "feedLike")
                            }

                            "好友关注" -> {
                                contacts_follow = 0
                                intent.putExtra("type", "contactsFollow")
                            }

                            "私信" -> {
                                message = 0
                                intent.putExtra("type", "list")
                            }
                        }
                        parent.context.startActivity(intent)
                    }
                    viewHolder.badge.visibility = View.GONE
                }
                viewHolder
            }

            2 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feed_content_reply_item, parent, false)
                val viewHolder = FeedContentAdapter.FeedContentReplyViewHolder(view)
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
                viewHolder.itemView.setOnClickListener {
                    val intent = Intent(parent.context, FeedActivity::class.java)
                    intent.putExtra("type", "feed")
                    intent.putExtra("id", viewHolder.id)
                    intent.putExtra("uid", viewHolder.uid)
                    intent.putExtra("uname", viewHolder.uname.text)
                    parent.context.startActivity(intent)
                }
                viewHolder.expand.setOnClickListener {
                    uid = viewHolder.uid
                    val popup = PopupMenu(mContext, it)
                    val inflater = popup.menuInflater
                    inflater.inflate(R.menu.feed_reply_menu, popup.menu)
                    popup.setOnMenuItemClickListener(this@MessageAdapter)
                    popup.show()
                }
                viewHolder
            }

            else -> throw IllegalArgumentException("invalid type")
        }

    }

    override fun getItemCount() = notiList.size + 7

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) -1
        else when (position) {
            0 -> 0
            in 1..5 -> 1
            else -> 2
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {

            is AppAdapter.FootViewHolder -> {
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

            is FeedContentAdapter.FeedContentReplyViewHolder -> {
                val noti = notiList[position - 6]
                val doc: Document = Jsoup.parse(noti.note)
                val links: Elements = doc.select("a[href]")
                for (link in links) {
                    val href = link.attr("href")
                    holder.id = href.replace("/feed/", "")
                }
                holder.uid = noti.fromuid
                holder.uname.text = noti.fromusername
                //holder.message.movementMethod = LinkMovementMethod.getInstance()
                holder.message.text = SpannableStringBuilderUtil.setText(
                    mContext, noti.note, (holder.message.textSize * 1.3).toInt(), null
                )
                holder.pubDate.text = DateUtils.fromToday(noti.dateline)
                ImageUtil.showAvatar(holder.avatar, noti.fromUserAvatar)
            }

            is FFFViewHolder -> {
                if (countList.isNotEmpty()) {
                    holder.apply {
                        feedCount.text = countList[0]
                        feedTitle.text = fffTitle[0]
                        followCount.text = countList[1]
                        followTitle.text = fffTitle[1]
                        fansCount.text = countList[2]
                        fansTitle.text = fffTitle[2]
                    }
                }
            }

            is MessViewHolder -> {
                holder.title.text = messTitle[position - 1]
                holder.logoCover.setBackgroundColor(Color.parseColor(logoColorList[position - 1]))
                holder.logo.setBackgroundDrawable(mContext.getDrawable(logoList[position - 1]))
                if (messCountList.isNotEmpty()) {
                    if (messCountList[position - 1] != 0) {
                        holder.badge.visibility = View.VISIBLE
                        holder.badge.text = messCountList[position - 1].toString()
                    } else
                        holder.badge.visibility = View.GONE
                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onMenuItemClick(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.block -> {
                BlackListUtil.saveUid(uid)
            }

            R.id.report -> {
                val intent = Intent(mContext, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://m.coolapk.com/mp/do?c=user&m=report&id=$uid"
                )
                mContext.startActivity(intent)
            }
        }
        return false
    }

}