package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.BHistoryAdapter
import com.example.c001apk.databinding.ActivityHistoryBinding
import com.example.c001apk.logic.database.BrowseHistoryDatabase
import com.example.c001apk.logic.database.FeedFavoriteDatabase
import com.example.c001apk.logic.model.BrowseHistory
import com.example.c001apk.logic.model.FeedFavorite
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryActivity : BaseActivity<ActivityHistoryBinding>() {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: BHistoryAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private val browseHistoryDao by lazy {
        BrowseHistoryDatabase.getDatabase(this).browseHistoryDao()
    }
    private val feedFavoriteDao by lazy {
        FeedFavoriteDatabase.getDatabase(this).feedFavoriteDao()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.type = intent.getStringExtra("type")

        binding.toolBar.title =
            when (viewModel.type) {
                "browse" -> "浏览历史"
                "favorite" -> "本地收藏"
                else -> throw IllegalArgumentException("error type: ${viewModel.type}")
            }

        initBar()
        initView()
        if (viewModel.bHistoryList.isEmpty()) {
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            viewModel.getBrowseList(viewModel.type.toString(), this)
        }

        viewModel.browseLiveData.observe(this) {
            viewModel.bHistoryList.clear()
            for (element in it) {
                if (viewModel.type == "browse") {
                    if (!BlackListUtil.checkUid((element as BrowseHistory).uid))
                        viewModel.bHistoryList.add(element)
                } else {
                    if (!BlackListUtil.checkUid((element as FeedFavorite).uid))
                        viewModel.bHistoryList.add(element)
                }
            }
            mAdapter.setDataListData(viewModel.type.toString(), viewModel.bHistoryList)
            binding.indicator.parent.visibility = View.GONE
            binding.indicator.parent.isIndeterminate = false
            mAdapter.notifyDataSetChanged()
        }

    }

    private fun initBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.clearAll -> {
                MaterialAlertDialogBuilder(this).apply {
                    if (viewModel.type == "browse") setTitle("确定清除全部浏览历史？")
                    else setTitle("确定清除全部收藏？")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            if (viewModel.type == "browse") browseHistoryDao.deleteAll()
                            else feedFavoriteDao.deleteAll()
                        }
                        viewModel.bHistoryList.clear()
                        mAdapter.notifyDataSetChanged()
                    }
                    show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        mLayoutManager = LinearLayoutManager(this)
        mAdapter = when (viewModel.type) {
            "browse" -> BHistoryAdapter(this)
            "favorite" -> BHistoryAdapter(this)
            else -> throw IllegalArgumentException("error type: ${viewModel.type}")
        }

        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(10.dp))
        }
    }

}