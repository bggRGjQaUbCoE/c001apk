package com.example.c001apk.ui.activity

import android.os.Bundle
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
        } else {
            openLink(this, data.toString(), null)
        }

        finish()

    }

}