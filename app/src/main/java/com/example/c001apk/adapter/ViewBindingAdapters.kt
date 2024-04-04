package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.logic.model.FeedArticleContentBean
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.NetWorkUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.LinearAdapterLayout
import com.example.c001apk.view.LinkTextView
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.google.android.material.color.MaterialColors

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
                    android.R.attr.windowBackground,
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
    linearAdapterLayout: LinearAdapterLayout,
    targetRow: HomeFeedResponse.TargetRow?,
    relationRows: ArrayList<HomeFeedResponse.RelationRows>?,
    isFeedContent: Boolean?
) {
    relationRows?.let {
        val dataList = it.toMutableList()
        if (targetRow?.id != null) {
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

        linearAdapterLayout.adapter = object : BaseAdapter() {
            override fun getCount(): Int =
                if (targetRow?.id == null) it.size
                else 1 + it.size

            override fun getItem(p0: Int): Any = 0

            override fun getItemId(p0: Int): Long = 0

            @SuppressLint("ViewHolder")
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_home_icon_mini_scroll_card_item,
                    parent,
                    false
                )
                if (isFeedContent == true)
                    view.background = parent.context.getDrawable(R.drawable.round_corners_20)
                val logo: ImageView = view.findViewById(R.id.iconMiniScrollCard)
                val title: TextView = view.findViewById(R.id.title)
                if (position != 0) {
                    val layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(5.dp, 0, 0, 0)
                    view.layoutParams = layoutParams
                }
                title.text = dataList[position].title
                ImageUtil.showIMG(logo, dataList[position].logo)

                view.setOnClickListener {
                    NetWorkUtil.openLinkDyh(
                        dataList[position].entityType,
                        parent.context,
                        dataList[position].url,
                        dataList[position].id,
                        title.text.toString()
                    )
                }
                return view
            }
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
fun setHotReply(hotReply: TextView, feed: HomeFeedResponse.Data?) {
    if (feed != null) {
        if (!feed.replyRows.isNullOrEmpty()) {
            hotReply.isVisible = true
            hotReply.highlightColor = Color.TRANSPARENT
            val mess =
                if (feed.replyRows[0].picArr.isNullOrEmpty())
                    "<a class=\"feed-link-uname\" href=\"/u/${feed.replyRows[0].uid}\">${feed.replyRows[0].userInfo?.username}</a>: ${feed.replyRows[0].message}"
                else if (feed.replyRows[0].message == "[图片]")
                    "<a class=\"feed-link-uname\" href=\"/u/${feed.replyRows[0].uid}\">${feed.replyRows[0].userInfo?.username}</a>: ${feed.replyRows[0].message} <a class=\"feed-forward-pic\" href=${feed.replyRows[0].pic}>查看图片(${feed.replyRows[0].picArr?.size})</a>"
                else
                    "<a class=\"feed-link-uname\" href=\"/u/${feed.replyRows[0].uid}\">${feed.replyRows[0].userInfo?.username}</a>: ${feed.replyRows[0].message} [图片] <a class=\"feed-forward-pic\" href=${feed.replyRows[0].pic}>查看图片(${feed.replyRows[0].picArr?.size})</a>"
            hotReply.movementMethod = LinkMovementMethod.getInstance()
            hotReply.text = SpannableStringBuilderUtil.setText(
                hotReply.context,
                mess,
                hotReply.textSize,
                feed.replyRows[0].picArr
            )
            SpannableStringBuilderUtil.isReturn = true
        } else
            hotReply.isVisible = false
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