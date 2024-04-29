package com.example.c001apk.ui.feed.reply.attopic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.databinding.ItemAtUserBinding
import com.example.c001apk.logic.model.RecentAtUser
import com.example.c001apk.util.ImageUtil.showIMG

class AtUserAdapter(
    private val onClickUser: (Int, Boolean) -> Unit
) : ListAdapter<RecentAtUser, AtUserAdapter.ViewHolder>(AtTopicDiffCallback()) {

    class ViewHolder(val binding: ItemAtUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(
            ItemAtUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        fun onClick() {
            onClickUser(viewHolder.bindingAdapterPosition, viewHolder.binding.checkBox.isChecked)
        }
        viewHolder.binding.checkBox.setOnClickListener {
            onClick()
        }
        viewHolder.itemView.setOnClickListener {
            viewHolder.binding.checkBox.isChecked = !viewHolder.binding.checkBox.isChecked
            onClick()
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = currentList[position]
        holder.binding.username.text = data.username
        if (data.avatar.isNotBlank()) {
            showIMG(holder.binding.avatar, data.avatar)
        }
    }

    fun isGroupHead(childPosition: Int): Boolean {
        return if (childPosition == 0) {
            true
        } else {
            val thisGroup = currentList[childPosition].group
            val lastGroup = currentList[childPosition - 1].group
            thisGroup != lastGroup
        }
    }

    fun getGroupName(childPosition: Int): String {
        return currentList[childPosition].group
    }

}

class AtTopicDiffCallback : DiffUtil.ItemCallback<RecentAtUser>() {
    override fun areItemsTheSame(
        oldItem: RecentAtUser,
        newItem: RecentAtUser
    ): Boolean {
        return oldItem.username == newItem.username && oldItem.group == newItem.group
    }

    override fun areContentsTheSame(
        oldItem: RecentAtUser,
        newItem: RecentAtUser
    ): Boolean {
        return oldItem.username == newItem.username && oldItem.group == newItem.group
    }

}
