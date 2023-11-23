package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
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
import com.example.c001apk.logic.database.BlackListDataBaseHelper
import com.example.c001apk.ui.fragment.minterface.IOnItemClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BlackListActivity : BaseActivity(), IOnItemClickListener {

    private lateinit var binding: ActivityBlackListBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var dbHelper: BlackListDataBaseHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var mAdapter: HistoryAdapter
    private lateinit var mLayoutManager: FlexboxLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlackListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = BlackListDataBaseHelper(this, "BlackList.db", 1)
        db = dbHelper.writableDatabase

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
                    db.delete("BlackList", "", arrayOf())
                    viewModel.historyList.clear()
                    mAdapter.notifyDataSetChanged()
                    //binding.historyLayout.visibility = View.GONE
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
        val cursor = db.query("BlackList", null, null, null, null, null, null)
        viewModel.historyList.clear()
        if (cursor.moveToLast()) {
            do {
                val history = cursor.getString(cursor.getColumnIndex("uid"))
                viewModel.historyList.add(history)
                if (viewModel.historyList.isEmpty())
                    binding.historyLayout.visibility = View.GONE
                else
                    binding.historyLayout.visibility = View.VISIBLE
            } while (cursor.moveToPrevious())
            cursor.close()
        }
        mAdapter.notifyDataSetChanged()
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
            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEARCH ) && keyEvent != null) {
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

    @SuppressLint("Range", "NotifyDataSetChanged")
    private fun saveUid(uid: String) {
        var isExist = false
        val cursor = db.query("BlackList", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val history = cursor.getString(cursor.getColumnIndex("uid"))
                if (uid == history)
                    isExist = true
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (!isExist) {
            viewModel.historyList.add(0, uid)
            mAdapter.notifyDataSetChanged()
            val value = ContentValues().apply {
                put("uid", uid)
            }
            db.insert("BlackList", null, value)
        }
    }

    private fun updateUid(uid: String) {
        viewModel.historyList.remove(uid)
        db.delete("BlackList", "uid = ?", arrayOf(uid))
        saveUid(uid)
    }

    override fun onItemClick(keyword: String) {
        val intent = Intent(this, UserActivity::class.java)
        intent.putExtra("id", keyword)
        this.startActivity(intent)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemDeleteClick(keyword: String) {
        db.delete("BlackList", "uid = ?", arrayOf(keyword))
        viewModel.historyList.remove(keyword)
        mAdapter.notifyDataSetChanged()
    }


}