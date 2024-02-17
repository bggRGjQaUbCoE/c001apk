package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.databinding.ItemCollectionListItemBinding
import com.example.c001apk.databinding.ItemFeedReplyBinding
import com.example.c001apk.databinding.ItemHomeFeedBinding
import com.example.c001apk.databinding.ItemHomeFeedRefreshCardBinding
import com.example.c001apk.databinding.ItemHomeIconLinkGridCardBinding
import com.example.c001apk.databinding.ItemHomeIconMiniScrollCardBinding
import com.example.c001apk.databinding.ItemHomeImageCarouselCardBinding
import com.example.c001apk.databinding.ItemHomeImageSquareScrollCardBinding
import com.example.c001apk.databinding.ItemHomeImageTextScrollCardBinding
import com.example.c001apk.databinding.ItemRecentHistoryBinding
import com.example.c001apk.databinding.ItemSearchApkBinding
import com.example.c001apk.databinding.ItemSearchTopicBinding
import com.example.c001apk.databinding.ItemSearchUserBinding
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.IconLinkGridCardBean
import com.example.c001apk.logic.model.Like
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.util.Utils.getColorFromAttr
import com.example.c001apk.view.LinearItemDecoration1

class AppAdapter(private val listener: ItemListener) : BaseViewTypeAdapter<ViewDataBinding>() {

    enum class ViewType {
        IMAGE_CAROUSEL_CARD_1,
        ICON_LINK_GRID_CARD,
        IMAGE_TEXT_SCROLL_CARD,
        ICON_MINI_SCROLL_CARD,
        REFRESH_CARD,
        IMAGE_SQUARE_SCROLL_CARD,
        FEED,
        FEED_VOTE,
        USER, // == CONTACTS
        TOPIC, // == PRODUCT
        APK,
        FEED_REPLY,
        COLLECTION,
        RECENT_HISTORY
    }

    inner class FeedViewHolder(val binding: ItemHomeFeedBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {

        override fun bind() {
            val feed = currentList[bindingAdapterPosition]
            binding.data = feed
            val likeData = Like().also {
                it.apply {
                    feed.userAction?.like?.let { like ->
                        isLike.set(like)
                    }
                    likeNum.set(feed.likenum)
                }
            }
            binding.likeData = likeData
            binding.listener = listener
            binding.multiImage.listener = listener
            binding.forwardedPic.listener = listener

            binding.expand.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    menuInflater.inflate(R.menu.feed_reply_menu, menu).apply {
                        menu.findItem(R.id.copy)?.isVisible = false
                        menu.findItem(R.id.delete)?.isVisible = PrefManager.uid == feed.uid
                        menu.findItem(R.id.show)?.isVisible = false
                        menu.findItem(R.id.report)?.isVisible = PrefManager.isLogin
                    }
                    setOnMenuItemClickListener(
                        PopClickListener(
                            listener,
                            it.context,
                            feed.entityType,
                            feed.id,
                            feed.uid,
                            bindingAdapterPosition
                        )
                    )
                    show()
                }
            }

            binding.like.setOnClickListener {
                listener.onLikeClick(
                    feed.entityType, feed.id,
                    bindingAdapterPosition, likeData
                )
            }

        }

    }

    inner class ImageCarouselCardViewHolder(val binding: ItemHomeImageCarouselCardBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {
        override fun bind() {
            if (!currentList[bindingAdapterPosition].entities.isNullOrEmpty()) {
                val imageCarouselCard: MutableList<HomeFeedResponse.Entities> = ArrayList()
                currentList[bindingAdapterPosition].entities?.forEach {
                    if (!it.url.startsWith("http"))
                        imageCarouselCard.add(it)
                }
                val data: MutableList<IconLinkGridCardBean> = ArrayList()
                data.add(
                    IconLinkGridCardBean(
                        imageCarouselCard[imageCarouselCard.size - 1].title,
                        imageCarouselCard[imageCarouselCard.size - 1].pic,
                        imageCarouselCard[imageCarouselCard.size - 1].url
                    )
                )
                imageCarouselCard.forEach {
                    data.add(IconLinkGridCardBean(it.title, it.pic, it.url))
                }
                data.add(
                    IconLinkGridCardBean(
                        imageCarouselCard[0].title,
                        imageCarouselCard[0].pic,
                        imageCarouselCard[0].url
                    )
                )
                var currentPosition = 0
                binding.viewPager.adapter = ImageCarouselCardAdapter(listener).also {
                    it.submitList(data)
                }
                binding.viewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        currentPosition = position
                    }

                    override fun onPageScrollStateChanged(state: Int) {
                        if (state == ViewPager2.SCROLL_STATE_IDLE) {
                            if (currentPosition == 0) {
                                binding.viewPager.setCurrentItem(data.size - 2, false)
                            } else if (currentPosition == data.size - 1) {
                                binding.viewPager.setCurrentItem(1, false)
                            }
                        }
                    }
                })
                binding.viewPager.setCurrentItem(1, false)
            }
        }
    }


    inner class IconLinkGridCardViewHolder(val binding: ItemHomeIconLinkGridCardBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {
        override fun bind() {
            if (!currentList[bindingAdapterPosition].entities.isNullOrEmpty()) {
                val data = currentList[bindingAdapterPosition].entities?.map {
                    IconLinkGridCardBean(it.title, it.pic, it.url)
                }
                val maps: MutableList<List<IconLinkGridCardBean>> = ArrayList()
                val page = currentList[bindingAdapterPosition].entities!!.size / 5
                var index = 0
                repeat(page) {
                    maps.add(data!!.subList(index * 5, (index + 1) * 5))
                    index++
                }
                binding.viewPager.adapter = IconLinkGridCardAdapter(listener).also {
                    it.submitList(maps)
                }
                if (page < 2) binding.indicator.visibility = View.GONE
                else {
                    binding.indicator.visibility = View.VISIBLE
                    binding.indicator.setViewPager(binding.viewPager)
                }
            }
        }
    }

    inner class ImageTextScrollCardViewHolder(val binding: ItemHomeImageTextScrollCardBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {
        override fun bind() {
            if (!currentList[bindingAdapterPosition].entities.isNullOrEmpty()) {
                val imageTextScrollCard = ArrayList<HomeFeedResponse.Entities>()
                currentList[bindingAdapterPosition].entities?.forEach {
                    if (it.entityType == "feed" && !BlackListUtil.checkUid(it.userInfo.uid))
                        imageTextScrollCard.add(it)
                }
                binding.title.text = currentList[bindingAdapterPosition].title
                binding.title.setPadding(10.dp, 10.dp, 10.dp, 0)
                binding.recyclerView.apply {
                    adapter = ImageTextScrollCardAdapter(listener).also {
                        it.submitList(imageTextScrollCard)
                    }
                    layoutManager = LinearLayoutManager(itemView.context).also {
                        it.orientation = LinearLayoutManager.HORIZONTAL
                    }
                    if (itemDecorationCount == 0)
                        addItemDecoration(LinearItemDecoration1(10.dp))
                }
            }

        }

    }

    inner class IconMiniScrollCardViewHolder(val binding: ItemHomeIconMiniScrollCardBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {
        override fun bind() {
            if (!currentList[bindingAdapterPosition].entities.isNullOrEmpty()) {
                val imageTextScrollCard = ArrayList<HomeFeedResponse.Entities>()
                currentList[bindingAdapterPosition].entities?.forEach {
                    if ((it.entityType == "topic" || it.entityType == "product")
                        && !TopicBlackListUtil.checkTopic(it.title)
                    )
                        imageTextScrollCard.add(it)
                }
                binding.recyclerView.apply {
                    adapter = IconMiniScrollCardAdapter(listener).also {
                        it.submitList(imageTextScrollCard)
                    }
                    layoutManager = LinearLayoutManager(itemView.context).also {
                        it.orientation = LinearLayoutManager.HORIZONTAL
                    }
                    if (itemDecorationCount == 0)
                        addItemDecoration(LinearItemDecoration1(10.dp))
                }
            }

        }
    }

    inner class RefreshCardViewHolder(val binding: ItemHomeFeedRefreshCardBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {
        override fun bind() {
            binding.textView.text = currentList[bindingAdapterPosition].title
        }
    }

    inner class ImageSquareScrollCardViewHolder(val binding: ItemHomeImageSquareScrollCardBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {
        override fun bind() {
            if (!currentList[bindingAdapterPosition].entities.isNullOrEmpty()) {
                val imageTextScrollCard = ArrayList<HomeFeedResponse.Entities>()
                currentList[bindingAdapterPosition].entities?.forEach {
                    if (it.entityType == "picCategory")
                        imageTextScrollCard.add(it)
                }
                binding.recyclerView.apply {
                    adapter = ImageSquareScrollCardAdapter(listener).also {
                        it.submitList(imageTextScrollCard)
                    }
                    layoutManager = LinearLayoutManager(itemView.context).also {
                        it.orientation = LinearLayoutManager.HORIZONTAL
                    }
                    if (itemDecorationCount == 0)
                        addItemDecoration(LinearItemDecoration1(10.dp))
                }
            }
        }
    }

    inner class UserViewHolder(val binding: ItemSearchUserBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {
        @SuppressLint("SetTextI18n")
        override fun bind() {
            val user = currentList[bindingAdapterPosition]

            binding.listener = listener
            binding.position = bindingAdapterPosition
            if (user.userInfo != null && user.fUserInfo != null) {
                binding.uid = user.userInfo.uid
                binding.uname.text = user.userInfo.username
                binding.follow.text = "${user.userInfo.follow}关注"
                binding.fans.text = "${user.userInfo.fans}粉丝"
                binding.act.text = DateUtils.fromToday(user.userInfo.logintime) + "活跃"
                ImageUtil.showIMG(binding.avatar, user.userInfo.userAvatar)
            } else if (user.userInfo == null && user.fUserInfo != null) {
                binding.uid = user.fUserInfo.uid
                binding.uname.text = user.fUserInfo.username
                binding.follow.text = "${user.fUserInfo.follow}关注"
                binding.fans.text = "${user.fUserInfo.fans}粉丝"
                binding.act.text = DateUtils.fromToday(user.fUserInfo.logintime) + "活跃"
                ImageUtil.showIMG(binding.avatar, user.fUserInfo.userAvatar)
            } else if (user.userInfo != null) {
                binding.uid = user.uid
                binding.uname.text = user.username
                binding.follow.text = "${user.follow}关注"
                binding.fans.text = "${user.fans}粉丝"
                binding.act.text = DateUtils.fromToday(user.logintime) + "活跃"
                binding.isFollow = user.isFollow
                if (user.isFollow == 0) {
                    binding.followBtn.text = "关注"
                    binding.followBtn.setTextColor(itemView.context.getColorFromAttr(rikka.preference.simplemenu.R.attr.colorPrimary))
                } else {
                    binding.followBtn.text = "已关注"
                    binding.followBtn.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                }
                binding.followBtn.visibility = if (PrefManager.isLogin) View.VISIBLE
                else View.GONE
                ImageUtil.showIMG(binding.avatar, user.userAvatar)
            }
        }
    }

    inner class TopicProductViewHolder(val binding: ItemSearchTopicBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {
        override fun bind() {
            val data = currentList[bindingAdapterPosition]
            if (data.description == "home") {
                binding.parent.setCardBackgroundColor(
                    itemView.context.getColorFromAttr(android.R.attr.windowBackground)
                )
            } else {
                binding.parent.setCardBackgroundColor(
                    itemView.context.getColor(R.color.home_card_background_color)
                )
            }
            binding.commentNum.text = if (data.entityType == "topic")
                "${data.commentnumTxt}讨论"
            else
                "${data.feedCommentNumTxt}讨论"

            binding.data = data
            binding.listener = listener
        }
    }

    inner class AppViewHolder(val binding: ItemSearchApkBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {
        override fun bind() {
            binding.data = currentList[bindingAdapterPosition]
            binding.listener = listener
        }
    }

    inner class CollectionViewHolder(val binding: ItemCollectionListItemBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {
        override fun bind() {
            binding.data = currentList[bindingAdapterPosition]
            binding.listener = listener
        }
    }

    inner class RecentHistoryViewHolder(val binding: ItemRecentHistoryBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {
        override fun bind() {
            val data = currentList[bindingAdapterPosition]
            binding.data = data
            binding.listener = listener
            binding.fans.text =
                if (data.targetType == "user")
                    "${data.fansNum}粉丝"
                else
                    "${data.commentNum}讨论"

        }
    }

    inner class FeedReplyViewHolder(val binding: ItemFeedReplyBinding) :
        BaseViewHolder<ViewDataBinding>(binding) {
        override fun bind() {
            binding.data = currentList[bindingAdapterPosition]
            binding.listener = listener
            val feed = currentList[bindingAdapterPosition]
            val likeData = Like().also {
                it.apply {
                    feed.userAction?.like?.let { like ->
                        isLike.set(like)
                    }
                    likeNum.set(feed.likenum)
                }
            }
            binding.likeData = likeData
            binding.expand.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    menuInflater.inflate(R.menu.feed_reply_menu, menu).apply {
                        menu.findItem(R.id.copy)?.isVisible = false
                        menu.findItem(R.id.delete)?.isVisible = PrefManager.uid == feed.uid
                        menu.findItem(R.id.show)?.isVisible = false
                        menu.findItem(R.id.report)?.isVisible = PrefManager.isLogin
                    }
                    setOnMenuItemClickListener(
                        PopClickListener(
                            listener,
                            it.context,
                            feed.entityType,
                            feed.id,
                            feed.uid,
                            bindingAdapterPosition
                        )
                    )
                    show()
                }
            }
            binding.like.setOnClickListener {
                listener.onLikeClick(
                    feed.entityType,
                    feed.id,
                    bindingAdapterPosition,
                    likeData
                )
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: ViewType
    ): BaseViewHolder<ViewDataBinding> {
        return when (viewType) {

            ViewType.IMAGE_CAROUSEL_CARD_1 -> ImageCarouselCardViewHolder(
                ItemHomeImageCarouselCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )

            ViewType.ICON_LINK_GRID_CARD -> IconLinkGridCardViewHolder(
                ItemHomeIconLinkGridCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent,
                    false
                )
            )

            ViewType.IMAGE_TEXT_SCROLL_CARD -> ImageTextScrollCardViewHolder(
                ItemHomeImageTextScrollCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent,
                    false
                )
            )

            ViewType.ICON_MINI_SCROLL_CARD -> IconMiniScrollCardViewHolder(
                ItemHomeIconMiniScrollCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent,
                    false
                )
            )

            ViewType.REFRESH_CARD -> RefreshCardViewHolder(
                ItemHomeFeedRefreshCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent,
                    false
                )
            )

            ViewType.IMAGE_SQUARE_SCROLL_CARD -> ImageSquareScrollCardViewHolder(
                ItemHomeImageSquareScrollCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent,
                    false
                )
            )

            ViewType.FEED -> FeedViewHolder(
                ItemHomeFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            ViewType.USER -> UserViewHolder(
                ItemSearchUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            ViewType.TOPIC -> TopicProductViewHolder(
                ItemSearchTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            ViewType.APK -> AppViewHolder(
                ItemSearchApkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            ViewType.COLLECTION -> CollectionViewHolder(
                ItemCollectionListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            ViewType.RECENT_HISTORY -> RecentHistoryViewHolder(
                ItemRecentHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            ViewType.FEED_REPLY -> FeedReplyViewHolder(
                ItemFeedReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            else -> throw IllegalArgumentException("invalid viewType: $viewType")
        }
    }

    override fun getViewType(position: Int): ViewType {
        return when (currentList[position].entityType) {
            "card" -> {
                when (currentList[position].entityTemplate) {
                    "imageCarouselCard_1" -> ViewType.IMAGE_CAROUSEL_CARD_1

                    "iconLinkGridCard" -> ViewType.ICON_LINK_GRID_CARD

                    "imageTextScrollCard" -> ViewType.IMAGE_TEXT_SCROLL_CARD

                    "iconMiniScrollCard" -> ViewType.ICON_MINI_SCROLL_CARD
                    "iconMiniGridCard" -> ViewType.ICON_MINI_SCROLL_CARD

                    "refreshCard" -> ViewType.REFRESH_CARD

                    "imageSquareScrollCard" -> ViewType.IMAGE_SQUARE_SCROLL_CARD

                    else -> throw IllegalArgumentException("entityType error: ${currentList[position].entityTemplate}")
                }
            }

            "feed" -> when (currentList[position].feedType) {
                "vote" -> ViewType.FEED // ViewType.FEED_VOTE

                else -> ViewType.FEED
            }

            "contacts" -> ViewType.USER
            "user" -> ViewType.USER

            "topic" -> ViewType.TOPIC
            "product" -> ViewType.TOPIC

            "apk" -> ViewType.APK

            "feed_reply" -> ViewType.FEED_REPLY

            "collection" -> ViewType.COLLECTION

            "recentHistory" -> ViewType.RECENT_HISTORY

            else -> throw IllegalArgumentException("entityType error: ${currentList[position].entityType}")
        }
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