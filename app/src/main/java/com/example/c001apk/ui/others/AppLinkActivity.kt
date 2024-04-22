package com.example.c001apk.ui.others

import android.os.Bundle
import com.example.c001apk.util.NetWorkUtil.openLink
import rikka.material.app.MaterialActivity

class AppLinkActivity : MaterialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data

        openLink(this, data.toString(), null)

        finish()

    }

}