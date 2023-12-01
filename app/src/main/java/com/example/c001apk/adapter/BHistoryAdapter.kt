package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.database.BrowseHistoryDatabase
import com.example.c001apk.logic.database.FeedFavoriteDatabase
import com.example.c001apk.logic.model.BrowseHistory
import com.example.c001apk.logic.model.FeedFavorite
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.FeedActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.SpannableStringBuilderUtil
import kotlin.concurrent.thread


class BHistoryAdapter(
    private val mContext: Context,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), PopupMenu.OnMenuItemClickListener {

    private val browseHistoryDao by lazy {
        BrowseHistoryDatabase.getDatabase(mContext).browseHistoryDao()
    }
    private val feedFavoriteDao by lazy {
        FeedFavoriteDatabase.getDatabase(mContext).feedFavoriteDao()
    }

    private var dataList = ArrayList<Any>()
    private var type = ""
    private var fid = ""

    fun setDataListData(type: String, dataList: ArrayList<Any>) {
        this.type = type
        this.dataList = dataList
    }

    private var uid = ""
    private var position = -1
    private var loadState = 3
    val LOADING = 1
    val LOADING_COMPLETE = 2
    val LOADING_END = 3

    @SuppressLint("NotifyDataSetChanged")
    fun setLoadState(loadState: Int) {
        this.loadState = loadState
        //notifyDataSetChanged()
    }

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var id = ""
        var uid = ""
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val uname: TextView = view.findViewById(R.id.uname)
        val device: TextView = view.findViewById(R.id.device)
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val expand: ImageButton = view.findViewById(R.id.expand)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            -1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_rv_footer, parent, false)
                AppAdapter.FootViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_history_feed, parent, false)
                val viewHolder = HistoryViewHolder(view)
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
                    intent.putExtra("id", viewHolder.uid)
                    parent.context.startActivity(intent)
                }
                viewHolder.uname.setOnClickListener {
                    val intent = Intent(parent.context, UserActivity::class.java)
                    intent.putExtra("id", viewHolder.uid)
                    parent.context.startActivity(intent)
                }
                viewHolder.expand.setOnClickListener {
                    uid = viewHolder.uid
                    fid = viewHolder.id
                    position = viewHolder.bindingAdapterPosition
                    val popup = PopupMenu(mContext, it)
                    val inflater = popup.menuInflater
                    inflater.inflate(R.menu.feed_history_menu, popup.menu)
                    popup.setOnMenuItemClickListener(this@BHistoryAdapter)
                    popup.show()
                }
                viewHolder
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) -1
        else 1
    }

    override fun getItemCount() = dataList.size + 1

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

            is HistoryViewHolder -> {
                if (dataList.isNotEmpty()) {
                    if (type == "browse") {
                        val history = dataList[position] as BrowseHistory
                        holder.id = history.fid
                        holder.uid = history.uid
                        holder.uname.text = history.uname
                        if (history.device == "")
                            holder.device.visibility = View.GONE
                        else {
                            holder.device.visibility = View.VISIBLE
                            holder.device.text = history.device
                        }
                        holder.pubDate.text = DateUtils.fromToday(history.pubDate.toLong())
                        holder.message.movementMethod = LinkMovementMethod.getInstance()
                        holder.message.text = SpannableStringBuilderUtil.setText(
                            mContext,
                            history.message,
                            (holder.message.textSize * 1.3).toInt(),
                            null
                        )
                        ImageUtil.showAvatar(holder.avatar, history.avatar)
                    } else {
                        val history = dataList[position] as FeedFavorite
                        holder.id = history.feedId
                        holder.uid = history.uid
                        holder.uname.text = history.uname
                        if (history.device == "")
                            holder.device.visibility = View.GONE
                        else {
                            holder.device.visibility = View.VISIBLE
                            holder.device.text = history.device
                        }
                        holder.pubDate.text = DateUtils.fromToday(history.pubDate.toLong())
                        holder.message.movementMethod = LinkMovementMethod.getInstance()
                        holder.message.text = SpannableStringBuilderUtil.setText(
                            mContext,
                            history.message,
                            (holder.message.textSize * 1.3).toInt(),
                            null
                        )
                        ImageUtil.showAvatar(holder.avatar, history.avatar)
                    }
                }
            }
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
                val intent = Intent(mContext, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed&id=$fid"
                )
                mContext.startActivity(intent)
            }

            R.id.delete -> {
                dataList.removeAt(position)
                notifyItemRemoved(position)
                thread {
                    if (type == "browse")
                        browseHistoryDao.delete(fid)
                    else
                        feedFavoriteDao.delete(fid)
                }
            }
        }
        return false
    }

}