package com.example.c001apk.ui.others

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityWebViewBinding
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.util.ClipboardUtil.copyText
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.http2https
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.net.URISyntaxException
import java.net.URLDecoder
import kotlin.system.exitProcess


class WebViewActivity : BaseActivity<ActivityWebViewBinding>() {

    private var link: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        link = intent.getStringExtra("url")
        link?.let {
            loadUrlInWebView(it.http2https)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadUrlInWebView(url: String) {
        binding.webView.settings.also {
            it.javaScriptEnabled = true
            it.domStorageEnabled = true
            it.setSupportZoom(true)
            it.builtInZoomControls = true
            it.displayZoomControls = false
            it.cacheMode = WebSettings.LOAD_NO_CACHE
            it.defaultTextEncodingName = "UTF-8"
            it.allowContentAccess = true
            it.useWideViewPort = true
            it.loadWithOverviewMode = true
            it.javaScriptCanOpenWindowsAutomatically = true
            it.loadsImagesAutomatically = true
            it.allowFileAccess = false
            it.userAgentString = PrefManager.USER_AGENT
            if (SDK_INT >= 32) {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(
                        it,
                        true
                    )
                }
            } else {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    val nightModeFlags =
                        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                        WebSettingsCompat.setForceDark(
                            it,
                            WebSettingsCompat.FORCE_DARK_ON
                        )

                    }
                }
            }
        }
        CookieManager.getInstance().apply {
            setAcceptThirdPartyCookies(binding.webView, false)
            removeAllCookies { }
            setCookie("m.coolapk.com", "DID=${PrefManager.SZLMID}")
            setCookie("m.coolapk.com", "forward=https://www.coolapk.com")
            setCookie("m.coolapk.com", "displayVersion=v14")
            setCookie("m.coolapk.com", "uid=${PrefManager.uid}")
            setCookie("m.coolapk.com", "username=${PrefManager.username}")
            setCookie("m.coolapk.com", "token=${PrefManager.token}")
        }
        binding.webView.apply {
            setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                val fileName = URLDecoder.decode(
                    URLUtil.guessFileName(url, contentDisposition, mimetype),
                    "UTF-8"
                )
                MaterialAlertDialogBuilder(this@WebViewActivity).apply {
                    setTitle("确定下载文件吗？")
                    setMessage(fileName)
                    setNeutralButton("外部打开") { _, _ ->
                        try {
                            this@WebViewActivity.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            )
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(this@WebViewActivity, "打开失败", Toast.LENGTH_SHORT)
                                .show()
                            copyText(this@WebViewActivity, url)
                            e.printStackTrace()
                        }
                    }
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        try {
                            val request = DownloadManager.Request(Uri.parse(url))
                                .setMimeType(mimetype)
                                .addRequestHeader(
                                    "cookie",
                                    CookieManager.getInstance().getCookie(url)
                                )
                                .addRequestHeader("User-Agent", userAgent)
                                .setTitle(fileName)
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    fileName
                                )
                            val downloadManager =
                                getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            downloadManager.enqueue(request)
                        } catch (e: Exception) {
                            Toast.makeText(this@WebViewActivity, "下载失败", Toast.LENGTH_SHORT)
                                .show()
                            copyText(this@WebViewActivity, url)
                            e.printStackTrace()
                        }
                    }
                    show()
                }
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    webView: WebView?, request: WebResourceRequest?
                ): Boolean {
                    request?.let {
                        try {
                            //处理intent协议
                            if (request.url.toString().startsWith("intent://")) {
                                val intent: Intent
                                try {
                                    intent = Intent.parseUri(
                                        request.url.toString(), Intent.URI_INTENT_SCHEME
                                    )
                                    intent.addCategory("android.intent.category.BROWSABLE")
                                    intent.component = null
                                    intent.selector = null
                                    val resolves =
                                        context.packageManager.queryIntentActivities(intent, 0)
                                    if (resolves.size > 0) {
                                        startActivityIfNeeded(intent, -1)
                                    }
                                    return true
                                } catch (e: URISyntaxException) {
                                    e.printStackTrace()
                                }
                            }
                            // 处理自定义scheme协议
                            if (!request.url.toString().startsWith("http")) {
                                webView?.let {
                                    Snackbar.make(
                                        it,
                                        "当前网页将要打开外部链接，是否打开",
                                        Snackbar.LENGTH_SHORT
                                    ).setAction("打开") {
                                        try {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(request.url.toString())
                                            )
                                            intent.flags =
                                                (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                this@WebViewActivity,
                                                "打开失败",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            e.printStackTrace()
                                        }
                                    }.show()
                                }
                                return true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    return super.shouldOverrideUrlLoading(webView, request)
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (newProgress == 100) {
                        binding.progressBar.isVisible = false
                    } else {
                        binding.progressBar.isVisible = true
                        binding.progressBar.progress = newProgress
                    }
                }

                override fun onReceivedTitle(view: WebView, title: String) {
                    super.onReceivedTitle(view, title)
                    binding.toolBar.title = title
                }
            }
            loadUrl(url, mutableMapOf("X-Requested-With" to "com.coolapk.market"))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.webview_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.refresh -> binding.webView.reload()

            R.id.copyLink -> {
                binding.webView.url?.let {
                    copyText(this, it.http2https)
                }
            }

            R.id.openInBrowser -> {
                binding.webView.url?.let {
                    val uri = Uri.parse(it.http2https)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show()
                        Log.w("error", "Activity was not found for intent, $intent")
                    }
                }
            }

            R.id.clearCache -> {
                binding.webView.clearHistory()
                binding.webView.clearCache(true)
                binding.webView.clearFormData()
                Toast.makeText(this, "清除缓存成功", Toast.LENGTH_SHORT).show()
            }

        }
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
            binding.webView.goBack() //返回上个页面
            return true
        }
        return super.onKeyDown(keyCode, event) //退出H5界面
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onDestroy() {
        try {
            binding.webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            binding.webView.loadUrl("about:blank")
            binding.webView.parent?.let {
                (it as ViewGroup).removeView(binding.webView)
            }
            binding.webView.stopLoading()
            binding.webView.settings.javaScriptEnabled = false
            binding.webView.clearHistory()
            binding.webView.clearCache(true)
            binding.webView.removeAllViewsInLayout()
            binding.webView.removeAllViews()
            binding.webView.setOnTouchListener(null)
            binding.webView.setOnKeyListener(null)
            binding.webView.onFocusChangeListener = null
            binding.webView.webChromeClient = null
            binding.webView.onPause()
            binding.webView.removeAllViews()
            binding.webView.destroy()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        super.onDestroy()
        exitProcess(0)
    }

}