package com.example.c001apk.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.example.c001apk.logic.model.ItemBean
import com.example.c001apk.ui.fragment.minterface.IOnEmojiClickListener

class HorizontalScrollAdapter(
    private val context: Context,
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
        val recyclerView = RecyclerView(context)
        val layoutManager = GridLayoutManager(context, 7, GridLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        val itemAdapter = HorizontalScrollItemAdapter(
            context, maps[position]
        )
        itemAdapter.setIOnEmojiClickListener(this)
        recyclerView.adapter = itemAdapter
        container.addView(recyclerView) //将recyclerView作为子视图加入 container即为viewpager
        return recyclerView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun onShowEmoji(name: String) {
        iOnEmojiClickListener.onShowEmoji(name)
    }
}