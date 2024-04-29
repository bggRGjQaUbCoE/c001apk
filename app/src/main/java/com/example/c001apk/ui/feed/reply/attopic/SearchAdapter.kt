package com.example.c001apk.ui.feed.reply.attopic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.util.ImageUtil.showIMG
import com.google.android.material.imageview.ShapeableImageView

class SearchAdapter(
    private val onClickUser: (String, String) -> Unit,
    private val onClickTopic: (String) -> Unit
) : ListAdapter<HomeFeedResponse.Data, RecyclerView.ViewHolder>(SearchDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (val type = currentList[position].entityType) {
            "user" -> 0
            "topic" -> 1
            else -> throw IllegalArgumentException("entityType error: $type")
        }
    }

    class UserViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ShapeableImageView = view.findViewById(R.id.logoCover)
        val username: TextView = view.findViewById(R.id.title)
        var avatarUrl: String = ""
    }

    class TopicViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val logo: ShapeableImageView = view.findViewById(R.id.logoCover)
        val title: TextView = view.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val viewHolder = UserViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_message_mess, parent, false)
                )
                viewHolder.itemView.setOnClickListener {
                    onClickUser(viewHolder.avatarUrl, viewHolder.username.text.toString())
                }
                viewHolder
            }

            1 -> {
                val viewHolder = TopicViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_message_mess, parent, false)
                )
                viewHolder.itemView.setOnClickListener {
                    onClickTopic(viewHolder.title.text.toString())
                }
                viewHolder
            }

            else -> throw IllegalArgumentException("viewType error: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> {
                val user = currentList[position]
                holder.avatarUrl = user.userAvatar ?: ""
                holder.username.text = user.username
                showIMG(holder.avatar, user.userAvatar)
            }

            is TopicViewHolder -> {
                val topic = currentList[position]
                holder.title.text = topic.title
                showIMG(holder.logo, topic.logo)
            }
        }
    }
}

class SearchDiffCallback : DiffUtil.ItemCallback<HomeFeedResponse.Data>() {
    override fun areItemsTheSame(
        oldItem: HomeFeedResponse.Data,
        newItem: HomeFeedResponse.Data
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: HomeFeedResponse.Data,
        newItem: HomeFeedResponse.Data
    ): Boolean {
        return oldItem.id == newItem.id
    }
}