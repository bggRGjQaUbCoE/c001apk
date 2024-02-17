package com.example.c001apk.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModelProvider
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentSearchBinding
import com.example.c001apk.logic.database.SearchHistoryDatabase
import com.example.c001apk.logic.model.SearchHistory
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.feed.reply.IOnItemClickListener
import com.example.c001apk.util.Utils.getColorFromAttr
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SearchFragment : BaseFragment<FragmentSearchBinding>(), IOnItemClickListener {

    private val viewModel by lazy { ViewModelProvider(this)[SearchViewModel::class.java] }
    private var mAdapter: HistoryAdapter? = null
    private var mLayoutManager: FlexboxLayoutManager? = null
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.listSize == -1) {
            binding.clearAll.visibility = View.GONE
            viewModel.getBlackList("history", requireContext())
        } else
            binding.clearAll.visibility = View.VISIBLE

        initView()
        initEditText()
        initEdit()
        initButton()
        initClearHistory()

        viewModel.blackListLiveData.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            mAdapter?.submitList(it)
            if (it.isEmpty())
                binding.clearAll.visibility = View.GONE
            else
                binding.clearAll.visibility = View.VISIBLE
        }

    }

    private fun initView() {
        mLayoutManager = FlexboxLayoutManager(requireContext(), FlexDirection.ROW, FlexWrap.WRAP)
        mAdapter = HistoryAdapter()
        mAdapter?.setOnItemClickListener(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
        }
    }

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
                        viewModel.blackListLiveData.postValue(emptyList())
                        binding.clearAll.visibility = View.GONE
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
            Toast.makeText(requireContext(), "请输入关键词", Toast.LENGTH_SHORT).show()
        } else {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.right_in,
                    R.anim.left_out_fragment,
                    R.anim.left_in,
                    R.anim.right_out
                )
                .replace(
                    R.id.searchFragment,
                    SearchResultFragment.newInstance(
                        binding.editText.text.toString(),
                        viewModel.pageType,
                        viewModel.pageParam,
                        viewModel.title
                    )
                )
                .addToBackStack(null)
                .commit()
            updateHistory(binding.editText.text.toString())
            hideKeyBoard()
        }
    }

    private fun hideKeyBoard() {
        binding.editText.clearFocus()
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(binding.editText.windowToken, 0)
    }

    private fun initEditText() {
        binding.editText.apply {
            highlightColor = ColorUtils.setAlphaComponent(
                requireContext().getColorFromAttr(
                    rikka.preference.simplemenu.R.attr.colorPrimary
                ), 128
            )
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(binding.editText, 0)
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
            val currentList = viewModel.blackListLiveData.value?.toMutableList() ?: ArrayList()
            val index = currentList.indexOf(keyword)
            if (index != -1)
                currentList.removeAt(index)
            currentList.add(0, keyword)
            viewModel.blackListLiveData.postValue(currentList)
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("searchFragment: fail to update keyword: ${e.message}")
        }
    }

    override fun onItemDeleteClick(position: Int, keyword: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                searchHistoryDao.delete(keyword)
            }
            val currentList = viewModel.blackListLiveData.value?.toMutableList() ?: ArrayList()
            currentList.removeAt(position)
            viewModel.blackListLiveData.postValue(currentList)
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("searchFragment: fail to delete keyword: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mLayoutManager = null
        mAdapter = null
    }

}