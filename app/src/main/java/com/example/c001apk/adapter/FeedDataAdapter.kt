package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.databinding.ItemFeedArticleImageBinding
import com.example.c001apk.databinding.ItemFeedArticleShareUrlBinding
import com.example.c001apk.databinding.ItemFeedArticleTextBinding
import com.example.c001apk.databinding.ItemFeedContentBinding
import com.example.c001apk.logic.model.FeedArticleContentBean
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.Like

class FeedDataAdapter(
    private val listener: ItemListener,
    private val feedDataList: List<HomeFeedResponse.Data>?,
    private val articleList: List<FeedArticleContentBean.Data>?,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class FeedViewHolder(val binding: ItemFeedContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val feed = feedDataList!![bindingAdapterPosition]
            binding.data = feed
            binding.likeData = Like().also {
                it.apply {
                    feed.userAction?.like?.let { like ->
                        isLike.set(like)
                    }
                    likeNum.set(feed.likenum)
                }
            }
            binding.listener = listener
            binding.multiImage.listener = listener
            binding.executePendingBindings()
        }
    }

    inner class TextViewHolder(val binding: ItemFeedArticleTextBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.data = articleList!![bindingAdapterPosition]
            binding.listener = listener
            binding.executePendingBindings()
        }
    }

    inner class ImageViewHolder(val binding: ItemFeedArticleImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.data = articleList!![bindingAdapterPosition]
            binding.imageView.listener = listener
            binding.executePendingBindings()
        }
    }

    inner class ShareUrlViewHolder(val binding: ItemFeedArticleShareUrlBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.data = articleList!![bindingAdapterPosition]
            binding.listener = listener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
        return onCreateViewHolder(parent, getViewType(position))
    }

    fun onCreateViewHolder(parent: ViewGroup, feedType: FeedType): RecyclerView.ViewHolder {
        return when (feedType) {

            FeedType.FEED -> FeedViewHolder(
                ItemFeedContentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            FeedType.TEXT -> TextViewHolder(
                ItemFeedArticleTextBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            FeedType.IMAGE -> ImageViewHolder(
                ItemFeedArticleImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            FeedType.SHARE_URL -> ShareUrlViewHolder(
                ItemFeedArticleShareUrlBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return if (feedDataList.isNullOrEmpty() && !articleList.isNullOrEmpty()) articleList.size
        else if (!feedDataList.isNullOrEmpty() && articleList.isNullOrEmpty()) feedDataList.size
        else 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, getViewType(position))
    }

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, feedType: FeedType) {
        when (holder) {
            is FeedViewHolder -> holder.bind()
            is TextViewHolder -> holder.bind()
            is ImageViewHolder -> holder.bind()
            is ShareUrlViewHolder -> holder.bind()
        }
    }

    override fun getItemViewType(position: Int) = position

    private fun getViewType(position: Int): FeedType {
        return if (articleList.isNullOrEmpty()) FeedType.FEED
        else when (articleList[position].type) {
            "text" -> FeedType.TEXT
            "image" -> FeedType.IMAGE
            "shareUrl" -> FeedType.SHARE_URL
            else -> throw IllegalArgumentException("invalid article type: ${articleList[position].type}")
        }
    }

    enum class FeedType {
        FEED,
        TEXT,
        IMAGE,
        SHARE_URL
    }

}