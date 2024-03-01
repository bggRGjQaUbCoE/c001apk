package com.example.c001apk.ui.others

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.c001apk.databinding.ActivityCopyBinding
import com.example.c001apk.logic.database.HomeMenuDatabase
import com.example.c001apk.logic.model.HomeMenu
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.home.HomeMenuAdapter
import com.example.c001apk.ui.home.ItemTouchHelperCallback
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern


class CopyActivity : BaseActivity<ActivityCopyBinding>() {

    private lateinit var mAdapter: HomeMenuAdapter
    private lateinit var mLayoutManager: FlexboxLayoutManager
    private var menuList: ArrayList<HomeMenu> = ArrayList()
    private val homeMenuDao by lazy {
        HomeMenuDatabase.getDatabase(this).homeMenuDao()
    }

    private fun getAllLinkAndText(str: String?): String {
        return if (TextUtils.isEmpty(str)) "" else
            Pattern.compile("<a class=\"feed-link-url\"\\s+href=\"([^<>\"]*)\"[^<]*[^>]*>")
                .matcher(str).replaceAll(" $1 ")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.getStringExtra("text")?.let {
            val linkText = getAllLinkAndText(it)
            binding.textView.text = Html.fromHtml(
                linkText.replace("\n", " <br/>"),
                Html.FROM_HTML_MODE_COMPACT
            ).toString()
            return
        }

        val type: String? = intent.getStringExtra("type")

        if (type != null && type == "homeMenu") {
            binding.done.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                menuList.addAll(homeMenuDao.loadAll())
                withContext(Dispatchers.Main) {
                    initView()
                }
            }
        }

        binding.done.setOnClickListener {
            var i = 0
            CoroutineScope(Dispatchers.IO).launch {
                homeMenuDao.deleteAll()
                for (element in menuList) {
                    homeMenuDao.insert(
                        HomeMenu(
                            i,
                            element.title,
                            element.isEnable
                        )
                    )
                    i++
                }
                withContext(Dispatchers.Main) {
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
        }

    }

    private fun initView() {
        mLayoutManager = FlexboxLayoutManager(this)
        mLayoutManager.flexDirection = FlexDirection.ROW
        mLayoutManager.flexWrap = FlexWrap.WRAP
        mAdapter = HomeMenuAdapter(menuList)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
        }
        val callback: ItemTouchHelper.Callback = ItemTouchHelperCallback(mAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

}