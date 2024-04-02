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

    class ViewHolder(val binding: ItemRecyclerviewBinding, val listener: ItemListener) :
        RecyclerView.ViewHolder(binding.recyclerView) {
        fun bind(data: List<IconLinkGridCardBean>) {
            binding.recyclerView.apply {
                isNestedScrollingEnabled = false
                layoutManager =
                    GridLayoutManager(itemView.context, 5, GridLayoutManager.VERTICAL, false)
                adapter = IconLinkGridCardItemAdapter(listener).also {
                    it.submitList(data)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRecyclerviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

}

class IconLinkGridCardDiffCallback : DiffUtil.ItemCallback<List<IconLinkGridCardBean>>() {
    override fun areItemsTheSame(
        oldItem: List<IconLinkGridCardBean>,
        newItem: List<IconLinkGridCardBean>
    ): Boolean {
        return oldItem.first().url == newItem.first().url
    }

    override fun areContentsTheSame(
        oldItem: List<IconLinkGridCardBean>,
        newItem: List<IconLinkGridCardBean>
    ): Boolean {
        return oldItem.size == newItem.size
    }
}