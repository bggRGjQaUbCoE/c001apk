package com.example.c001apk.ui.history

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.databinding.ActivityHistoryBinding
import com.example.c001apk.logic.database.BrowseHistoryDatabase
import com.example.c001apk.logic.database.FeedFavoriteDatabase
import com.example.c001apk.logic.model.BrowseHistory
import com.example.c001apk.logic.model.FeedFavorite
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryActivity : BaseActivity<ActivityHistoryBinding>() {

    private val viewModel by lazy { ViewModelProvider(this)[HistoryViewModel::class.java] }
    private lateinit var mAdapter: HistoryAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private val browseHistoryDao by lazy {
        BrowseHistoryDatabase.getDatabase(this).browseHistoryDao()
    }
    private val feedFavoriteDao by lazy {
        FeedFavoriteDatabase.getDatabase(this).feedFavoriteDao()
    }

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
        if (viewModel.listSize == -1) {
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            viewModel.getBrowseList(viewModel.type.toString(), this)
        }

        viewModel.browseLiveData.observe(this) { list ->
            if (list.isNullOrEmpty())
                mAdapter.setDataListData(viewModel.type.toString(), emptyList())
            else {
                val bHistoryList: MutableList<Any> = ArrayList()
                for (element in list) {
                    if (viewModel.type == "browse") {
                        if (!BlackListUtil.checkUid((element as BrowseHistory).uid))
                            bHistoryList.add(element)
                    } else {
                        if (!BlackListUtil.checkUid((element as FeedFavorite).uid))
                            bHistoryList.add(element)
                    }
                }
                mAdapter.setDataListData(viewModel.type.toString(), bHistoryList)
            }
            binding.indicator.parent.visibility = View.GONE
            binding.indicator.parent.isIndeterminate = false
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
                        viewModel.browseLiveData.postValue(emptyList())
                    }
                    show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        mAdapter = HistoryAdapter(viewModel.ItemClickListener())
        binding.recyclerView.apply {
            adapter = ConcatAdapter(HeaderAdapter(), mAdapter)
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mLayoutManager = LinearLayoutManager(this@HistoryActivity)
                    mLayoutManager
                } else {
                    sLayoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    sLayoutManager
                }
            if (itemDecorationCount == 0) {
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    addItemDecoration(LinearItemDecoration(10.dp))
                else
                    addItemDecoration(StaggerItemDecoration(10.dp))
            }
        }
    }

}