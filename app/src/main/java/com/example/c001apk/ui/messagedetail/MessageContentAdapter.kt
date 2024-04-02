package com.example.c001apk.ui.messagedetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.BR
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.databinding.ItemMessageContentBinding
import com.example.c001apk.databinding.ItemMessageUserBinding
import com.example.c001apk.logic.model.MessageResponse
import com.example.c001apk.ui.feed.FeedActivity
import com.example.c001apk.util.IntentUtil


class MessageContentAdapter(
    private val type: String,
    private val listener: ItemListener
) : ListAdapter<MessageResponse.Data, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    class UserViewHolder(
        val binding: ItemMessageUserBinding,
        val type: String,
        val listener: ItemListener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MessageResponse.Data) {
            binding.setVariable(BR.type, type)
            binding.setVariable(BR.data, data)
            binding.setVariable(BR.listener, listener)
            binding.executePendingBindings()
        }

    }

    class MessageViewHolder(
        val binding: ItemMessageContentBinding,
        val type: String,
        val listener: ItemListener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        var id: String = ""

        init {
            if (type != "feedLike") {
                itemView.setOnClickListener {
                    IntentUtil.startActivity<FeedActivity>(itemView.context) {
                        putExtra("id", id)
                    }
                }
            }
        }

        fun bind(data: MessageResponse.Data) {
            id = data.id
            binding.setVariable(BR.type, type)
            binding.setVariable(BR.data, data)
            binding.setVariable(BR.listener, listener)

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
                    ), type, listener
                )
            }


            1 -> {
                UserViewHolder(
                    ItemMessageUserBinding.inflate(
                        LayoutInflater.from(parent.context), parent,
                        false
                    ), type, listener
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
                holder.bind(currentList[position])
            }


            is MessageViewHolder -> {
                holder.bind(currentList[position])
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