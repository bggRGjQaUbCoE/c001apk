package com.example.c001apk.ui.feed.reply.attopic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.databinding.ItemAtUserBinding
import com.example.c001apk.logic.model.RecentAtUser
import com.example.c001apk.util.ImageUtil.showIMG

class AtUserAdapter(
    private val type: String,
    private val onClickUser: (RecentAtUser, Boolean) -> Unit,
    private val onClickTopic: (String, String) -> Unit
) : ListAdapter<RecentAtUser, AtUserAdapter.ViewHolder>(AtTopicDiffCallback()) {

    var listSize: Int = 0
    private var checkMap = HashMap<Int, Boolean>()

    class ViewHolder(val binding: ItemAtUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(
            ItemAtUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        viewHolder.binding.checkBox.isVisible = type == "user"

        fun onClick() {
            checkMap[viewHolder.bindingAdapterPosition + listSize] =
                viewHolder.binding.checkBox.isChecked
            onClickUser(
                currentList[viewHolder.bindingAdapterPosition],
                viewHolder.binding.checkBox.isChecked
            )
        }

        if (type == "user") {
            viewHolder.binding.checkBox.setOnClickListener {
                onClick()
            }
        }
        viewHolder.itemView.setOnClickListener {
            if (type == "user") {
                viewHolder.binding.checkBox.isChecked = !viewHolder.binding.checkBox.isChecked
                onClick()
            } else {
                onClickTopic(
                    currentList[viewHolder.bindingAdapterPosition].username,
                    currentList[viewHolder.bindingAdapterPosition].id.toString()
                )
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = currentList[position]
        holder.binding.username.text = data.username
        if (data.avatar.isNotBlank()) {
            showIMG(holder.binding.avatar, data.avatar)
        }
        if (type == "user")
            holder.binding.checkBox.isChecked = checkMap.getOrDefault(position + listSize, false)
    }

    fun isGroupHead(childPosition: Int): Boolean {
        return when (childPosition) {
            0 -> false

            1 -> currentList.getOrNull(0)?.username != null

            else -> {
                val thisGroup = currentList.getOrNull(childPosition - 1)?.group
                val lastGroup = currentList.getOrNull(childPosition - 2)?.group
                thisGroup != lastGroup && thisGroup != null && lastGroup != null
            }
        }
    }

    fun getGroupName(childPosition: Int): String {
        return when (currentList.getOrNull(childPosition - 1)?.group) {
            "recent" -> "最近联系人"
            "follow" -> "好友"
            "recentTopic" -> "最近参与"
            "hotTopic" -> "热门话题"
            else -> ""
        }
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