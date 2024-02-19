package com.example.c001apk.adapter

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.c001apk.logic.model.HomeFeedResponse

abstract class BaseViewTypeAdapter<T : ViewDataBinding> :
    ListAdapter<HomeFeedResponse.Data, BaseViewHolder<T>>(HomeFeedDiffCallback()) {

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.bind(currentList[position])
        holder.dataBinding.executePendingBindings()
    }

}

class HomeFeedDiffCallback : DiffUtil.ItemCallback<HomeFeedResponse.Data>() {
    override fun areItemsTheSame(
        oldItem: HomeFeedResponse.Data,
        newItem: HomeFeedResponse.Data
    ): Boolean {
        return oldItem.entityId == newItem.entityId
    }

    override fun areContentsTheSame(
        oldItem: HomeFeedResponse.Data,
        newItem: HomeFeedResponse.Data
    ): Boolean {
        return oldItem.entityId == newItem.entityId && oldItem.lastupdate == newItem.lastupdate
    }
}
