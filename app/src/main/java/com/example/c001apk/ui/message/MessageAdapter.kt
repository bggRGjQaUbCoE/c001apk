package com.example.c001apk.ui.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.setPadding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.BR
import com.example.c001apk.R
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.adapter.PopClickListener
import com.example.c001apk.databinding.ItemMessageItemBinding
import com.example.c001apk.logic.model.MessageResponse
import com.example.c001apk.ui.messagedetail.MessageDiffCallback


class MessageAdapter(
    private val listener: ItemListener
) : ListAdapter<MessageResponse.Data, MessageAdapter.MessViewHolder>(MessageDiffCallback()) {

    class MessViewHolder(val binding: ItemMessageItemBinding, val listener: ItemListener) :
        RecyclerView.ViewHolder(binding.root) {
        var entityType: String = ""
        var id: String = ""
        var uid: String = ""
        private var fromusername: String = ""

        init {
            itemView.setOnLongClickListener {
                listener.onMessLongClicked(
                    fromusername,
                    id,
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
                            entityType,
                            id,
                            uid,
                            bindingAdapterPosition
                        )
                    )
                    show()
                }
            }
        }

        fun bind(data: MessageResponse.Data) {
            entityType = data.entityType
            id = data.id
            uid = data.uid
            fromusername = data.fromusername

            binding.setVariable(BR.data, data)
            binding.setVariable(BR.listener, listener)

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
        return MessViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: MessViewHolder, position: Int) {
        holder.bind(currentList[position])
    }


}