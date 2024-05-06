package com.example.c001apk.ui.hometopic

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.example.c001apk.adapter.PlaceHolderAdapter
import com.example.c001apk.ui.base.BaseAppFragment
import com.example.c001apk.ui.main.MainActivity
import com.example.c001apk.util.setSpaceFooterView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeTopicContentFragment : BaseAppFragment<HomeTopicContentViewModel>() {

    @Inject
    lateinit var viewModelAssistedFactory: HomeTopicContentViewModel.Factory
    override val viewModel by viewModels<HomeTopicContentViewModel> {
        HomeTopicContentViewModel.provideFactory(
            viewModelAssistedFactory,
            arguments?.getString("url").orEmpty(),
            arguments?.getString("title").orEmpty(),
        )
    }
    private val placeHolderAdapter by lazy { PlaceHolderAdapter() }

    companion object {
        @JvmStatic
        fun newInstance(url: String, title: String) = HomeTopicContentFragment().apply {
            arguments = Bundle().apply {
                putString("url", url)
                putString("title", title)
            }
        }
    }

    override fun initView() {
        super.initView()

        binding.vfContainer.setOnDisplayedChildChangedListener {
            binding.recyclerView.setSpaceFooterView(placeHolderAdapter)
        }
    }

    override fun onScrolled(dy: Int) {
        if (dy > 0) {
            (activity as? MainActivity)?.hideNavigationView()
        } else if (dy < 0) {
            (activity as? MainActivity)?.showNavigationView()
        }
    }
}