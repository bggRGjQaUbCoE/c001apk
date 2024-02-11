package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.databinding.ItemRecyclerviewBinding
import com.example.c001apk.logic.model.IconLinkGridCardBean

class IconLinkGridCardAdapter(
    private val listener: ItemListener,
) : ListAdapter<List<IconLinkGridCardBean>, IconLinkGridCardAdapter.ViewHolder>(
    IconLinkGridCardDiffCallback()
) {

    inner class ViewHolder(val binding: ItemRecyclerviewBinding) :
        RecyclerView.ViewHolder(binding.recyclerView) {
        fun bind() {
            val layoutManager =
                GridLayoutManager(itemView.context, 5, GridLayoutManager.VERTICAL, false)
            binding.recyclerView.layoutManager = layoutManager
            binding.recyclerView.isNestedScrollingEnabled = false
            binding.recyclerView.adapter = IconLinkGridCardItemAdapter(listener).also {
                it.submitList(currentList[bindingAdapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRecyclerviewBinding.inflate(
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

class IconLinkGridCardDiffCallback : DiffUtil.ItemCallback<List<IconLinkGridCardBean>>() {
    override fun areItemsTheSame(
        oldItem: List<IconLinkGridCardBean>,
        newItem: List<IconLinkGridCardBean>
    ): Boolean {
        return oldItem[0].url == newItem[0].url
    }

    override fun areContentsTheSame(
        oldItem: List<IconLinkGridCardBean>,
        newItem: List<IconLinkGridCardBean>
    ): Boolean {
        return oldItem.size == newItem.size && oldItem.last().url == newItem.last().url
    }
}