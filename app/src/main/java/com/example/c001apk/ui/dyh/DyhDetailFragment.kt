package com.example.c001apk.ui.dyh

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.c001apk.ui.base.BaseAppFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DyhDetailFragment : BaseAppFragment<DyhViewModel>() {

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

    override fun initObserve() {
        super.initObserve()

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

}