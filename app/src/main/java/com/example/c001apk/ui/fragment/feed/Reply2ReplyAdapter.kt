package com.example.c001apk.ui.fragment.feed

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.bean.ImageInfo
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.util.EmojiUtil
import com.example.c001apk.view.CenteredImageSpan
import com.example.c001apk.view.MyURLSpan
import java.util.regex.Pattern


class Reply2ReplyAdapter(
    private val mContext: Context,
    private val uid: String,
    private val reply2ReplyList: List<HomeFeedResponse.ReplyRows>
) :
    RecyclerView.Adapter<Reply2ReplyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var id = ""
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
                viewHolder.id,
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
        holder.name = reply.username

        /*val space = mContext.resources.getDimensionPixelSize(R.dimen.minor_space)
        if (position == 0)
            holder.reply.setPadding(space, space, space, space)
        else
            holder.reply.setPadding(space, 0, space, space)*/

        holder.reply.highlightColor = Color.TRANSPARENT

        val urlList: MutableList<ImageInfo> = ArrayList()
        if (reply.pic != "") {
            for (url in reply.picArr) {
                val imageInfo = ImageInfo()
                imageInfo.thumbnailUrl = "$url.s.jpg"
                imageInfo.originUrl = url
                urlList.add(imageInfo)
            }
        }

        val text =
            if (reply.ruid == uid) {
                if (reply.pic == "")
                    """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>: ${reply.message}"""
                else {
                    if (reply.message == "[图片]")
                        """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>: ${reply.message} <a class=\"feed-forward-pic\" href=${reply.pic}> 查看图片(${reply.picArr.size})</a> """
                    else
                        """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>: ${reply.message} <a class=\"feed-forward-pic\" href=${reply.pic}> [图片] 查看图片(${reply.picArr.size})</a> """
                }
            } else {
                if (reply.pic == "")
                    """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>回复<a class="feed-link-uname" href="/u/${reply.rusername}">${reply.rusername}</a>: ${reply.message}"""
                else {
                    if (reply.message == "[图片]")
                        """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>回复<a class="feed-link-uname" href="/u/${reply.rusername}">${reply.rusername}</a>: ${reply.message} <a class=\"feed-forward-pic\" href=${reply.pic}> 查看图片(${reply.picArr.size})</a> """
                    else
                        """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>回复<a class="feed-link-uname" href="/u/${reply.rusername}">${reply.rusername}</a>: ${reply.message} <a class=\"feed-forward-pic\" href=${reply.pic}> [图片] 查看图片(${reply.picArr.size})</a> """
                }
            }

        val mess = Html.fromHtml(
            text.replace("\n", " <br />"),
            Html.FROM_HTML_MODE_COMPACT
        )
        val builder = SpannableStringBuilder(mess)
        val pattern = Pattern.compile("\\[[^\\]]+\\]")
        val matcher = pattern.matcher(builder)
        val urls = builder.getSpans(
            0, text.length,
            URLSpan::class.java
        )
        for (url in urls) {
            val myURLSpan = MyURLSpan(mContext, null, url.url, urlList)
            val start = builder.getSpanStart(url)
            val end = builder.getSpanEnd(url)
            val flags = builder.getSpanFlags(url)
            builder.setSpan(myURLSpan, start, end, flags)
            builder.removeSpan(url)
        }
        holder.reply.text = builder
        holder.reply.movementMethod = LinkMovementMethod.getInstance()
        while (matcher.find()) {
            val group = matcher.group()
            if (EmojiUtil.getEmoji(group) != -1) {
                val emoji: Drawable =
                    mContext.getDrawable(EmojiUtil.getEmoji(group))!!
                emoji.setBounds(
                    0,
                    0,
                    (holder.reply.textSize * 1.3).toInt(),
                    (holder.reply.textSize * 1.3).toInt()
                )
                val imageSpan = CenteredImageSpan(emoji)
                builder.setSpan(
                    imageSpan,
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                holder.reply.text = builder
            }
        }

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