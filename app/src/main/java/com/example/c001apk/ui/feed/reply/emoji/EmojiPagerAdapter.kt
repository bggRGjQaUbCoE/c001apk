package com.example.c001apk.ui.feed.reply.emoji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.c001apk.R
import com.example.c001apk.view.circleindicator.CircleIndicator3

class EmojiPagerAdapter(
    private val emojiList: List<List<List<Pair<String, Int>>>>,
    private val onClickEmoji: (String) -> Unit,
    private val onCountStart: () -> Unit,
    private val onCountStop: () -> Unit
) : RecyclerView.Adapter<EmojiPagerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)
        val indicator: CircleIndicator3 = view.findViewById(R.id.indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_emoji_child_viewpager, parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount() = emojiList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewPager.adapter = EmojiChildPagerAdapter(
            emojiList[position], onClickEmoji, onCountStart, onCountStop
        )
        holder.indicator.setViewPager(holder.viewPager)
    }

}