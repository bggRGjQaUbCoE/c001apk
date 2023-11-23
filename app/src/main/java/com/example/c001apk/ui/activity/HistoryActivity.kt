package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.c001apk.R
import com.example.c001apk.adapter.BHistoryAdapter
import com.example.c001apk.databinding.ActivityHistoryBinding
import com.example.c001apk.logic.database.BHistoryDataBaseHelper
import com.example.c001apk.logic.model.BHistoryBean
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var dbHelper: BHistoryDataBaseHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var mAdapter: BHistoryAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = BHistoryDataBaseHelper(this, "BHistory.db", 1)
        db = dbHelper.writableDatabase

        initBar()
        initView()
        if (viewModel.bHistoryList.isEmpty()) {
            binding.indicator.visibility = View.VISIBLE
            binding.indicator.isIndeterminate = true
            queryData()
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
                    setTitle("确定清除全部浏览历史？")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        db.delete("BHistory", "", arrayOf())
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
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mLayoutManager = LinearLayoutManager(this)
        mAdapter = BHistoryAdapter(this, viewModel.bHistoryList)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    @SuppressLint("NotifyDataSetChanged", "Range")
    private fun queryData() {
        val cursor = db.query("BHistory", null, null, null, null, null, null)
        viewModel.bHistoryList.clear()
        if (cursor.moveToLast()) {
            do {
                val fid = cursor.getString(cursor.getColumnIndex("fid"))
                val uid = cursor.getString(cursor.getColumnIndex("uid"))
                val uname = cursor.getString(cursor.getColumnIndex("uname"))
                val avatar = cursor.getString(cursor.getColumnIndex("avatar"))
                val device = cursor.getString(cursor.getColumnIndex("device"))
                val message = cursor.getString(cursor.getColumnIndex("message"))
                val pubDate = cursor.getString(cursor.getColumnIndex("pubDate"))
                if (!BlackListUtil.checkUid(uid))
                    viewModel.bHistoryList.add(
                        BHistoryBean(
                            fid,
                            uid,
                            uname,
                            avatar,
                            device,
                            message,
                            pubDate
                        )
                    )
            } while (cursor.moveToPrevious())
            cursor.close()
        }
        binding.indicator.visibility = View.GONE
        binding.indicator.isIndeterminate = false
        mAdapter.notifyDataSetChanged()
    }

}