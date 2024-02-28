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
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.setPadding
import androidx.databinding.BindingAdapter
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.logic.model.FeedArticleContentBean
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.NetWorkUtil
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.util.Utils.getColorFromAttr
import com.example.c001apk.view.LinearAdapterLayout
import com.example.c001apk.view.LinkTextView
import com.example.c001apk.view.ninegridimageview.NineGridImageView

@BindingAdapter("setExtraPic")
fun setExtraPic(imageView: ImageView, extraPic: String?) {
    if (extraPic.isNullOrEmpty())
        imageView.apply {
            setBackgroundColor(
                imageView.context.getColorFromAttr(rikka.preference.simplemenu.R.attr.colorPrimary)
            )
            val link = imageView.context.getDrawable(R.drawable.ic_link)
            link!!.setTint(imageView.context.getColorFromAttr(android.R.attr.windowBackground))
            setImageDrawable(link)
        }
    else {
        ImageUtil.showIMG(imageView, extraPic)
    }
}

@BindingAdapter("setFollowText")
fun setFollowText(textView: TextView, userAction: HomeFeedResponse.UserAction?) {
    if (userAction == null)
        textView.visibility = View.GONE
    else {
        textView.visibility = View.VISIBLE
        if (userAction.followAuthor == 0) {
            textView.text = "关注"
            textView.setTextColor(textView.context.getColorFromAttr(rikka.preference.simplemenu.R.attr.colorPrimary))
        } else if (userAction.followAuthor == 1) {
            textView.text = "取消关注"
            textView.setTextColor(textView.context.getColor(android.R.color.darker_gray))
        }
    }
}

@BindingAdapter("setArticleImage")
fun setArticleImage(
    imageView: NineGridImageView,
    setArticleImage: FeedArticleContentBean.Data,
) {
    val urlList = ArrayList<String>()
    urlList.add("${setArticleImage.url}.s.jpg")
    val imageLp = ImageUtil.getImageLp(setArticleImage.url!!)
    imageView.imgWidth = imageLp.first
    imageView.imgHeight = imageLp.second
    imageView.isCompress = true
    imageView.setUrlList(urlList)
}

@BindingAdapter(value = ["targetRow", "relationRows", "isFeedContent"], requireAll = true)
fun setRows(
    linearAdapterLayout: LinearAdapterLayout,
    targetRow: HomeFeedResponse.TargetRow?,
    relationRows: ArrayList<HomeFeedResponse.RelationRows>?,
    isFeedContent: Boolean?
) {
    relationRows?.let {
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
                    view.background = view.context.getDrawable(R.drawable.round_corners_20)
                val logo: ImageView = view.findViewById(R.id.iconMiniScrollCard)
                val title: TextView = view.findViewById(R.id.title)
                val type: String
                val id: String
                val url: String
                if (targetRow?.id != null) {
                    if (position == 0) {
                        type = targetRow.targetType.toString()
                        id = targetRow.id
                        url = targetRow.url
                        title.text = targetRow.title
                        ImageUtil.showIMG(logo, targetRow.logo)
                    } else {
                        val layoutParams = ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                        )
                        layoutParams.setMargins(5.dp, 0, 0, 0)
                        view.layoutParams = layoutParams
                        type = it[position - 1].entityType
                        id = it[position - 1].id
                        url = it[position - 1].url
                        title.text = it[position - 1].title
                        ImageUtil.showIMG(
                            logo,
                            it[position - 1].logo
                        )
                    }
                } else {
                    if (position == 0) {
                        type = it[0].entityType
                        id = it[0].id
                        title.text = it[0].title
                        url = it[0].url
                        ImageUtil.showIMG(logo, it[0].logo)
                    } else {
                        val layoutParams = ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                        )
                        layoutParams.setMargins(5.dp, 0, 0, 0)
                        view.layoutParams = layoutParams
                        type = it[position].entityType
                        id = it[position].id
                        url = it[position].url
                        title.text = it[position].title
                        ImageUtil.showIMG(
                            logo,
                            it[position].logo
                        )
                    }
                }
                view.setOnClickListener {
                    NetWorkUtil.openLinkDyh(type, view.context, url, id, title.text.toString())
                }
                return view
            }
        }
    }
}

@BindingAdapter(value = ["picArr", "feedType"], requireAll = true)
fun setGridView(
    imageView: NineGridImageView,
    picArr: List<String>?,
    feedType: String?
) {
    if (!picArr.isNullOrEmpty()) {
        imageView.visibility = View.VISIBLE
        if (picArr.size == 1 || feedType == "feedArticle") {
            val imageLp = ImageUtil.getImageLp(picArr[0])
            imageView.imgWidth = imageLp.first
            imageView.imgHeight = imageLp.second
        }
        imageView.apply {
            val urlList: MutableList<String> = ArrayList()
            if (feedType == "feedArticle" && imgWidth > imgHeight)
                urlList.add("${picArr[0]}.s.jpg")
            else
                picArr.forEach {
                    urlList.add("$it.s.jpg")
                }
            setUrlList(urlList)
        }
    } else {
        imageView.visibility = View.GONE
    }
}

@BindingAdapter("setLike")
fun setLike(textView: TextView, isLike: Int?) {
    isLike?.let {
        val color = if (it == 1)
            textView.context.getColorFromAttr(rikka.preference.simplemenu.R.attr.colorPrimary)
        else textView.context.getColor(android.R.color.darker_gray)
        val size = textView.textSize.toInt()
        val drawableLike = textView.context.getDrawable(R.drawable.ic_like)!!
        drawableLike.setBounds(0, 0, size, size)
        DrawableCompat.setTint(drawableLike, color)
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
            if (BlackListUtil.checkUid(feed.replyRows[0].uid)) {
                hotReply.visibility = View.GONE
                return
            }
            hotReply.visibility = View.VISIBLE
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
            hotReply.visibility = View.GONE
    } else
        hotReply.visibility = View.GONE
}


@BindingAdapter("setImage")
fun setImage(imageView: ImageView, imageUrl: String?) {
    if (imageUrl.isNullOrEmpty())
        imageView.visibility = View.GONE
    else {
        imageView.visibility = View.VISIBLE
        ImageUtil.showIMG(imageView, imageUrl)
    }
}

@BindingAdapter("setCover")
fun setCover(imageView: ImageView, imageUrl: String?) {
    imageUrl?.let {
        ImageUtil.showUserCover(imageView, it)
    }
}