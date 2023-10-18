package com.example.c001apk.ui.activity.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityWebViewBinding


class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val url = intent.getStringExtra("url")
        url?.let {
            binding.webView.apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(view: WebView, title: String) {
                        super.onReceivedTitle(view, title)
                        binding.toolBar.title = title
                    }
                }
                loadUrl(url)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.webview_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.refresh -> Toast.makeText(this, "refresh", Toast.LENGTH_SHORT).show()
            R.id.copyLink -> Toast.makeText(this, "copy", Toast.LENGTH_SHORT).show()
            R.id.openInBrowser -> Toast.makeText(this, "open", Toast.LENGTH_SHORT).show()
        }
        return true
    }


}