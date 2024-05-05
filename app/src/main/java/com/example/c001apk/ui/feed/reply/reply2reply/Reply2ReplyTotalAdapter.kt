package com.example.c001apk.ui.feed.reply.reply2reply

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.c001apk.BR
import com.example.c001apk.R
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.adapter.PopClickListener
import com.example.c001apk.databinding.ItemReplyToReplyItemBinding
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.dp


class Reply2ReplyTotalAdapter(
    private val listener: ItemListener
) : ListAdapter<TotalReplyResponse.Data, Reply2ReplyTotalAdapter.ReplyViewHolder>(
    Reply2ReplyDiffCallback()
) {

    class ReplyViewHolder(
        val binding: ItemReplyToReplyItemBinding,
        private val listener: ItemListener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private var rId: String = ""
        private var rUid: String = ""
        private var username: String = ""

        init {
            itemView.setOnClickListener {
                listener.onReply(
                    rId,
                    rUid,
                    rUid,
                    username,
                    bindingAdapterPosition,
                    null
                )
            }

            binding.expand.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    menuInflater.inflate(R.menu.feed_reply_menu, menu).apply {
                        menu.findItem(R.id.copy)?.isVisible = false
                        menu.findItem(R.id.delete)?.isVisible = PrefManager.uid == rUid
                        menu.findItem(R.id.report)?.isVisible = PrefManager.isLogin
                    }
                    setOnMenuItemClickListener(
                        PopClickListener(
                            listener,
                            it.context,
                            "feed_reply",
                            rId,
                            rUid,
                            bindingAdapterPosition
                        )
                    )
                    show()
                }
            }
        }

        fun bind(reply: TotalReplyResponse.Data) {

            rId = reply.id
            rUid = reply.uid
            username = reply.userInfo.username

            binding.setVariable(
                BR.likeData,
                Like(
                    reply.likenum,
                    reply.userAction?.like ?: 0
                )
            )
            binding.setVariable(BR.data, reply)
            binding.setVariable(BR.listener, listener)
            binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int) = position

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ReplyViewHolder {
        val binding = ItemReplyToReplyItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.apply {
            if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                if (position == 0) {
                    setBackgroundColor(Color.TRANSPARENT)
                } else {
                    background = parent.context.getDrawable(R.drawable.text_card_bg)
                    foreground = parent.context.getDrawable(R.drawable.selector_bg_12_trans)
                    setPadding(10.dp)
                }
            } else {
                if (position == 0) {
                    setBackgroundColor(Color.TRANSPARENT)
                } else {
                    setBackgroundColor(parent.context.getColor(R.color.home_card_background_color))
                }
                foreground = parent.context.getDrawable(R.drawable.selector_bg_trans)
                setPadding(15.dp, 12.dp, 15.dp, 12.dp)
            }
        }
        return ReplyViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun onBindViewHolder(
        holder: ReplyViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            if (payloads[0] == true) {
                holder.binding.likeData = Like(
                    currentList[position].likenum,
                    currentList[position].userAction?.like ?: 0
                )
                holder.binding.executePendingBindings()
            }
        }
    }

}


class Reply2ReplyDiffCallback : DiffUtil.ItemCallback<TotalReplyResponse.Data>() {
    override fun areItemsTheSame(
        oldItem: TotalReplyResponse.Data,
        newItem: TotalReplyResponse.Data
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: TotalReplyResponse.Data,
        newItem: TotalReplyResponse.Data
    ): Boolean {
        return oldItem.likenum == newItem.likenum
    }

    override fun getChangePayload(
        oldItem: TotalReplyResponse.Data,
        newItem: TotalReplyResponse.Data
    ): Any? {
        return if (oldItem.likenum != newItem.likenum) true else null
    }
}

interface ReplyRefreshListener {
    fun onRefreshReply(listType: String)
}