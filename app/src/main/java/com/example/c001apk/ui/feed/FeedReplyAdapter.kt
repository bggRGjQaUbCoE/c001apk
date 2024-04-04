package com.example.c001apk.ui.feed

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.BR
import com.example.c001apk.R
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.databinding.ItemFeedContentReplyItemBinding
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.LinkMovementClickMethod
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FeedReplyAdapter(
    private val blackListRepo: BlackListRepo,
    private val listener: ItemListener
) :
    ListAdapter<TotalReplyResponse.Data, FeedReplyAdapter.ViewHolder>(FeedReplyDiffCallback()) {

    private var haveTop = false
    private var topReplyId: String? = null

    fun setHaveTop(haveTop: Boolean, topReplyId: String?) {
        this.haveTop = haveTop
        this.topReplyId = topReplyId
    }

    class ViewHolder(val binding: ItemFeedContentReplyItemBinding, val listener: ItemListener) :
        RecyclerView.ViewHolder(binding.root) {
        var id: String = ""
        var uid: String = ""
        var username: String = ""
        var message: String = ""

        init {
            itemView.setOnClickListener {
                listener.onReply(
                    id, uid, username,
                    bindingAdapterPosition, null
                )
            }

            binding.expand.setOnClickListener {
                listener.onExpand(
                    it, id, uid,
                    message, bindingAdapterPosition, null
                )
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(
            reply: TotalReplyResponse.Data,
            haveTop: Boolean,
            topReplyId: String?,
            blackListRepo: BlackListRepo
        ) {

            if (!reply.username.contains("[楼主]") && !reply.username.contains("[置顶]")) {
                val unameTag =
                    when (reply.uid) {
                        reply.feedUid -> " [楼主]"
                        else -> ""
                    }
                val replyTag =
                    when (haveTop && reply.id == topReplyId) {
                        true -> {
                            if (bindingAdapterPosition == 0) " [置顶]"
                            else ""
                        }

                        else -> ""
                    }
                reply.username = "${reply.username}$unameTag$replyTag\u3000"
            }

            id = reply.id
            uid = reply.uid
            username = reply.username
            message = reply.message

            binding.setVariable(BR.data, reply)
            binding.setVariable(BR.listener, listener)
            binding.setVariable(
                BR.likeData, Like(
                    reply.likenum,
                    reply.userAction?.like ?: 0
                )
            )

            if (!reply.replyRows.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    val sortedList = reply.replyRows.filter {
                        !blackListRepo.checkUid(it.uid)
                    }
                    if (sortedList.isNotEmpty()) {
                        binding.replyLayout.isVisible = true
                        if (itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                            binding.replyLayout.setCardBackgroundColor(
                                MaterialColors.getColor(
                                    itemView.context,
                                    android.R.attr.windowBackground,
                                    0
                                )
                            )
                        }
                        binding.linearAdapterLayout.adapter = object : BaseAdapter() {
                            override fun getCount(): Int = sortedList.size
                            override fun getItem(p0: Int): Any = 0
                            override fun getItemId(p0: Int): Long = 0
                            override fun getView(
                                position: Int,
                                convertView: View?,
                                parent: ViewGroup
                            ): View {
                                val view = LayoutInflater.from(parent.context).inflate(
                                    R.layout.item_feed_content_reply_to_reply_item,
                                    parent,
                                    false
                                )
                                val replyData = sortedList[position]
                                val textView: TextView = view.findViewById(R.id.reply)
                                textView.highlightColor = ColorUtils.setAlphaComponent(
                                    MaterialColors.getColor(
                                        parent.context,
                                        com.google.android.material.R.attr.colorPrimaryDark,
                                        0
                                    ), 128
                                )

                                val replyTag1 =
                                    when (replyData.uid) {
                                        reply.feedUid -> " [楼主] "
                                        reply.uid -> " [层主] "
                                        else -> ""
                                    }

                                val rReplyTag =
                                    when (replyData.ruid) {
                                        reply.feedUid -> " [楼主] "
                                        reply.uid -> " [层主] "
                                        else -> ""
                                    }

                                val rReplyUser =
                                    when (replyData.ruid) {
                                        reply.uid -> ""
                                        else -> """<a class="feed-link-uname" href="/u/${replyData.ruid}">${replyData.rusername}${rReplyTag}</a>"""
                                    }

                                val replyPic =
                                    when (replyData.pic) {
                                        "" -> ""
                                        else -> """ <a class=\"feed-forward-pic\" href=${replyData.pic}>查看图片(${replyData.picArr?.size})</a>"""
                                    }

                                val mess =
                                    """<a class="feed-link-uname" href="/u/${replyData.uid}">${replyData.username}${replyTag1}</a>回复${rReplyUser}: ${replyData.message}${replyPic}"""

                                textView.movementMethod = LinkMovementClickMethod.instance

                                textView.text = SpannableStringBuilderUtil.setText(
                                    parent.context,
                                    mess,
                                    textView.textSize,
                                    replyData.picArr
                                ) {
                                    listener.showTotalReply(
                                        reply.id,
                                        reply.uid,
                                        bindingAdapterPosition,
                                        null
                                    )
                                }

                                SpannableStringBuilderUtil.setData(
                                    position + 1,
                                    reply.uid
                                )

                                view.setOnClickListener {
                                    listener.onReply(
                                        replyData.id, replyData.uid, replyData.username,
                                        bindingAdapterPosition, position
                                    )
                                }

                                view.setOnLongClickListener {
                                    listener.onExpand(
                                        it, replyData.id, replyData.uid,
                                        replyData.message, bindingAdapterPosition, position
                                    )
                                    true
                                }

                                return view
                            }
                        }
                    } else binding.replyLayout.isVisible = false
                }
            } else binding.replyLayout.isVisible = false

            if (reply.replyRowsMore != 0) {
                binding.totalReply.isVisible = true
                val count = (reply.replyRowsMore ?: 0) + (reply.replyRows?.size ?: 0)
                binding.totalReply.text = "查看更多回复($count)"
                binding.totalReply.setOnClickListener {
                    listener.showTotalReply(
                        reply.id, reply.uid,
                        bindingAdapterPosition,
                        null
                    )
                }
            } else
                binding.totalReply.isVisible = false

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFeedContentReplyItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.apply {
            if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                background = parent.context.getDrawable(R.drawable.text_card_bg)
                foreground = parent.context.getDrawable(R.drawable.selector_bg_12_trans)
                setPadding(10.dp)
            } else {
                foreground = parent.context.getDrawable(R.drawable.selector_bg_trans)
                setPadding(15.dp, 12.dp, 15.dp, 12.dp)
            }
        }
        return ViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], haveTop, topReplyId, blackListRepo)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
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

class FeedReplyDiffCallback : DiffUtil.ItemCallback<TotalReplyResponse.Data>() {
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