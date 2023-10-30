package com.example.c001apk.ui.fragment.feed

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.bean.ImageInfo
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.util.SpannableStringBuilderUtil


class Reply2ReplyAdapter(
    private val mContext: Context,
    private val uid: String,
    private val position: Int,
    private val reply2ReplyList: List<HomeFeedResponse.ReplyRows>
) :
    RecyclerView.Adapter<Reply2ReplyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var id = ""
        var uid = ""
        var name = ""
        val reply: TextView = view.findViewById(R.id.reply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_feed_content_reply_to_reply_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.reply.setOnClickListener {
            IOnReplyClickContainer.controller?.onReply2Reply(
                position,
                null,
                viewHolder.id,
                viewHolder.uid,
                viewHolder.name,
                "reply"
            )
        }
        viewHolder.reply.setOnLongClickListener {
            val intent = Intent(parent.context, CopyActivity::class.java)
            intent.putExtra("text", viewHolder.reply.text.toString())
            parent.context.startActivity(intent)
            true
        }
        return viewHolder
    }

    override fun getItemCount() = reply2ReplyList.size

    @SuppressLint("SetTextI18n", "RestrictedApi", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reply = reply2ReplyList[position]

        holder.id = reply.id
        holder.uid = reply.uid
        holder.name = reply.username

        /*val space = mContext.resources.getDimensionPixelSize(R.dimen.minor_space)
        if (position == 0)
            holder.reply.setPadding(space, space, space, space)
        else
            holder.reply.setPadding(space, 0, space, space)*/

        holder.reply.highlightColor = Color.TRANSPARENT

        val text =
            if (reply.ruid == uid) {
                if (reply.pic == "")
                    """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>: ${reply.message}"""
                else {
                    if (reply.message == "[图片]")
                        """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>: ${reply.message} <a class=\"feed-forward-pic\" href=${reply.pic}> 查看图片(${reply.picArr?.size})</a> """
                    else
                        """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>: ${reply.message} <a class=\"feed-forward-pic\" href=${reply.pic}> [图片] 查看图片(${reply.picArr?.size})</a> """
                }
            } else {
                if (reply.pic == "")
                    """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>回复<a class="feed-link-uname" href="/u/${reply.rusername}">${reply.rusername}</a>: ${reply.message}"""
                else {
                    if (reply.message == "[图片]")
                        """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>回复<a class="feed-link-uname" href="/u/${reply.rusername}">${reply.rusername}</a>: ${reply.message} <a class=\"feed-forward-pic\" href=${reply.pic}> 查看图片(${reply.picArr?.size})</a> """
                    else
                        """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>回复<a class="feed-link-uname" href="/u/${reply.rusername}">${reply.rusername}</a>: ${reply.message} <a class=\"feed-forward-pic\" href=${reply.pic}> [图片] 查看图片(${reply.picArr?.size})</a> """
                }
            }

        val urlList: MutableList<ImageInfo> = ArrayList()
        if (!reply.picArr.isNullOrEmpty()){
            for (element in reply.picArr){
                val imageInfo = ImageInfo()
                imageInfo.thumbnailUrl = "$element.s.jpg"
                imageInfo.originUrl = element
                urlList.add(imageInfo)
            }
        }

        holder.reply.movementMethod = LinkMovementMethod.getInstance()
        holder.reply.text = SpannableStringBuilderUtil.setText(
            mContext,
            text,
            (holder.reply.textSize * 1.3).toInt(),
            urlList
        )


    }

    /*@SuppressLint("RestrictedApi")
    private fun setTextFieldColor(text: String, start: Int, end: Int): SpannableString {
        val spannableString = SpannableString(text)
        val foregroundColorSpan = ForegroundColorSpan(
            ThemeUtils.getThemeAttrColor(
                mContext,
                com.google.android.material.R.attr.colorPrimary
            )
        )
        spannableString.setSpan(foregroundColorSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannableString
    }*/

}