package com.example.c001apk.adapter

import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.c001apk.ui.app.AppActivity
import com.example.c001apk.ui.coolpic.CoolPicActivity
import com.example.c001apk.ui.feed.FeedActivity
import com.example.c001apk.ui.follow.FFFListActivity
import com.example.c001apk.ui.others.CopyActivity
import com.example.c001apk.ui.others.WebViewActivity
import com.example.c001apk.ui.topic.TopicActivity
import com.example.c001apk.ui.user.UserActivity
import com.example.c001apk.util.ClipboardUtil.copyText
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.NetWorkUtil.openLink
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

interface ItemListener {

    fun onFollowUser(uid: String, followAuthor: Int) {}

    fun onShowCollection(id: String, title: String) {}

    fun onViewApk(view: View, id: String?) {
        id?.let {
            IntentUtil.startActivity<AppActivity>(view.context) {
                putExtra("id", id)
            }
        }
    }

    fun onMessLongClicked(uname: String, id: String, position: Int): Boolean {
        return true
    }

    fun onMessClicked(view: View, note: String) {
        val doc: Document = Jsoup.parse(note)
        val links: Elements = doc.select("a[href]")
        links.forEach { link ->
            val href = link.attr("href")
            if (href.contains("/feed/")) {
                val id: String
                var rid: String? = null
                val index0 = href.replace("/feed/", "").indexOf('?')
                val index1 = href.indexOf("rid=")
                val index2 = href.indexOf('&')
                if (index0 != -1 && index1 != -1 && index2 != -1) {
                    id = href.replace("/feed/", "").substring(0, index0)
                    rid = href.substring(index1 + 4, index2)
                } else if (index0 != -1 && index1 != -1 && index2 == -1){
                    id = href.replace("/feed/", "").substring(0, index0)
                    rid = href.substring(index1 + 4)
                } else id = href
                IntentUtil.startActivity<FeedActivity>(view.context) {
                    putExtra("viewReply", true)
                    putExtra("id", id)
                    putExtra("rid", rid)
                }
            } else if (href.contains("http")) {
                IntentUtil.startActivity<WebViewActivity>(view.context) {
                    putExtra("url", href)
                }
            } else if (href.isNullOrEmpty()) {
                return
            } else {
                Toast.makeText(view.context, "unknown type", Toast.LENGTH_SHORT)
                    .show()
                copyText(view.context, href)
            }
        }
    }

    fun showTotalReply(id: String, uid: String, position: Int, rPosition: Int?) {}

    fun viewFFFList(view: View, uid: String?, isEnable: Boolean, type: String) {
        uid?.let {
            IntentUtil.startActivity<FFFListActivity>(view.context) {
                putExtra("uid", uid)
                putExtra("isEnable", isEnable)
                putExtra("type", type)
            }
        }
    }

    fun loadImage(view: View, imageUrl: String?) {
        imageUrl?.let {
            ImageUtil.startBigImgViewSimple(view as ImageView, it)
        }
    }

    fun onViewCoolPic(view: View, title: String?) {
        IntentUtil.startActivity<CoolPicActivity>(view.context) {
            putExtra("title", title?.replace("#", ""))
        }
    }

    fun onViewTopic(view: View, type: String?, title: String?, url: String?, id: String?) {
        IntentUtil.startActivity<TopicActivity>(view.context) {
            putExtra("type", type)
            putExtra("title", title)
            putExtra("url", url)
            putExtra("id", id)
        }
    }

    fun onOpenLink(view: View, url: String?, title: String?) {
        url?.let {
            openLink(view.context, it, title)
        }
    }

    fun onViewFeed(
        view: View, id: String?, uid: String?, username: String?, userAvatar: String?,
        deviceTitle: String?, message: String?, dateline: String?, rid: Any?, isViewReply: Any?
    ) {
        IntentUtil.startActivity<FeedActivity>(view.context) {
            putExtra("id", id)
            rid?.let {
                putExtra("rid", rid as String)
            }
            isViewReply?.let {
                putExtra("viewReply", it as Boolean)
            }
        }
    }

    fun onViewUser(view: View, uid: String?) {
        IntentUtil.startActivity<UserActivity>(view.context) {
            putExtra("id", uid)
        }
    }

    fun onCopyText(view: View, text: String?): Boolean {
        IntentUtil.startActivity<CopyActivity>(view.context) {
            putExtra("text", text)
        }
        return true
    }

    fun onCopyToClip(view: View, text: String?) {
        text?.let {
            copyText(view.context, it)
        }
    }

    fun onExpand(
        view: View, id: String, uid: String,
        text: String?, position: Int, rPosition: Int?
    ) {
    }

    fun onLikeClick(type: String, id: String, isLike: Int) {}

    fun onReply(id: String, uid: String, username: String?, position: Int, rPosition: Int?) {}

    fun onBlockUser(id: String, uid: String, position: Int) {}

    fun onDeleteClicked(entityType: String, id: String, position: Int) {}
}