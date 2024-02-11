package com.example.c001apk.adapter

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.databinding.ItemHomeImageTextScrollCardItemBinding
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.util.DensityTool

class ImageTextScrollCardAdapter(
    private val listener: ItemListener
) :
    ListAdapter<HomeFeedResponse.Entities, ImageTextScrollCardAdapter.ViewHolder>(
        ImageTextScrollCardDiffCallback()
    ) {

    inner class ViewHolder(val binding: ItemHomeImageTextScrollCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.data = currentList[bindingAdapterPosition]
            binding.listener = listener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemHomeImageTextScrollCardItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        val padding = 50.dp
        val imageWidth =
            if (parent.context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                DensityTool.getScreenWidth(parent.context) - padding
            else
                DensityTool.getScreenWidth(parent.context) / 2 - padding
        binding.root.layoutParams.width = (imageWidth - imageWidth / 3)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

}

class ImageTextScrollCardDiffCallback : DiffUtil.ItemCallback<HomeFeedResponse.Entities>() {
    override fun areItemsTheSame(
        oldItem: HomeFeedResponse.Entities,
        newItem: HomeFeedResponse.Entities
    ): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(
        oldItem: HomeFeedResponse.Entities,
        newItem: HomeFeedResponse.Entities
    ): Boolean {
        return oldItem.url == newItem.url
    }
}