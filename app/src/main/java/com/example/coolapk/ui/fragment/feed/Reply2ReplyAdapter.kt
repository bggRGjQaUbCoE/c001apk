package com.example.coolapk.ui.fragment.feed

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.ThemeUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.coolapk.R
import com.example.coolapk.logic.model.HomeFeedResponse


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
            LayoutInflater.from(parent.context).inflate(R.layout.item_feed_content_reply_to_reply_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = reply2ReplyList.size

    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reply = reply2ReplyList[position]

        if (reply.ruid == uid) {
            val uCount = reply.username.length
            val text = "${reply.username}: ${reply.message}"
            val builder =
                SpannableStringBuilder(Html.fromHtml(text.replace("\n", "<br />"), Html.FROM_HTML_MODE_COMPACT))
            val foregroundColorSpan = ForegroundColorSpan(
                ThemeUtils.getThemeAttrColor(
                    mContext,
                    com.google.android.material.R.attr.colorPrimary
                )
            )
            builder.setSpan(foregroundColorSpan, 0, uCount, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.reply.text = builder
        } else {
            val uCount = reply.username.length
            val urCount = reply.rusername.length
            val text = "${reply.username}回复${reply.rusername}: ${reply.message}"
            val builder =
                SpannableStringBuilder(Html.fromHtml(text.replace("\n", "<br />"), Html.FROM_HTML_MODE_COMPACT))
            val foregroundColorSpan = ForegroundColorSpan(
                ThemeUtils.getThemeAttrColor(
                    mContext,
                    com.google.android.material.R.attr.colorPrimary
                )
            )
            val foregroundColorSpan1 = ForegroundColorSpan(
                ThemeUtils.getThemeAttrColor(
                    mContext,
                    com.google.android.material.R.attr.colorPrimary
                )
            )
            builder.setSpan(foregroundColorSpan, 0, uCount, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(
                foregroundColorSpan1,
                uCount + 2,
                uCount + urCount + 2,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            holder.reply.text = builder
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