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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentSearchBinding
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.feed.reply.IOnItemClickListener
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>(), IOnItemClickListener {

    @Inject
    lateinit var viewModelAssistedFactory: SearchFragmentViewModel.Factory
    private val viewModel by viewModels<SearchFragmentViewModel> {
        SearchFragmentViewModel.provideFactory(
            viewModelAssistedFactory,
            arguments?.getString("pageType").orEmpty(),
            arguments?.getString("pageParam").orEmpty(),
            arguments?.getString("title").orEmpty(),
        )
    }
    private var mAdapter: HistoryAdapter? = null
    private var mLayoutManager: FlexboxLayoutManager? = null

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

        initView()
        initEditText()
        initEdit()
        initButton()
        initClearHistory()

        viewModel.blackListLiveData.observe(viewLifecycleOwner) {
            mAdapter?.submitList(it)
            binding.clearAll.isVisible = it.isNotEmpty()
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
                    viewModel.deleteAll()
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
                    R.id.fragmentContainer,
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
                MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorPrimaryDark,
                    0
                ), 128
            )
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(binding.editText, 0)
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            inputType = EditorInfo.TYPE_CLASS_TEXT
            hint = if (viewModel.pageType.isNotEmpty()) "在 ${viewModel.title} 中搜索"
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
                    binding.clear.isVisible = s.isNotEmpty()
                }
            })

        }
    }

    override fun onItemClick(data: String) {
        binding.editText.setText(data)
        binding.editText.setSelection(data.length)
        search()
    }

    private fun updateHistory(data: String) {
        viewModel.insertData(data)
    }

    override fun onItemDeleteClick(data: String) {
        viewModel.deleteData(data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mLayoutManager = null
        mAdapter = null
    }

}