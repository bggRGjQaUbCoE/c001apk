package com.example.c001apk.ui.dyh

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.example.c001apk.ui.base.BaseAppFragment
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DyhDetailFragment : BaseAppFragment<DyhViewModel>(), IOnTabClickListener {

    @Inject
    lateinit var viewModelAssistedFactory: DyhViewModel.Factory
    override val viewModel by viewModels<DyhViewModel> {
        DyhViewModel.provideFactory(
            viewModelAssistedFactory,
            arguments?.getString("id").orEmpty(),
            arguments?.getString("type").orEmpty()
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(id: String, type: String) =
            DyhDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("id", id)
                    putString("type", type)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        (parentFragment as? IOnTabClickContainer)?.tabController = this
    }

    override fun onPause() {
        super.onPause()
        (parentFragment as? IOnTabClickContainer)?.tabController = null
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        binding.swipeRefresh.isRefreshing = true
        binding.recyclerView.scrollToPosition(0)
        refreshData()
    }


}