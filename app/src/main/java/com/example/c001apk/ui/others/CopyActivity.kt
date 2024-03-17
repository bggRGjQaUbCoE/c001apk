package com.example.c001apk.ui.others

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.c001apk.databinding.ActivityCopyBinding
import com.example.c001apk.logic.model.HomeMenu
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.home.HomeMenuAdapter
import com.example.c001apk.ui.home.HomeViewModel
import com.example.c001apk.ui.home.ItemTouchHelperCallback
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern


@AndroidEntryPoint
class CopyActivity : BaseActivity<ActivityCopyBinding>() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var mAdapter: HomeMenuAdapter
    private lateinit var mLayoutManager: FlexboxLayoutManager
    private lateinit var menuList: ArrayList<HomeMenu>

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
            menuList = ArrayList()
            viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        }

        binding.done.setOnClickListener {
            viewModel.updateTab(menuList.mapIndexed { index, tab ->
                HomeMenu(
                    index,
                    tab.title,
                    tab.isEnable
                )
            })
        }

        viewModel.tabListLiveData.observe(this) {
            menuList.addAll(it)
            initView()
        }

        viewModel.restart.observe(this) {
            if (it) {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
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
        binding.done.visibility = View.VISIBLE
    }

}