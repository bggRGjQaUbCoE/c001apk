package com.example.c001apk.ui.topic

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.ui.base.BaseAppFragment
import com.example.c001apk.ui.search.IOnSearchMenuClickContainer
import com.example.c001apk.ui.search.IOnSearchMenuClickListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TopicContentFragment : BaseAppFragment<TopicContentViewModel>(),
    IOnSearchMenuClickListener {

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

    override fun initObserve() {
        super.initObserve()

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
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
        viewModel.dataList.value = emptyList()
        viewModel.footerState.value = FooterState.LoadingDone
        viewModel.loadingState.value = LoadingState.Loading
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.title == "讨论")
            (parentFragment as? IOnSearchMenuClickContainer)?.controller = this
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.title == "讨论")
            (parentFragment as? IOnSearchMenuClickContainer)?.controller = null
    }

}