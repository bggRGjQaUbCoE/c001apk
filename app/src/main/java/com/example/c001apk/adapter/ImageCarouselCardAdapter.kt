package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.databinding.ItemHomeImageCarouselCardItemBinding
import com.example.c001apk.logic.model.IconLinkGridCardBean

class ImageCarouselCardAdapter(
    private val listener: ItemListener
) :
    ListAdapter<IconLinkGridCardBean, ImageCarouselCardAdapter.ViewHolder>(
        ImageCarouselCardDiffCallback()
    ) {

    inner class ViewHolder(val binding: ItemHomeImageCarouselCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind() {
            binding.data = currentList[bindingAdapterPosition]
            binding.listener = listener
            binding.count.text =
                if (itemCount == 1) "1/1"
                else {
                    when (bindingAdapterPosition) {
                        0 -> "${itemCount - 2}/${itemCount - 2}"
                        itemCount - 1 -> "1/${itemCount - 2}"
                        else -> "${bindingAdapterPosition}/${itemCount - 2}"
                    }
                }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHomeImageCarouselCardItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

}

class ImageCarouselCardDiffCallback : DiffUtil.ItemCallback<IconLinkGridCardBean>() {
    override fun areItemsTheSame(
        oldItem: IconLinkGridCardBean,
        newItem: IconLinkGridCardBean
    ): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(
        oldItem: IconLinkGridCardBean,
        newItem: IconLinkGridCardBean
    ): Boolean {
        return oldItem.url == newItem.url
    }
}