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
    private val onClickItem: (String, String) -> Unit
) : ListAdapter<HomeFeedResponse.Data, SearchAdapter.ViewHolder>(SearchDiffCallback()) {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ShapeableImageView = view.findViewById(R.id.logoCover)
        val username: TextView = view.findViewById(R.id.title)
        var avatarUrl: String = ""
        var id: String = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_mess, parent, false)
        )
        viewHolder.itemView.setOnClickListener {
            when (currentList[viewHolder.bindingAdapterPosition].entityType) {
                "user" -> onClickItem(viewHolder.avatarUrl, viewHolder.username.text.toString())
                "topic" -> onClickItem(viewHolder.username.text.toString(), viewHolder.id)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = currentList[position]
        when (data.entityType) {
            "user" -> {
                holder.avatarUrl = data.userAvatar ?: ""
                holder.username.text = data.username
                showIMG(holder.avatar, data.userAvatar)
            }

            "topic" -> {
                holder.id = data.id.toString()
                holder.username.text = data.title
                showIMG(holder.avatar, data.logo)
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