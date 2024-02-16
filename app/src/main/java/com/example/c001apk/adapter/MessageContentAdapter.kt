package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.databinding.ItemMessageContentBinding
import com.example.c001apk.databinding.ItemMessageUserBinding
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.model.MessageResponse
import com.example.c001apk.ui.activity.FeedActivity
import com.example.c001apk.util.IntentUtil


class MessageContentAdapter(
    private val type: String,
    private val listener: ItemListener
) : ListAdapter<MessageResponse.Data, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    inner class UserViewHolder(val binding: ItemMessageUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.type = type
            binding.data = currentList[bindingAdapterPosition]
            binding.listener = listener
            binding.executePendingBindings()
        }

    }


    inner class MessageViewHolder(val binding: ItemMessageContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val data = currentList[bindingAdapterPosition]
            binding.type = type
            binding.data = data
            binding.likeData = Like().also {
                it.likeNum.set(data.likenum)
                data.userAction?.like?.let { like ->
                    it.isLike.set(like)
                }
            }
            binding.listener = listener

            if (type != "feedLike") {
                itemView.setOnClickListener {
                    IntentUtil.startActivity<FeedActivity>(itemView.context) {
                        putExtra("id", data.id)
                    }
                }
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                MessageViewHolder(
                    ItemMessageContentBinding.inflate(
                        LayoutInflater.from(parent.context), parent,
                        false
                    )
                )
            }


            1 -> {
                UserViewHolder(
                    ItemMessageUserBinding.inflate(
                        LayoutInflater.from(parent.context), parent,
                        false
                    )
                )
            }

            else -> throw IllegalArgumentException("invalid type")
        }

    }


    override fun getItemViewType(position: Int): Int {
        return when (type) {
            "atMe" -> 0
            "atCommentMe" -> 0
            "feedLike" -> 0
            "contactsFollow" -> 1
            "list" -> 1
            else -> throw IllegalArgumentException("invalid type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {

            is UserViewHolder -> {
                holder.bind()
            }


            is MessageViewHolder -> {
                holder.bind()
            }
        }
    }

}

class MessageDiffCallback : DiffUtil.ItemCallback<MessageResponse.Data>() {
    override fun areItemsTheSame(
        oldItem: MessageResponse.Data,
        newItem: MessageResponse.Data
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: MessageResponse.Data,
        newItem: MessageResponse.Data
    ): Boolean {
        return oldItem.id == newItem.id
    }
}