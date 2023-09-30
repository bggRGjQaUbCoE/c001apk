package com.example.coolapk.ui.fragment.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.coolapk.R
import com.example.coolapk.logic.model.HomeFeedResponse
import com.example.coolapk.util.ImageShowUtil
import com.example.coolapk.util.LinearItemDecoration1
import com.example.coolapk.util.SpacesItemDecoration


class HomeFeedAdapter(
    private val mContext: Context,
    private val homeFeedList: List<HomeFeedResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ImageCarouselCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    class IconLinkGridCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val uname: TextView = view.findViewById(R.id.uname)
        val from: TextView = view.findViewById(R.id.from)
        val device: TextView = view.findViewById(R.id.device)
        val message: TextView = view.findViewById(R.id.message)
    }

    class ImageTextScrollCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            0 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_home_image_carousel_card, parent, false)
                return ImageCarouselCardViewHolder(view)
            }

            1 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_home_icon_link_grid_card, parent, false)
                return IconLinkGridCardViewHolder(view)
            }

            2 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_home_feed, parent, false)
                return FeedViewHolder(view)
            }

            else -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_home_image_text_scroll_card, parent, false)
                return ImageTextScrollCardViewHolder(view)
            }
        }

    }

    override fun getItemCount() = homeFeedList.size

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageCarouselCardViewHolder -> {
                val imageCarouselCard = homeFeedList[position].entities
                val mAdapter = ImageCarouselCardAdapter(imageCarouselCard)
                val mLayoutManager = LinearLayoutManager(mContext)
                mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                holder.recyclerView.onFlingListener = null
                PagerSnapHelper().attachToRecyclerView(holder.recyclerView)
                holder.recyclerView.apply {
                    adapter = mAdapter
                    layoutManager = mLayoutManager
                }
            }

            is IconLinkGridCardViewHolder -> {
                val iconLinkGridCard = homeFeedList[position].entities
                val mAdapter = IconLinkGridCardAdapter(iconLinkGridCard)
                val mLayoutManager = GridLayoutManager(mContext, 5)
                val space = mContext.resources.getDimensionPixelSize(R.dimen.normal_space)
                val spaceValue = HashMap<String, Int>()
                spaceValue[SpacesItemDecoration.TOP_SPACE] = space
                spaceValue[SpacesItemDecoration.BOTTOM_SPACE] = space
                spaceValue[SpacesItemDecoration.LEFT_SPACE] = space
                spaceValue[SpacesItemDecoration.RIGHT_SPACE] = space
                holder.recyclerView.apply {
                    adapter = mAdapter
                    layoutManager = mLayoutManager
                    if (itemDecorationCount == 0)
                        addItemDecoration(SpacesItemDecoration(5, spaceValue, true))
                }
            }

            is FeedViewHolder -> {
                val feed = homeFeedList[position]
                holder.uname.text = feed.username
                holder.device.text = feed.deviceTitle
                holder.from.text = feed.infoHtml
                holder.message.text = feed.message
                ImageShowUtil.showAvatar(holder.avatar, feed.userAvatar)
            }

            is ImageTextScrollCardViewHolder -> {
                val imageTextScrollCard = homeFeedList[position].entities
                val mAdapter = ImageTextScrollCardAdapter(mContext, imageTextScrollCard)
                val mLayoutManager = LinearLayoutManager(mContext)
                mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                val space = mContext.resources.getDimensionPixelSize(R.dimen.normal_space)
                holder.title.text = homeFeedList[position].title
                holder.title.setPadding(space, space, space, space)
                val drawable: Drawable = mContext.getDrawable(R.drawable.ic_forward)!!
                drawable.setBounds(
                    0,
                    0,
                    holder.title.textSize.toInt(),
                    holder.title.textSize.toInt()
                )
                holder.title.setCompoundDrawables(null, null, drawable, null)
                holder.recyclerView.apply {
                    adapter = mAdapter
                    layoutManager = mLayoutManager
                    if (itemDecorationCount == 0)
                        addItemDecoration(LinearItemDecoration1(space))
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (homeFeedList[position].entityType) {
            "imageCarouselCard_1" -> return 0
            "iconLinkGridCard" -> return 1
            "feed" -> return 2
            else -> return 3
            //"imageTextScrollCard" -> return 3
        }
    }

    private fun http2https(url: String) =
        if (StringBuilder(url)[4] != 's')
            StringBuilder(url).insert(4, "s").toString()
        else url


}