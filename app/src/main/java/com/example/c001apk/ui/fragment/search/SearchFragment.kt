package com.example.c001apk.ui.fragment.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.c001apk.R
import com.example.c001apk.adapter.HistoryAdapter
import com.example.c001apk.databinding.FragmentSearchBinding
import com.example.c001apk.logic.database.SearchHistoryDatabase
import com.example.c001apk.logic.model.SearchHistory
import com.example.c001apk.ui.fragment.BaseFragment
import com.example.c001apk.ui.fragment.minterface.IOnItemClickListener
import com.example.c001apk.util.PrefManager
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SearchFragment : BaseFragment<FragmentSearchBinding>(), IOnItemClickListener {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: HistoryAdapter
    private lateinit var mLayoutManager: FlexboxLayoutManager
    private val searchHistoryDao by lazy {
        SearchHistoryDatabase.getDatabase(this@SearchFragment.requireContext()).searchHistoryDao()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.pageType = it.getString("pageType")
            viewModel.pageParam = it.getString("pageParam")
            viewModel.title = it.getString("title")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(pageType: String?, pageParam: String?, title: String?) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString("pageType", pageType)
                    putString("pageParam", pageParam)
                    putString("title", title)
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.historyList.isEmpty())
            binding.historyLayout.visibility = View.GONE
        else
            binding.historyLayout.visibility = View.VISIBLE

        initView()
        if (viewModel.historyList.isEmpty()) {
            viewModel.isNew = true
            viewModel.getBlackList("history", requireContext())
        }

        initEditText()
        initEdit()
        initButton()
        initClearHistory()

        viewModel.blackListLiveData.observe(viewLifecycleOwner) {
            if (viewModel.isNew) {
                viewModel.isNew = false

                try {
                    viewModel.historyList.clear()
                    viewModel.historyList.addAll(it)
                    if (viewModel.historyList.isEmpty())
                        binding.historyLayout.visibility = View.GONE
                    else
                        binding.historyLayout.visibility = View.VISIBLE
                    mAdapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw IllegalArgumentException("searchFragment: fail to load keyword: ${e.message}")
                }
            }
        }

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

    @SuppressLint("NotifyDataSetChanged")
    private fun initClearHistory() {
        binding.clearAll.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.clearAllTitle)
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    try {
                        CoroutineScope(Dispatchers.IO).launch {
                            searchHistoryDao.deleteAll()
                        }
                        viewModel.historyList.clear()
                        mAdapter.notifyDataSetChanged()
                        binding.historyLayout.visibility = View.GONE
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw IllegalArgumentException("searchFragment: fail to clear keyword: ${e.message}")
                    }
                }
                show()
            }
        }
    }

    private fun initButton() {
        binding.toolBar.apply {
            setNavigationOnClickListener {
                requireActivity().finish()
            }
        }
        binding.search.setOnClickListener {
            search()
        }
        binding.clear.setOnClickListener {
            binding.editText.text = null
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
            //hideKeyBoard()
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.searchFragment,
                    SearchResultFragment.newInstance(
                        binding.editText.text.toString(),
                        viewModel.pageType,
                        viewModel.pageParam,
                        viewModel.title
                    ),
                    null
                )
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
            updateHistory(binding.editText.text.toString())
            if (PrefManager.isClearKeyWord)
                binding.editText.text = null
        }
    }

    /*private fun hideKeyBoard() {
        val im =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(
            requireActivity().currentFocus!!.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
*/
    @SuppressLint("RestrictedApi")
    private fun initEditText() {
        binding.editText.apply {
            highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editText, 0)
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            inputType = EditorInfo.TYPE_CLASS_TEXT
            hint = if (!viewModel.pageType.isNullOrEmpty()) "在 ${viewModel.title} 中搜索"
            else "搜索"

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                    if (s.isEmpty())
                        binding.clear.visibility = View.GONE
                    else
                        binding.clear.visibility = View.VISIBLE
                }
            })

        }
    }

    /*override fun onStart() {
        super.onStart()
        initEditText()
    }
*/
    override fun onItemClick(keyword: String) {
        binding.editText.setText(keyword)
        binding.editText.setSelection(keyword.length)
        search()
    }

    private fun updateHistory(keyword: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                if (searchHistoryDao.isExist(keyword)) {
                    searchHistoryDao.delete(keyword)
                }
                searchHistoryDao.insert(SearchHistory(keyword))
            }
            viewModel.historyList.remove(keyword)
            viewModel.historyList.add(0, keyword)
            mAdapter.notifyItemInserted(0)
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("searchFragment: fail to update keyword: ${e.message}")
        }

    }

    override fun onItemDeleteClick(keyword: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                searchHistoryDao.delete(keyword)
            }
            val position = viewModel.historyList.indexOf(keyword)
            viewModel.historyList.removeAt(position)
            mAdapter.notifyItemRemoved(position)
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("searchFragment: fail to delete keyword: ${e.message}")
        }
    }

}