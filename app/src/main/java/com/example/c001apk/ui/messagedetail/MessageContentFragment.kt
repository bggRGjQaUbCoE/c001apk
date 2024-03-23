package com.example.c001apk.ui.messagedetail

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.ui.base.BaseViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MessageContentFragment : BaseViewFragment<MessageViewModel>() {

    @Inject
    lateinit var viewModelAssistedFactory: MessageViewModel.Factory
    override val viewModel by viewModels<MessageViewModel> {
        MessageViewModel.provideFactory(
            viewModelAssistedFactory,
            arguments?.getString("type").orEmpty()
        )
    }
    private lateinit var messAdapter: MessageContentAdapter
    private lateinit var footerAdapter: FooterAdapter

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            MessageContentFragment().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                }
            }
    }

    override fun initAdapter() {
        messAdapter = MessageContentAdapter(viewModel.type, object : ItemListener {})
        footerAdapter = FooterAdapter(ReloadListener())
        mAdapter = ConcatAdapter(HeaderAdapter(), messAdapter, footerAdapter)
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }

    override fun initObserve() {
        super.initObserve()
        viewModel.messageListData.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            messAdapter.submitList(it)
        }

        viewModel.footerState.observe(viewLifecycleOwner) {
            footerAdapter.setLoadState(it)
            if (it !is FooterState.Loading) {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}