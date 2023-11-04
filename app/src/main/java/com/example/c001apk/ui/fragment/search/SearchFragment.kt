package com.example.c001apk.ui.fragment.search

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.c001apk.R
import com.example.c001apk.adapter.HistoryAdapter
import com.example.c001apk.databinding.FragmentSearchBinding
import com.example.c001apk.ui.fragment.minterface.IOnItemClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SearchFragment : Fragment(), IOnItemClickListener {

    private lateinit var binding: FragmentSearchBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var dbHelper: HistoryDataBaseHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var mAdapter: HistoryAdapter
    private lateinit var mLayoutManager: FlexboxLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = HistoryDataBaseHelper(requireContext(), "SearchHistory.db", 1)
        db = dbHelper.writableDatabase

        if (viewModel.historyList.isEmpty())
            binding.historyLayout.visibility = View.GONE
        else
            binding.historyLayout.visibility = View.VISIBLE

        initView()
        if (viewModel.historyList.isEmpty())
            queryData()

        initEditText()
        initEdit()
        initButton()
        initClearHistory()

    }

    private fun initView() {
        mLayoutManager = FlexboxLayoutManager(activity)
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
        val cursor = db.query("SearchHistory", null, null, null, null, null, null)
        viewModel.historyList.clear()
        if (cursor.moveToLast()) {
            do {
                val history = cursor.getString(cursor.getColumnIndex("keyword"))
                viewModel.historyList.add(history)
                mAdapter.notifyDataSetChanged()
                if (viewModel.historyList.isEmpty())
                    binding.historyLayout.visibility = View.GONE
                else
                    binding.historyLayout.visibility = View.VISIBLE
            } while (cursor.moveToPrevious())
            cursor.close()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initClearHistory() {
        binding.clearAll.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.clearAllTitle)
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    db.delete("SearchHistory", "", arrayOf())
                    viewModel.historyList.clear()
                    mAdapter.notifyDataSetChanged()
                    binding.historyLayout.visibility = View.GONE
                }
                show()
            }
        }
    }

    private fun initButton() {
        binding.search.setOnClickListener {
            search()
        }
    }

    private fun initEdit() {
        binding.editText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, keyEvent ->
            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEARCH) && keyEvent != null) {
                search()
                return@OnEditorActionListener false
            }
            false
        })
    }

    private fun search() {
        if (binding.editText.text.toString() == "") {
            Toast.makeText(activity, "请输入关键词", Toast.LENGTH_SHORT).show()
            //hideKeyBoard()
        } else {
            hideKeyBoard()
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.searchFragment,
                    SearchResultFragment.newInstance(binding.editText.text.toString()),
                    null
                )
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
            updateHistory(binding.editText.text.toString())
            //binding.editText.text = null
        }
    }

    @SuppressLint("Range", "NotifyDataSetChanged")
    private fun saveHistory(keyword: String) {
        var isExist = false
        val cursor = db.query("SearchHistory", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val history = cursor.getString(cursor.getColumnIndex("keyword"))
                if (keyword == history)
                    isExist = true
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (!isExist) {
            viewModel.historyList.add(0, keyword)
            mAdapter.notifyDataSetChanged()
            val value = ContentValues().apply {
                put("keyword", keyword)
            }
            db.insert("SearchHistory", null, value)
        }
    }

    private fun hideKeyBoard() {
        val im =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(
            requireActivity().currentFocus!!.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    private fun initEditText() {
        binding.editText.isFocusable = true
        binding.editText.isFocusableInTouchMode = true
        binding.editText.requestFocus()
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.editText, 0)
        binding.editText.imeOptions = EditorInfo.IME_ACTION_SEARCH
        binding.editText.inputType = EditorInfo.TYPE_CLASS_TEXT
    }

    override fun onStart() {
        super.onStart()
        initEditText()
    }

    override fun onItemClick(keyword: String) {
        binding.editText.setText(keyword)
        search()
        updateHistory(keyword)
    }

    private fun updateHistory(keyword: String) {
        viewModel.historyList.remove(keyword)
        db.delete("SearchHistory", "keyword = ?", arrayOf(keyword))
        saveHistory(keyword)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemDeleteClick(keyword: String) {
        db.delete("SearchHistory", "keyword = ?", arrayOf(keyword))
        viewModel.historyList.remove(keyword)
        mAdapter.notifyDataSetChanged()
    }

}