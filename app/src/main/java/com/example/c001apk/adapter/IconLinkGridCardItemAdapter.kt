package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.BR
import com.example.c001apk.databinding.ItemHomeIconLinkGridCardItemBinding
import com.example.c001apk.logic.model.IconLinkGridCardBean

class IconLinkGridCardItemAdapter(
    private val listener: ItemListener
) : ListAdapter<IconLinkGridCardBean, IconLinkGridCardItemAdapter.ViewHolder>(
    IconLinkGridCardItemDiffCallback()
) {


    class ViewHolder(val binding: ItemHomeIconLinkGridCardItemBinding, val listener: ItemListener) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: IconLinkGridCardBean) {
            binding.setVariable(BR.data, data)
            binding.setVariable(BR.listener, listener)
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHomeIconLinkGridCardItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
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