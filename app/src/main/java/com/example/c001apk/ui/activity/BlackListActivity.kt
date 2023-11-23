package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.ThemeUtils
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModelProvider
import com.example.c001apk.adapter.HistoryAdapter
import com.example.c001apk.databinding.ActivityBlackListBinding
import com.example.c001apk.logic.database.BlackListDatabase
import com.example.c001apk.logic.model.SearchHistory
import com.example.c001apk.ui.fragment.minterface.IOnItemClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.concurrent.thread

class BlackListActivity : BaseActivity(), IOnItemClickListener {

    private lateinit var binding: ActivityBlackListBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: HistoryAdapter
    private lateinit var mLayoutManager: FlexboxLayoutManager
    private val blackListDao by lazy {
        BlackListDatabase.getDatabase(this@BlackListActivity).blackListDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlackListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        if (viewModel.historyList.isEmpty())
            queryData()

        initButton()
        initEditText()
        initEdit()
        initClearHistory()

    }

    private fun initButton() {
        binding.search.setOnClickListener {
            checkUid()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initClearHistory() {
        binding.clearAll.setOnClickListener {
            MaterialAlertDialogBuilder(this).apply {
                setTitle("确定清除全部黑名单？")
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    thread {
                        blackListDao.deleteAll()
                    }
                    viewModel.historyList.clear()
                    mAdapter.notifyDataSetChanged()
                    binding.clearAll.visibility = View.GONE
                }
                show()
            }
        }
    }

    private fun initView() {
        mLayoutManager = FlexboxLayoutManager(this)
        mLayoutManager.flexDirection = FlexDirection.ROW
        mLayoutManager.flexWrap = FlexWrap.WRAP
        mAdapter = HistoryAdapter(viewModel.historyList)
        mAdapter.setOnItemClickListener(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
        }
    }

    @SuppressLint("NotifyDataSetChanged", "Range")
    private fun queryData() {
        viewModel.historyList.clear()
        thread {
            for (element in blackListDao.loadAllList()) {
                viewModel.historyList.add(element.keyWord)
            }
            if (viewModel.historyList.isEmpty())
                binding.clearAll.visibility = View.GONE
            else
                binding.clearAll.visibility = View.VISIBLE
            runOnUiThread {
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun initEditText() {
        binding.editText.highlightColor = ColorUtils.setAlphaComponent(
            ThemeUtils.getThemeAttrColor(
                this,
                rikka.preference.simplemenu.R.attr.colorPrimaryDark
            ), 128
        )
        binding.editText.isFocusable = true
        binding.editText.isFocusableInTouchMode = true
        binding.editText.requestFocus()
        val imm =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.editText, 0)
        binding.editText.imeOptions = EditorInfo.IME_ACTION_SEARCH
        binding.editText.inputType = EditorInfo.TYPE_CLASS_NUMBER
    }

    private fun initEdit() {
        binding.editText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, keyEvent ->
            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEARCH) && keyEvent != null) {
                checkUid()
                return@OnEditorActionListener true
            }
            false
        })
    }


    private fun checkUid() {
        if (binding.editText.text.toString() == "") {
            return
            //Toast.makeText(this, "uid不能为空", Toast.LENGTH_SHORT).show()
            //hideKeyBoard()
        } else {
            updateUid(binding.editText.text.toString())
            binding.editText.text = null
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUid(uid: String) {
        thread {
            if (blackListDao.isExist(uid)) {
                viewModel.historyList.remove(uid)
                blackListDao.delete(uid)
            }
            viewModel.historyList.add(0, uid)
            blackListDao.insert(SearchHistory(uid))
            runOnUiThread {
                mAdapter.notifyDataSetChanged()
                binding.clearAll.visibility = View.VISIBLE
            }
        }
    }

    override fun onItemClick(keyword: String) {
        val intent = Intent(this, UserActivity::class.java)
        intent.putExtra("id", keyword)
        this.startActivity(intent)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemDeleteClick(keyword: String) {
        thread {
            blackListDao.delete(keyword)
        }
        viewModel.historyList.remove(keyword)
        mAdapter.notifyDataSetChanged()
    }


}