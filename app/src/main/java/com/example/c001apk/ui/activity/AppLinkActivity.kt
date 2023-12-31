package com.example.c001apk.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast

class AppLinkActivity : BaseActivity() {

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

            val replace = data.toString()
                .replace("https://", "")
                .replace("http://", "")
                .replace("www.", "")
                .replace("coolapk1s", "coolapk")
                .replace("coolapk.com", "")

            if (replace.startsWith("/feed/")) {
                val intent = Intent(this, FeedActivity::class.java)
                intent.putExtra("type", "feed")
                intent.putExtra("id", replace.replace("/feed/", ""))
                intent.putExtra("uid", "")
                intent.putExtra("uname", "")
                startActivity(intent)
            } else if (replace.startsWith("/apk/") || replace.startsWith("/game/")) {
                val intent = Intent(this, AppActivity::class.java)
                intent.putExtra("id", replace.replace("/apk/", "").replace("/game/", ""))
                startActivity(intent)
            } else if (replace.startsWith("/u/")) {
                val intent = Intent(this, UserActivity::class.java)
                intent.putExtra("id", replace.replace("/u/", ""))
                startActivity(intent)
            } else if (replace.startsWith("/t/")) {
                val intent = Intent(this, TopicActivity::class.java)
                intent.putExtra("type", "topic")
                intent.putExtra("title", replace.replace("/t/", ""))
                intent.putExtra("url", replace.replace("/t/", ""))
                intent.putExtra("id", "")
                startActivity(intent)
            } else {
                Toast.makeText(this, "unsupported intent: ${data.toString()}", Toast.LENGTH_SHORT)
                    .show()
            }

        }

        finish()

    }

}