package com.example.c001apk.ui.feed.reply

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.example.c001apk.logic.model.ItemBean

class HorizontalScrollAdapter(
    private val mContext: Context,
    private val maps: List<List<ItemBean>>
) : PagerAdapter(), IOnEmojiClickListener {

    private lateinit var iOnEmojiClickListener: IOnEmojiClickListener

    fun setIOnEmojiClickListener(iOnEmojiClickListener: IOnEmojiClickListener) {
        this.iOnEmojiClickListener = iOnEmojiClickListener
    }

    override fun getCount() = maps.size //页数

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view === o
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val recyclerView = RecyclerView(mContext)
        val layoutManager = GridLayoutManager(mContext, 7, GridLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        val itemAdapter = HorizontalScrollItemAdapter(
            mContext, maps[position]
        )
        itemAdapter.setIOnEmojiClickListener(this)
        recyclerView.adapter = itemAdapter
        container.addView(recyclerView)
        return recyclerView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun onShowEmoji(name: String) {
        iOnEmojiClickListener.onShowEmoji(name)
    }
}