package com.example.c001apk.ui.search

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.ui.base.BaseAppFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchContentFragment : BaseAppFragment<SearchContentViewModel>(),
    IOnSearchMenuClickListener {

    @Inject
    lateinit var viewModelAssistedFactory: SearchContentViewModel.Factory
    override val viewModel by viewModels<SearchContentViewModel> {
        SearchContentViewModel.provideFactory(
            viewModelAssistedFactory,
            arguments?.getString("keyWord").orEmpty(),
            arguments?.getString("type").orEmpty(),
            arguments?.getString("pageType").orEmpty(),
            arguments?.getString("pageParam").orEmpty(),
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(keyWord: String, type: String, pageType: String?, pageParam: String?) =
            SearchContentFragment().apply {
                arguments = Bundle().apply {
                    putString("keyWord", keyWord)
                    putString("type", type)
                    putString("pageType", pageType)
                    putString("pageParam", pageParam)
                }
            }
    }

    override fun onSearch(type: String, value: String, id: String?) {
        when (type) {
            "sort" -> viewModel.sort = value
            "feedType" -> viewModel.feedType = value
        }
        viewModel.dataList.value = emptyList()
        viewModel.footerState.value = FooterState.LoadingDone
        viewModel.loadingState.value = LoadingState.Loading
    }

    override fun onResume() {
        super.onResume()
        (parentFragment as? IOnSearchMenuClickContainer)?.controller = this
    }

    override fun onPause() {
        super.onPause()
        (parentFragment as? IOnSearchMenuClickContainer)?.controller = null
    }

    override fun initObserve() {
        super.initObserve()

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

}