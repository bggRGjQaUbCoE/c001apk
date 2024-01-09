package com.example.c001apk.ui.activity

import android.content.Intent
import android.os.Bundle
import com.example.c001apk.util.NetWorkUtil.openLink
import rikka.material.app.MaterialActivity

class AppLinkActivity : MaterialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data

        if (data.toString().startsWith("coolmarket://feed/")) {
            val intent = Intent(this, FeedActivity::class.java)
            intent.putExtra("type", "feed")
            intent.putExtra("id", data.toString().replace("coolmarket://feed/", ""))
            intent.putExtra("uid", "")
            intent.putExtra("uname", "")
            startActivity(intent)
        } else {
            openLink(this, data.toString(), null)
        }

        finish()

    }

}