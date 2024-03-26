package com.example.c001apk.adapter

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.BR
import com.example.c001apk.databinding.ItemHomeImageSquareScrollCardItemBinding
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.util.DensityTool

class ImageSquareScrollCardAdapter(
    private val listener: ItemListener
) :
    ListAdapter<HomeFeedResponse.Entities, ImageSquareScrollCardAdapter.ViewHolder>(
        ImageTextScrollCardDiffCallback()
    ) {
    class ViewHolder(
        val binding: ItemHomeImageSquareScrollCardItemBinding,
        val listener: ItemListener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: HomeFeedResponse.Entities) {
            binding.setVariable(BR.data, data)
            binding.setVariable(BR.listener, listener)
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemHomeImageSquareScrollCardItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        val isPortrait by lazy { parent.context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
        val padding =
            if (isPortrait) 80.dp
            else 90.dp
        val imageWidth =
            if (isPortrait)
                DensityTool.getScreenWidth(parent.context) - padding
            else
                DensityTool.getScreenWidth(parent.context) / 2 - padding
        binding.root.layoutParams.width = imageWidth / 5
        return ViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

}
