package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.BR
import com.example.c001apk.databinding.ItemHomeImageCarouselCardItemBinding
import com.example.c001apk.logic.model.IconLinkGridCardBean

class ImageCarouselCardAdapter(
    private val listener: ItemListener
) :
    ListAdapter<IconLinkGridCardBean, ImageCarouselCardAdapter.ViewHolder>(
        ImageCarouselCardDiffCallback()
    ) {
    class ViewHolder(
        val binding: ItemHomeImageCarouselCardItemBinding,
        val listener: ItemListener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: IconLinkGridCardBean, itemCount: Int) {
            binding.setVariable(BR.data, data)
            binding.setVariable(BR.listener, listener)
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
            ), listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], itemCount)
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