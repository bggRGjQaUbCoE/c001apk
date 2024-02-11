package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.databinding.ItemHomeIconLinkGridCardItemBinding
import com.example.c001apk.logic.model.IconLinkGridCardBean

class IconLinkGridCardItemAdapter(
    private val listener: ItemListener
) : ListAdapter<IconLinkGridCardBean, IconLinkGridCardItemAdapter.ViewHolder>(
    IconLinkGridCardItemDiffCallback()
) {


    inner class ViewHolder(val binding: ItemHomeIconLinkGridCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.data = currentList[bindingAdapterPosition]
            binding.listener = listener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHomeIconLinkGridCardItemBinding.inflate(
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

class IconLinkGridCardItemDiffCallback : DiffUtil.ItemCallback<IconLinkGridCardBean>() {
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