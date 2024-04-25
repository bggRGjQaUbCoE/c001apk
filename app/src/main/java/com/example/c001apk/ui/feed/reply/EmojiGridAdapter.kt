package com.example.c001apk.ui.feed.reply

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.absinthe.libraries.utils.extensions.dp


class EmojiGridAdapter(
    private val emojiList: List<Pair<String, Int>>,
    private val onClickEmoji: (String) -> Unit
) : BaseAdapter() {

    override fun getCount() = emojiList.size

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val emoji = emojiList[position]
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(28.dp,28.dp)
        }
        imageView.setImageResource(emoji.second)
        imageView.setOnClickListener {
            onClickEmoji(emoji.first)
        }
        return imageView
    }

}