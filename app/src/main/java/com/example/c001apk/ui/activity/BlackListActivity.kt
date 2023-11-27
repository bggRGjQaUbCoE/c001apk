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
import com.example.c001apk.R
import com.example.c001apk.adapter.HistoryAdapter
import com.example.c001apk.databinding.ActivityBlackListBinding
import com.example.c001apk.logic.database.BlackListDatabase
import com.example.c001apk.logic.database.TopicBlackListDatabase
import com.example.c001apk.logic.model.SearchHistory
import com.example.c001apk.ui.fragment.minterface.IOnItemClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlackListActivity : BaseActivity(), IOnItemClickListener {

    private lateinit var binding: ActivityBlackListBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: HistoryAdapter
    private lateinit var mLayoutManager: FlexboxLayoutManager
    private val blackListDao by lazy {
        BlackListDatabase.getDatabase(this@BlackListActivity).blackListDao()
    }
    private val topicBlackListDao by lazy {
        TopicBlackListDatabase.getDatabase(this@BlackListActivity).blackListDao()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlackListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.type = intent.getStringExtra("type").toString()

        initView()
        if (viewModel.historyList.isEmpty()) {
            when (viewModel.type) {
                "user" -> viewModel.getBlackList("blacklist", this)
                "topic" -> viewModel.getBlackList("topicBlacklist", this)
            }
        }

        initButton()
        initEditText()
        initEdit()
        initClearHistory()

        viewModel.blackListLiveData.observe(this) {
            viewModel.historyList.clear()
            viewModel.historyList.addAll(it)
            if (viewModel.historyList.isEmpty())
                binding.clearAll.visibility = View.GONE
            else
                binding.clearAll.visibility = View.VISIBLE
            mAdapter.notifyDataSetChanged()
        }

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
                    CoroutineScope(Dispatchers.IO).launch {
                        when (viewModel.type) {
                            "user" -> blackListDao.deleteAll()
                            "topic" -> topicBlackListDao.deleteAll()
                        }
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

    @SuppressLint("RestrictedApi")
    private fun initEditText() {
        binding.title.text = when (viewModel.type) {
            "user" -> this.getString(R.string.user_black_list)
            "topic" -> this.getString(R.string.topic_black_list)
            else -> ""
        }
        binding.editText.highlightColor = ColorUtils.setAlphaComponent(
            ThemeUtils.getThemeAttrColor(
                this,
                rikka.preference.simplemenu.R.attr.colorPrimaryDark
            ), 128
        )
        binding.editText.hint = when (viewModel.type) {
            "user" -> "uid"
            "topic" -> "话题"
            else -> ""
        }
        binding.editText.isFocusable = true
        binding.editText.isFocusableInTouchMode = true
        binding.editText.requestFocus()
        val imm =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.editText, 0)
        binding.editText.imeOptions = EditorInfo.IME_ACTION_SEARCH
        binding.editText.inputType = when (viewModel.type) {
            "user" -> EditorInfo.TYPE_CLASS_NUMBER
            "topic" -> EditorInfo.TYPE_CLASS_TEXT
            else -> EditorInfo.TYPE_CLASS_TEXT
        }
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
        CoroutineScope(Dispatchers.IO).launch {
            when (viewModel.type) {
                "user" -> {
                    if (blackListDao.isExist(uid)) {
                        viewModel.historyList.remove(uid)
                        blackListDao.delete(uid)
                    }
                }

                "topic" -> {
                    if (topicBlackListDao.isExist(uid)) {
                        viewModel.historyList.remove(uid)
                        topicBlackListDao.delete(uid)
                    }
                }
            }
            viewModel.historyList.add(0, uid)
            when (viewModel.type) {
                "user" -> blackListDao.insert(SearchHistory(uid))
                "topic" -> topicBlackListDao.insert(SearchHistory(uid))
            }
            withContext(Dispatchers.Main) {
                mAdapter.notifyDataSetChanged()
                if (binding.clearAll.visibility != View.VISIBLE)
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
        CoroutineScope(Dispatchers.IO).launch {
            when (viewModel.type) {
                "user" -> blackListDao.delete(keyword)
                "topic" -> topicBlackListDao.delete(keyword)
            }
        }
        viewModel.historyList.remove(keyword)
        mAdapter.notifyDataSetChanged()
    }


}