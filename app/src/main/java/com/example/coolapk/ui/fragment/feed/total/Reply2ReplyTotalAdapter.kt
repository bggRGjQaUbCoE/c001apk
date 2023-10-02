package com.example.coolapk.ui.fragment.feed.total

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bili.util.PubDateUtil
import com.example.coolapk.R
import com.example.coolapk.logic.model.HomeFeedResponse
import com.example.coolapk.ui.fragment.feed.FeedContentPicAdapter
import com.example.coolapk.util.ImageShowUtil


class Reply2ReplyTotalAdapter(
    private val mContext: Context,
    private val replyList: List<HomeFeedResponse.Data>
) : RecyclerView.Adapter<Reply2ReplyTotalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        val message: TextView = view.findViewById(R.id.message)
        val pubDate: TextView = view.findViewById(R.id.pubDate)
        val like: TextView = view.findViewById(R.id.like)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val reply: TextView = view.findViewById(R.id.reply)
        val picRecyclerView: RecyclerView = view.findViewById(R.id.picRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reply_to_reply_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = replyList.size

    @SuppressLint("SetTextI18n", "RestrictedApi", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reply = replyList[position]
        holder.uname.text = reply.username
        holder.message.text = Html.fromHtml(
            reply.message.replace("\n", "<br />"),
            Html.FROM_HTML_MODE_COMPACT
        )
        holder.pubDate.text = PubDateUtil.time(reply.dateline)
        holder.like.text = reply.likenum
        val drawableLike: Drawable = mContext.getDrawable(R.drawable.ic_like)!!
        drawableLike.setBounds(
            0,
            0,
            holder.like.textSize.toInt(),
            holder.like.textSize.toInt()
        )
        holder.like.setCompoundDrawables(drawableLike, null, null, null)
        holder.reply.text = reply.replynum
        val drawableReply: Drawable = mContext.getDrawable(R.drawable.ic_message)!!
        drawableReply.setBounds(
            0,
            0,
            holder.like.textSize.toInt(),
            holder.like.textSize.toInt()
        )
        holder.reply.setCompoundDrawables(drawableReply, null, null, null)
        ImageShowUtil.showAvatar(holder.avatar, reply.userAvatar)

        if (reply.picArr != null && reply.picArr.isNotEmpty()) {
            holder.picRecyclerView.visibility = View.VISIBLE
            val mAdapter = FeedContentPicAdapter(reply.picArr)
            val count =
                if (reply.picArr.size < 3) reply.picArr.size
                else 3
            val mLayoutManager = GridLayoutManager(mContext, count)
            holder.picRecyclerView.apply {
                adapter = mAdapter
                layoutManager = mLayoutManager
            }
        } else holder.picRecyclerView.visibility = View.GONE

    }

}