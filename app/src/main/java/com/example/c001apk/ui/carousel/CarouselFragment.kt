package com.example.c001apk.ui.carousel

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.c001apk.ui.base.BaseAppFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CarouselFragment : BaseAppFragment<CarouselViewModel>() {

    private val isSingle by lazy { arguments?.getBoolean("isSingle") ?: true }

    @Inject
    lateinit var viewModelAssistedFactory: CarouselViewModel.Factory
    override val viewModel by viewModels<CarouselViewModel>(
        factoryProducer = {
            CarouselViewModel.provideFactory(
                viewModelAssistedFactory,
                arguments?.getString("url").orEmpty(),
                arguments?.getString("title").orEmpty()
            )
        }, ownerProducer = {
            if (isSingle) requireActivity() else this
        })

    companion object {
        @JvmStatic
        fun newInstance(url: String, title: String, isSingle: Boolean = true) =
            CarouselFragment().apply {
                arguments = Bundle().apply {
                    putString("url", url)
                    putString("title", title)
                    putBoolean("isSingle", isSingle)
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