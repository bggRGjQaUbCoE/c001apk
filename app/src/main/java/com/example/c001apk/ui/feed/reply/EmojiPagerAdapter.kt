package com.example.c001apk.ui.feed.reply

import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridView
import androidx.recyclerview.widget.RecyclerView
import com.absinthe.libraries.utils.extensions.dp

class EmojiPagerAdapter(
    private val emojiList: List<List<Pair<String, Int>>>,
    private val onClickEmoji: (String) -> Unit
) : RecyclerView.Adapter<EmojiPagerAdapter.ViewHolder>() {

    class ViewHolder(val view: GridView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = GridView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.CENTER
            }
            numColumns = 7
            verticalSpacing = 4.dp
        }
        return ViewHolder(view)
    }

    override fun getItemCount() = emojiList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.adapter = EmojiGridAdapter(emojiList[position].toList(), onClickEmoji)
    }


}