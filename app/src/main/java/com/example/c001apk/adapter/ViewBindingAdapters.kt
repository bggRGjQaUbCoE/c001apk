package com.example.c001apk.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Html
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.example.c001apk.R
import com.example.c001apk.logic.model.FeedArticleContentBean
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.NetWorkUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.util.dp
import com.example.c001apk.view.LinkTextView
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.google.android.material.color.MaterialColors
import com.google.android.material.imageview.ShapeableImageView

@BindingAdapter("setExtraPic")
fun setExtraPic(imageView: ImageView, extraPic: String?) {
    if (extraPic.isNullOrEmpty())
        imageView.apply {
            setBackgroundColor(
                MaterialColors.getColor(
                    imageView.context,
                    com.google.android.material.R.attr.colorPrimary,
                    0
                )
            )
            val link = imageView.context.getDrawable(R.drawable.ic_link)
            link?.setTint(
                MaterialColors.getColor(
                    imageView.context,
                    com.google.android.material.R.attr.colorOnPrimary,
                    0
                )
            )
            setImageDrawable(link)
        }
    else {
        ImageUtil.showIMG(imageView, extraPic)
    }
}

@BindingAdapter("setFollowText")
fun setFollowText(textView: TextView, followAuthor: Int) {
    with(PrefManager.isLogin) {
        textView.isVisible = this
        if (this) {
            when (followAuthor) {
                0 -> {
                    textView.text = "关注"
                    textView.setTextColor(
                        MaterialColors.getColor(
                            textView.context,
                            com.google.android.material.R.attr.colorPrimary,
                            0
                        )
                    )
                }

                1 -> {
                    textView.text = "取消关注"
                    textView.setTextColor(textView.context.getColor(android.R.color.darker_gray))
                }

                else -> {}
            }
        }
    }

}

@BindingAdapter("setArticleImage")
fun setArticleImage(
    imageView: NineGridImageView,
    setArticleImage: FeedArticleContentBean.Data,
) {
    setArticleImage.url?.let {
        val urlList = ArrayList<String>()
        urlList.add("$it.s.jpg")
        val imageLp = ImageUtil.getImageLp(it)
        imageView.imgWidth = imageLp.first
        imageView.imgHeight = imageLp.second
        imageView.isCompress = true
        imageView.setUrlList(urlList)
    }
}

@BindingAdapter(value = ["targetRow", "relationRows", "isFeedContent"], requireAll = true)
fun setRows(
    linearLayout: LinearLayout,
    targetRow: HomeFeedResponse.TargetRow?,
    relationRows: ArrayList<HomeFeedResponse.RelationRows>?,
    isFeedContent: Boolean?
) {
    linearLayout.removeAllViews()
    relationRows?.let {
        val dataList = it.toMutableList()
        targetRow?.id?.let {
            dataList.add(
                0,
                HomeFeedResponse.RelationRows(
                    targetRow.id,
                    targetRow.logo,
                    targetRow.title,
                    targetRow.url,
                    targetRow.targetType.toString()
                )
            )
        }
        val context = linearLayout.context
        dataList.forEachIndexed { index, relationRows ->
            val view = LayoutInflater.from(context).inflate(
                R.layout.item_home_icon_mini_scroll_card_item, linearLayout, false
            )
            if (isFeedContent == true)
                view.background = context.getDrawable(R.drawable.round_corners_20)
            if (index != 0) {
                view.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(5.dp, 0, 0, 0)
                }
            }
            view.findViewById<TextView>(R.id.title).text = relationRows.title
            ImageUtil.showIMG(
                view.findViewById<ShapeableImageView>(R.id.iconMiniScrollCard), relationRows.logo
            )

            view.setOnClickListener {
                NetWorkUtil.openLinkDyh(
                    relationRows.entityType,
                    context,
                    relationRows.url,
                    relationRows.id,
                    relationRows.title
                )
            }
            linearLayout.addView(view)
        }

    }
}

@BindingAdapter(value = ["pic", "picArr", "feedType"], requireAll = true)
fun setGridView(
    imageView: NineGridImageView,
    pic: String?,
    picArr: List<String>?,
    feedType: String?
) {
    if (!picArr.isNullOrEmpty()) {
        imageView.isVisible = true
        if (picArr.size == 1 || feedType in listOf("feedArticle", "trade")) {
            val imageLp = ImageUtil.getImageLp(pic ?: picArr[0])
            imageView.imgWidth = imageLp.first
            imageView.imgHeight = imageLp.second
        }
        imageView.apply {
            val urlList: MutableList<String> = ArrayList()
            if (feedType in listOf("feedArticle", "trade") && imgWidth > imgHeight)
                if (!pic.isNullOrEmpty()) urlList.add("$pic.s.jpg")
                else urlList.add("${picArr[0]}.s.jpg")
            else
                urlList.addAll(picArr.map { "$it.s.jpg" })
            setUrlList(urlList)
        }
    } else {
        imageView.isVisible = false
    }
}

@BindingAdapter("setLike")
fun setLike(textView: TextView, isLike: Int?) {
    isLike?.let {
        val color = if (it == 1)
            MaterialColors.getColor(
                textView.context,
                com.google.android.material.R.attr.colorPrimary,
                0
            )
        else textView.context.getColor(android.R.color.darker_gray)
        val size = textView.textSize.toInt()
        val drawableLike = textView.context.getDrawable(R.drawable.ic_like).also { drawable ->
            drawable?.setBounds(0, 0, size, size)
            drawable?.setTint(color)
        }
        textView.setCompoundDrawables(drawableLike, null, null, null)
        textView.setTextColor(color)
    }
}

@BindingAdapter(
    value = ["customText", "icon", "isHtml", "isRichText"], requireAll = false
)
fun setCustomText(
    textView: TextView,
    customText: String?,
    icon: Drawable?,
    isHtml: Boolean?,
    isRichText: Boolean?
) {

    icon?.let {
        val size = textView.textSize.toInt()
        icon.setBounds(0, 0, size, size)
        textView.setCompoundDrawables(icon, null, null, null)
    }

    textView.text =
        if (isHtml == true && !customText.isNullOrEmpty()) Html.fromHtml(
            customText,
            Html.FROM_HTML_MODE_COMPACT
        ) else if (isRichText == true && !customText.isNullOrEmpty()) {
            textView.movementMethod = LinkTextView.LocalLinkMovementMethod.instance
            SpannableStringBuilderUtil.setText(
                textView.context,
                customText,
                textView.textSize,
                null
            )
        } else customText

}

@BindingAdapter("setHotReply")
fun setHotReply(hotReply: TextView, replyRow: HomeFeedResponse.ReplyRows?) {
    if (replyRow != null) {
        hotReply.isVisible = true
        hotReply.highlightColor = Color.TRANSPARENT
        hotReply.movementMethod = LinkMovementMethodCompat.getInstance()
        hotReply.text = SpannableStringBuilderUtil.setText(
            hotReply.context,
            replyRow.message.toString(),
            hotReply.textSize,
            replyRow.picArr
        )
    } else
        hotReply.isVisible = false
}


@BindingAdapter("setImage")
fun setImage(imageView: ImageView, imageUrl: String?) {
    if (imageUrl.isNullOrEmpty())
        imageView.isVisible = false
    else {
        imageView.isVisible = true
        ImageUtil.showIMG(imageView, imageUrl)
    }
}

@BindingAdapter("setCover")
fun setCover(imageView: ImageView, imageUrl: String?) {
    imageUrl?.let {
        ImageUtil.showIMG(imageView, it, true)
    }
}