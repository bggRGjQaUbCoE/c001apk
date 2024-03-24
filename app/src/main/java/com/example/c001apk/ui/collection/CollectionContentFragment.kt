package com.example.c001apk.ui.collection

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.c001apk.R
import com.example.c001apk.ui.base.BaseAppFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CollectionContentFragment : BaseAppFragment<CollectionContentViewModel>() {

    @Inject
    lateinit var viewModelAssistedFactory: CollectionContentViewModel.Factory
    override val viewModel by viewModels<CollectionContentViewModel> {
        CollectionContentViewModel.provideFactory(
            viewModelAssistedFactory,
            arguments?.getString("url").orEmpty(),
            arguments?.getString("id")
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String, id: String?) =
            CollectionContentFragment().apply {
                arguments = Bundle().apply {
                    putString("url", url)
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

        viewModel.showCollection.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                        R.anim.right_in,
                        R.anim.left_out_fragment,
                        R.anim.left_in,
                        R.anim.right_out
                    )
                    .replace(
                        R.id.fragmentContainer,
                        CollectionFragment.newInstance(it.first, it.second)
                    )
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

}