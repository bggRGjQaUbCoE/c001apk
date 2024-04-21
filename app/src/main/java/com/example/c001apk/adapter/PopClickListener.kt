package com.example.c001apk.adapter

import android.content.Context
import android.view.MenuItem
import androidx.appcompat.widget.PopupMenu
import com.example.c001apk.R
import com.example.c001apk.ui.others.WebViewActivity
import com.example.c001apk.util.IntentUtil

class PopClickListener(
    val listener: ItemListener,
    val context: Context,
    val entityType: String,
    val id: String,
    val uid: String,
    val position: Int
) :
    PopupMenu.OnMenuItemClickListener {
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.block -> {
                listener.onBlockUser(id, uid, position)
            }

            R.id.report -> {
                IntentUtil.startActivity<WebViewActivity>(context) {
                    putExtra(
                        "url",
                        when (entityType) {
                            "feed" -> "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed&id=$id"
                            "feed_reply" -> "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed_reply&id=$id"
                            "user" -> "https://m.coolapk.com/mp/do?c=user&m=report&id=$uid"
                            else -> "error: entityType: $entityType, id: $id, uid: $uid"
                        }
                    )
                }
            }

            R.id.delete -> {
                listener.onDeleteClicked(entityType, id, position)
            }

            R.id.show -> {
                listener.showTotalReply(id, uid, position, null)
            }
        }
        return true
    }

}
