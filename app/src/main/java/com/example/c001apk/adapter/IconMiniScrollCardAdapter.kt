package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.databinding.ItemHomeIconMiniScrollCardItemBinding
import com.example.c001apk.logic.model.HomeFeedResponse

class IconMiniScrollCardAdapter(
    private val listener: ItemListener
) :
    ListAdapter<HomeFeedResponse.Entities, IconMiniScrollCardAdapter.ViewHolder>(
        ImageTextScrollCardDiffCallback()
    ) {

    inner class ViewHolder(val binding: ItemHomeIconMiniScrollCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.data = currentList[bindingAdapterPosition]
            binding.listener = listener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHomeIconMiniScrollCardItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

}