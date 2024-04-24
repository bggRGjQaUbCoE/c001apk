package com.example.c001apk.util

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import com.example.c001apk.view.CenteredImageSpan
import com.example.c001apk.view.MyURLSpan
import java.util.regex.Pattern

object SpannableStringBuilderUtil {

    fun setText(
        mContext: Context,
        text: String,
        size: Float,
        imgList: List<String>?,
        showMoreReply: (() -> Unit)? = null
    ): SpannableStringBuilder {
        val mess = Html.fromHtml(
            text.replace("\n", "<br/>"),
            Html.FROM_HTML_MODE_COMPACT
        )
        val builder = SpannableStringBuilder(mess)
        val urls = builder.getSpans(
            0, mess.length,
            URLSpan::class.java
        )
        urls.forEach {
            val myURLSpan = MyURLSpan(mContext, it.url, imgList, showMoreReply)
            val start = builder.getSpanStart(it)
            val end = builder.getSpanEnd(it)
            val flags = builder.getSpanFlags(it)
            builder.setSpan(myURLSpan, start, end, flags)
            builder.removeSpan(it)
        }
        if (PrefManager.showEmoji) {
            val pattern = Pattern.compile("\\[[^\\]]+\\]")
            val matcher = pattern.matcher(builder)
            while (matcher.find()) {
                val group = matcher.group()
                EmojiUtils.emojiMap[group]?.let {
                    mContext.getDrawable(it)?.let { emoji ->
                        if (group in listOf("[楼主]", "[层主]", "[置顶]"))
                            emoji.setBounds(0, 0, (size * 2).toInt(), size.toInt())
                        else
                            emoji.setBounds(0, 0, (size * 1.3).toInt(), (size * 1.3).toInt())
                        val imageSpan = CenteredImageSpan(emoji, (size * 1.3).toInt(), group)
                        builder.setSpan(
                            imageSpan,
                            matcher.start(),
                            matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                }
            }
        }
        return builder
    }

}