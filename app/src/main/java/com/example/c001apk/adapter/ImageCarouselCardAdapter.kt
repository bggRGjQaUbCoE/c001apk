package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.databinding.ItemHomeImageCarouselCardItemBinding
import com.example.c001apk.logic.model.IconLinkGridCardBean

class ImageCarouselCardAdapter(
    private val listener: ItemListener
) : ListAdapter<IconLinkGridCardBean, ImageCarouselCardAdapter.ViewHolder>(
    ImageCarouselCardDiffCallback()
) {

    inner class ViewHolder(
        private val binding: ItemHomeImageCarouselCardItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: IconLinkGridCardBean, itemCount: Int) {
            with(binding) {
                this.data = data // Use direct property access
                this.listener = this@ImageCarouselCardAdapter.listener

                // Check bindingAdapterPosition to ensure it is not NO_POSITION
                val position = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return
                count.text = getItemCounterText(position, itemCount)
                executePendingBindings()
            }
        }

        private fun getItemCounterText(position: Int, itemCount: Int): String =
            if (itemCount == 1) "1/1"
            else {
                when (position) {
                    0 -> "${itemCount - 2}/${itemCount - 2}"
                    itemCount - 1 -> "1/${itemCount - 2}"
                    else -> "${position}/${itemCount - 2}"
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeImageCarouselCardItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), itemCount)
    }

}

class ImageCarouselCardDiffCallback : DiffUtil.ItemCallback<IconLinkGridCardBean>() {
    override fun areItemsTheSame(
        oldItem: IconLinkGridCardBean,
        newItem: IconLinkGridCardBean
    ): Boolean = oldItem.url == newItem.url

    override fun areContentsTheSame(
        oldItem: IconLinkGridCardBean,
        newItem: IconLinkGridCardBean
    ): Boolean = oldItem.url == newItem.url
}
