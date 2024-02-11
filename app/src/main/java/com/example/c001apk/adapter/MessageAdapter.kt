package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.setPadding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.databinding.ItemMessageItemBinding
import com.example.c001apk.logic.model.MessageResponse


class MessageAdapter(
    private val listener: ItemListener
) : ListAdapter<MessageResponse.Data, MessageAdapter.MessViewHolder>(MessageDiffCallback()) {

    inner class MessViewHolder(val binding: ItemMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val data = currentList[bindingAdapterPosition]
            binding.data = data
            binding.listener = listener

            itemView.setOnLongClickListener {
                listener.onMessLongClicked(
                    data.fromusername,
                    data.id,
                    bindingAdapterPosition
                )
                true
            }

            binding.expand.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    menuInflater.inflate(R.menu.feed_reply_menu, menu).apply {
                        menu.findItem(R.id.show)?.isVisible = false
                        menu.findItem(R.id.copy)?.isVisible = false
                        menu.findItem(R.id.delete)?.isVisible = false
                    }
                    setOnMenuItemClickListener(
                        PopClickListener(
                            listener,
                            it.context,
                            data.entityType,
                            data.id,
                            data.uid,
                            bindingAdapterPosition
                        )
                    )
                    show()
                }
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessViewHolder {
        val binding = ItemMessageItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.setPadding(10.dp)
        binding.root.background = parent.context.getDrawable(R.drawable.round_corners_12)
        binding.root.foreground = parent.context.getDrawable(R.drawable.selector_bg_12_trans)
        return MessViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessViewHolder, position: Int) {
        holder.bind()
    }


}