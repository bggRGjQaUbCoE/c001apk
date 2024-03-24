package com.example.c001apk.ui.follow

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.c001apk.ui.base.BaseAppFragment
import com.example.c001apk.util.PrefManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FollowFragment : BaseAppFragment<FollowViewModel>() {

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

    override fun initObserve() {
        super.initObserve()

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

}