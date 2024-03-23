package com.example.c001apk.ui.topic

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.ui.base.BaseAppFragment
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.ui.search.IOnSearchMenuClickContainer
import com.example.c001apk.ui.search.IOnSearchMenuClickListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TopicContentFragment : BaseAppFragment<TopicContentViewModel>(),
    IOnSearchMenuClickListener, IOnTabClickListener {

    @Inject
    lateinit var viewModelAssistedFactory: TopicContentViewModel.Factory
    override val viewModel by viewModels<TopicContentViewModel> {
        TopicContentViewModel.provideFactory(
            viewModelAssistedFactory,
            arguments?.getString("url").orEmpty(),
            arguments?.getString("title").orEmpty(),
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String, title: String) =
            TopicContentFragment().apply {
                arguments = Bundle().apply {
                    putString("url", url)
                    putString("title", title)
                }
            }
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        binding.swipeRefresh.isRefreshing = true
        binding.recyclerView.scrollToPosition(0)
        refreshData()
    }

    override fun onSearch(type: String, value: String, id: String?) {
        viewModel.title = value
        when (value) {
            "最近回复" -> viewModel.url =
                "/page?url=/product/feedList?type=feed&id=$id&ignoreEntityById=1"

            "热度排序" -> viewModel.url =
                "/page?url=/product/feedList?type=feed&id=$id&listType=rank_score"

            "最新发布" -> viewModel.url =
                "/page?url=/product/feedList?type=feed&id=$id&ignoreEntityById=1&listType=dateline_desc"
        }
        viewModel.dataList.postValue(emptyList())
        viewModel.footerState.value = FooterState.LoadingDone
        binding.swipeRefresh.isEnabled = false
        binding.errorMessage.errMsg.isVisible = false
        binding.errorLayout.parent.isVisible = false
        viewModel.loadingState.value = LoadingState.Loading
    }

    override fun onResume() {
        super.onResume()
        (parentFragment as? IOnTabClickContainer)?.tabController = this
        if (viewModel.title == "讨论")
            (parentFragment as? IOnSearchMenuClickContainer)?.controller = this
    }

    override fun onPause() {
        super.onPause()
        (parentFragment as? IOnTabClickContainer)?.tabController = null
        if (viewModel.title == "讨论")
            (parentFragment as? IOnSearchMenuClickContainer)?.controller = null
    }

}