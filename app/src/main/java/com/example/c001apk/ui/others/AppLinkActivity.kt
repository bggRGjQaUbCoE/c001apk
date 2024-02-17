package com.example.c001apk.ui.others

import android.os.Bundle
import com.example.c001apk.ui.feed.FeedActivity
import com.example.c001apk.ui.user.UserActivity
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.NetWorkUtil.openLink
import rikka.material.app.MaterialActivity

class AppLinkActivity : MaterialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data

        if (data.toString().startsWith("coolmarket://feed/")) {
            IntentUtil.startActivity<FeedActivity>(this) {
                putExtra("id", data.toString().replace("coolmarket://feed/", ""))
            }
        } else if (data.toString().startsWith("coolmarket://u/")) {
            IntentUtil.startActivity<UserActivity>(this) {
                putExtra("id", data.toString().replace("coolmarket://u/", ""))
            }
        } else {
            openLink(this, data.toString(), null)
        }

        finish()

    }

}