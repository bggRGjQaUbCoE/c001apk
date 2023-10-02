package com.example.c001apk.ui.fragment.feed.total

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.R
import com.example.c001apk.databinding.DialogReplyToReplyBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class Reply2ReplyBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogReplyToReplyBottomSheetBinding
    private val viewModel by lazy { ViewModelProvider(this)[ReplyTotalViewModel::class.java] }
    private lateinit var id: String
    private lateinit var mAdapter: Reply2ReplyTotalAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var lastVisibleItemPosition = 0

    companion object {
        fun newInstance(id: String): Reply2ReplyBottomSheetDialog {
            val args = Bundle()
            args.putString("ID", id)
            val fragment = Reply2ReplyBottomSheetDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private fun setData() {
        val args = arguments
        id = args!!.getString("ID", "")

    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogReplyToReplyBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setData()
        initView()
        initData()
        initScroll()

        viewModel.replyTotalLiveData.observe(viewLifecycleOwner) { result ->
            val data = result.getOrNull()
            if (!data.isNullOrEmpty()) {
                if (!viewModel.isLoadMore)
                    viewModel.replyTotalList.clear()
                viewModel.replyTotalList.addAll(data)
                mAdapter.notifyDataSetChanged()
            } else {
                viewModel.isEnd = true
                result.exceptionOrNull()?.printStackTrace()
            }
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == viewModel.replyTotalList.size - 1) {
                        if (!viewModel.isEnd) {
                            viewModel.isLoadMore = true
                            viewModel.page++
                            viewModel.getReplyTotal()
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
            }
        })
    }

    private fun initData() {
        viewModel.id = id
        if (viewModel.replyTotalList.isEmpty()) {
            viewModel.isEnd = false
            viewModel.isLoadMore = false
            viewModel.getReplyTotal()
        }
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)

        mAdapter = Reply2ReplyTotalAdapter(requireActivity(), viewModel.replyTotalList)
        mLayoutManager = LinearLayoutManager(activity)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    /**
     * 设置固定高度
     */
    override fun onStart() {
        super.onStart()
        val view: FrameLayout =
            dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!
        val behavior = BottomSheetBehavior.from(view)
        behavior.peekHeight = windowHeight
    }

    private val windowHeight: Int
        get() {
            val heightPixels = this.resources.displayMetrics.heightPixels
            return heightPixels - heightPixels / 4
        }


}