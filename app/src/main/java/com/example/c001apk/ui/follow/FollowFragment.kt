package com.example.c001apk.ui.follow

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.example.c001apk.ui.base.BaseAppFragment
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.util.PrefManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FollowFragment : BaseAppFragment<FollowViewModel>(), IOnTabClickListener {

    @Inject
    lateinit var viewModelAssistedFactory: FollowViewModel.Factory
    override val viewModel by viewModels<FollowViewModel> {
        FollowViewModel.provideFactory(
            viewModelAssistedFactory,
            uid = arguments?.getString("uid") ?: PrefManager.uid,
            type = arguments?.getString("type").orEmpty(),
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(uid: String? = null, type: String) =
            FollowFragment().apply {
                arguments = Bundle().apply {
                    putString("uid", uid)
                    putString("type", type)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            when (viewModel.type) {
                "topic" -> {
                    viewModel.url = "#/topic/userFollowTagList"
                    viewModel.title = "我关注的话题"
                }

                "product" -> {
                    viewModel.url = "#/product/followProductList"
                    viewModel.title = "我关注的数码吧"
                }

                "favorite" -> {
                    viewModel.url = "#/collection/followList"
                    viewModel.title = "我关注的收藏单"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? IOnTabClickContainer)?.tabController = this
    }

    override fun onPause() {
        super.onPause()
        (activity as? IOnTabClickContainer)?.tabController = null
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        binding.swipeRefresh.isRefreshing = true
        binding.recyclerView.scrollToPosition(0)
        refreshData()
    }

}