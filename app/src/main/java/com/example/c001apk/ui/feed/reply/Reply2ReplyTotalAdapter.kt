package com.example.c001apk.ui.feed.reply

import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.BR
import com.example.c001apk.R
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.adapter.PopClickListener
import com.example.c001apk.databinding.ItemReplyToReplyItemBinding
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil


class Reply2ReplyTotalAdapter(
    private val listener: ItemListener,
    private val fuid: String,
    private val uid: String,
) : ListAdapter<TotalReplyResponse.Data, Reply2ReplyTotalAdapter.ReplyViewHolder>(
    Reply2ReplyDiffCallback()
) {

    class ReplyViewHolder(
        private val binding: ItemReplyToReplyItemBinding,
        private val listener: ItemListener,
        private val fuid: String,
        private val uid: String
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private var rId: String = ""
        private var rUid: String = ""
        private var username: String = ""
        private var likeData: Like = Like()

        init {
            itemView.setOnClickListener {
                listener.onReply(
                    rId,
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
                        menu.findItem(R.id.delete)?.isVisible = PrefManager.uid == uid
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

            binding.like.setOnClickListener {
                listener.onLikeClick(
                    "feed_reply", rId,
                    bindingAdapterPosition, likeData
                )
            }
        }

        fun bind(reply: TotalReplyResponse.Data) {

            binding.root.also {
                if (it.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                    if (bindingAdapterPosition == 0) {
                        it.setBackgroundColor(Color.TRANSPARENT)
                    } else {
                        it.background =
                            itemView.context.getDrawable(R.drawable.text_card_bg)
                        it.foreground =
                            itemView.context.getDrawable(R.drawable.selector_bg_12_trans)
                        it.setPadding(10.dp)
                    }
                } else {
                    if (bindingAdapterPosition == 0) {
                        it.setBackgroundColor(Color.TRANSPARENT)
                    } else {
                        it.setBackgroundColor(itemView.context.getColor(R.color.home_card_background_color))
                    }
                    it.foreground =
                        itemView.context.getDrawable(R.drawable.selector_bg_trans)
                    it.setPadding(15.dp, 12.dp, 15.dp, 12.dp)
                }
            }

            rId = reply.id
            rUid = reply.uid
            username = reply.username

            val replyTag = if (bindingAdapterPosition == 0) ""
            else
                when (reply.uid) {
                    fuid -> " [楼主] "
                    uid -> " [层主] "
                    else -> ""
                }

            val rReplyTag = if (bindingAdapterPosition == 0) ""
            else
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

            binding.uname.movementMethod = LinkMovementMethod.getInstance()
            binding.uname.text = SpannableStringBuilderUtil.setText(
                itemView.context,
                text,
                binding.uname.textSize,
                null
            )

            binding.setVariable(BR.data, reply)
            binding.setVariable(BR.listener, listener)
            likeData = Like().also {
                it.apply {
                    reply.userAction?.like?.let { like ->
                        isLike.set(like)
                    }
                    likeNum.set(reply.likenum)
                }
            }
            binding.setVariable(BR.likeData, likeData)
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ReplyViewHolder {
        val binding = ItemReplyToReplyItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReplyViewHolder(binding, listener, fuid, uid)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        holder.bind(currentList[position])
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
        return oldItem.id == newItem.id
    }
}

interface ReplyRefreshListener {
    fun onRefreshReply(listType: String)
}