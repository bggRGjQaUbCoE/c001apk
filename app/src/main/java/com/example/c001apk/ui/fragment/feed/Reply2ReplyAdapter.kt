package com.example.c001apk.ui.fragment.feed

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.ThemeUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
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
        val reply: TextView = view.findViewById(R.id.reply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_feed_content_reply_to_reply_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = reply2ReplyList.size

    @SuppressLint("SetTextI18n", "RestrictedApi", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reply = reply2ReplyList[position]

        /*val space = mContext.resources.getDimensionPixelSize(R.dimen.minor_space)
        if (position == 0)
            holder.reply.setPadding(space, space, space, space)
        else
            holder.reply.setPadding(space, 0, space, space)*/

        if (reply.ruid == uid) {
            val uCount = reply.username.length
            val text =
                """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>: ${reply.message}"""
            val mess = Html.fromHtml(
                text.replace("\n", "<br />"),
                Html.FROM_HTML_MODE_COMPACT
            )
            val builder = SpannableStringBuilder(mess)
            /* val foregroundColorSpan = ForegroundColorSpan(
                 ThemeUtils.getThemeAttrColor(
                     mContext,
                     com.google.android.material.R.attr.colorPrimary
                 )
             )*/
            //builder.setSpan(foregroundColorSpan, 0, uCount, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            val pattern = Pattern.compile("\\[[^\\]]+\\]")
            val matcher = pattern.matcher(builder)
            val urls = builder.getSpans(
                0, text.length,
                URLSpan::class.java
            )
            for (url in urls) {
                val myURLSpan = MyURLSpan(mContext, "", url.url)
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
        } else {
            val text =
                """<a class="feed-link-uname" href="/u/${reply.username}">${reply.username}</a>回复<a class="feed-link-uname" href="/u/${reply.rusername}">${reply.rusername}</a>: ${reply.message}"""
            val mess = Html.fromHtml(
                text.replace("\n", "<br />"),
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
                val myURLSpan = MyURLSpan(mContext, "", url.url)
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

    @SuppressLint("RestrictedApi")
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
    }

}