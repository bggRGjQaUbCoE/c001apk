package com.example.c001apk.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import com.example.c001apk.view.CenteredImageSpan
import com.example.c001apk.view.MyURLSpan
import java.util.regex.Pattern

object SpannableStringBuilderUtil {

    fun setEmoji(mContext: Context, text: String, size: Int): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)
        val pattern = Pattern.compile("\\[[^\\]]+\\]")
        val matcher = pattern.matcher(builder)
        while (matcher.find()) {
            val group = matcher.group()
            if (EmojiUtil.getEmoji(group) != -1) {
                val emoji: Drawable =
                    mContext.getDrawable(EmojiUtil.getEmoji(group))!!
                emoji.setBounds(
                    0,
                    0,
                    size,
                    size
                )
                val imageSpan = CenteredImageSpan(emoji, size)
                builder.setSpan(
                    imageSpan,
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return builder
    }

    fun setText(
        mContext: Context,
        text: String,
        size: Int,
        imgList: List<String>?
    ): SpannableStringBuilder {
        val mess = Html.fromHtml(
            StringBuilder(text).append(" ").toString()
                .replace("\n", " <br />"),
            Html.FROM_HTML_MODE_COMPACT
        )
        val builder = SpannableStringBuilder(mess)
        val pattern = Pattern.compile("\\[[^\\]]+\\]")
        val matcher = pattern.matcher(builder)
        val urls = builder.getSpans(
            0, mess.length,
            URLSpan::class.java
        )
        for (url in urls) {
            val myURLSpan = MyURLSpan(mContext, url.url, imgList)
            val start = builder.getSpanStart(url)
            val end = builder.getSpanEnd(url)
            val flags = builder.getSpanFlags(url)
            builder.setSpan(myURLSpan, start, end, flags)
            builder.removeSpan(url)
        }
        while (matcher.find()) {
            val group = matcher.group()
            if (EmojiUtil.getEmoji(group) != -1) {
                val emoji: Drawable =
                    mContext.getDrawable(EmojiUtil.getEmoji(group))!!
                emoji.setBounds(
                    0,
                    0,
                    size,
                    size
                )
                val imageSpan = CenteredImageSpan(emoji, size)
                builder.setSpan(
                    imageSpan,
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return builder
    }

}