package com.example.c001apk.ui.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.BR
import com.example.c001apk.R
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.adapter.PopClickListener
import com.example.c001apk.databinding.ItemHistoryFeedBinding
import com.example.c001apk.logic.model.BrowseHistory
import com.example.c001apk.logic.model.FeedFavorite
import com.example.c001apk.util.PrefManager


class HistoryAdapter(
    private val listener: ItemListener
) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var dataList: List<Any> = ArrayList()
    private var type = ""

    @SuppressLint("NotifyDataSetChanged")
    fun setDataListData(type: String, dataList: List<Any>) {
        this.type = type
        this.dataList = dataList
        notifyDataSetChanged()
    }

    class HistoryViewHolder(val binding: ItemHistoryFeedBinding, val listener: ItemListener) :
        RecyclerView.ViewHolder(binding.root) {
        var id: String = ""
        var uid: String = ""

        init {
            binding.expand.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    menuInflater.inflate(R.menu.feed_reply_menu, menu).apply {
                        menu.findItem(R.id.copy)?.isVisible = false
                        menu.findItem(R.id.show)?.isVisible = false
                        menu.findItem(R.id.report)?.isVisible = PrefManager.isLogin
                    }
                    setOnMenuItemClickListener(
                        PopClickListener(
                            listener,
                            it.context,
                            "feed",
                            id,
                            uid,
                            bindingAdapterPosition
                        )
                    )
                    show()
                }
            }
        }

        fun bind(type: String, item: Any) {

            id =
                if (type == "browse") (item as BrowseHistory).fid else (item as FeedFavorite).feedId
            uid =
                if (type == "browse") (item as BrowseHistory).uid else (item as FeedFavorite).uid

            binding.setVariable(BR.id, id)
            binding.setVariable(BR.uid, uid)
            binding.setVariable(BR.listener, listener)
            binding.setVariable(
                BR.username,
                if (type == "browse") (item as BrowseHistory).uname else (item as FeedFavorite).uname
            )
            binding.setVariable(
                BR.avatarUrl,
                if (type == "browse") (item as BrowseHistory).avatar else (item as FeedFavorite).avatar
            )
            binding.setVariable(
                BR.deviceTitle,
                if (type == "browse") (item as BrowseHistory).device else (item as FeedFavorite).device
            )
            binding.setVariable(
                BR.dateline,
                if (type == "browse") (item as BrowseHistory).pubDate.toLong() else (item as FeedFavorite).pubDate.toLong()
            )
            binding.setVariable(
                BR.messageContent,
                if (type == "browse") (item as BrowseHistory).message else (item as FeedFavorite).message
            )

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            ItemHistoryFeedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), listener
        )
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        if (dataList.isNotEmpty()) {
            holder.bind(type, dataList[position])
        }
    }
}