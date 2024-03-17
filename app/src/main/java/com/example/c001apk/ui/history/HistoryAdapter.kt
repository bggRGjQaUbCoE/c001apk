package com.example.c001apk.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.BR
import com.example.c001apk.R
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.adapter.PopClickListener
import com.example.c001apk.databinding.ItemHistoryFeedBinding
import com.example.c001apk.logic.model.FeedEntity
import com.example.c001apk.util.PrefManager


class HistoryAdapter(
    private val listener: ItemListener
) :
    ListAdapter<FeedEntity, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

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

        fun bind(data: FeedEntity) {
            id = data.fid
            uid = data.uid

            binding.setVariable(BR.id, id)
            binding.setVariable(BR.uid, uid)
            binding.setVariable(BR.listener, listener)
            binding.setVariable(BR.username, data.uname)
            binding.setVariable(BR.avatarUrl, data.avatar)
            binding.setVariable(BR.deviceTitle, data.device)
            binding.setVariable(BR.dateline, data.pubDate.toLong())
            binding.setVariable(BR.messageContent, data.message)
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


    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}

class HistoryDiffCallback : DiffUtil.ItemCallback<FeedEntity>() {
    override fun areItemsTheSame(
        oldItem: FeedEntity,
        newItem: FeedEntity
    ): Boolean {
        return oldItem.fid == newItem.fid
    }

    override fun areContentsTheSame(
        oldItem: FeedEntity,
        newItem: FeedEntity
    ): Boolean {
        return oldItem.fid == newItem.fid
    }
}
