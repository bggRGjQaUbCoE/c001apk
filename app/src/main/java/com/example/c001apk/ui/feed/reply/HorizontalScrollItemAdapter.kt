package com.example.c001apk.ui.feed.reply

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.ItemBean

class HorizontalScrollItemAdapter(
    private val mContext: Context,
    private val dataList: List<ItemBean>
) : RecyclerView.Adapter<HorizontalScrollItemAdapter.HorizontalItemHolder>() {

    private lateinit var iOnEmojiClickListener: IOnEmojiClickListener

    fun setIOnEmojiClickListener(iOnEmojiClickListener: IOnEmojiClickListener) {
        this.iOnEmojiClickListener = iOnEmojiClickListener
    }

    class HorizontalItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var verticalImgView: ImageView = itemView.findViewById(R.id.verticalImgView)
        var verticalText = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalItemHolder {
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.item_emoji, parent, false)
        val viewHolder = HorizontalItemHolder(view)
        viewHolder.itemView.setOnClickListener {
            iOnEmojiClickListener.onShowEmoji(viewHolder.verticalText)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: HorizontalItemHolder, position: Int) {
        val itemBean = dataList[position]
        holder.verticalImgView.setImageResource(itemBean.emoji)
        holder.verticalText = itemBean.name
    }

    override fun getItemCount() = dataList.size
}