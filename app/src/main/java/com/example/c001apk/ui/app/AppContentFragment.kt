package com.example.c001apk.ui.app

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.c001apk.ui.base.BaseAppFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppContentFragment : BaseAppFragment<AppContentViewModel>() {

    private val type by lazy { arguments?.getString("type") }

    @Inject
    lateinit var viewModelAssistedFactory: AppContentViewModel.Factory
    override val viewModel by viewModels<AppContentViewModel> {
        AppContentViewModel.provideFactory(
            viewModelAssistedFactory,
            arguments?.getString("id").orEmpty(),
            when (type) {
                "reply" -> ""
                "pub" -> "&sort=dateline_desc"
                "hot" -> "&sort=popular"
                else -> ""
            },
            when (type) {
                "reply" -> "最近回复"
                "pub" -> "最新发布"
                "hot" -> "热度排序"
                else -> "最近回复"
            }
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String, id: String) =
            AppContentFragment().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                    putString("id", id)
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