package com.example.c001apk.adapter

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import com.example.c001apk.logic.model.HomeFeedResponse

abstract class BaseViewTypeAdapter<T : ViewDataBinding> :
    ListAdapter<HomeFeedResponse.Data, BaseViewHolder<T>>(HomeFeedDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): BaseViewHolder<T> {
        return onCreateViewHolder(parent, getViewType(position))
    }

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: AppAdapter.ViewType): BaseViewHolder<T>

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.bind()
        holder.dataBinding.executePendingBindings()
    }

    override fun getItemViewType(position: Int) = position

    protected abstract fun getViewType(position: Int): AppAdapter.ViewType

}

