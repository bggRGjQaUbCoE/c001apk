package com.example.c001apk.ui.user

import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.c001apk.ui.base.BaseAppFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserFragment : BaseAppFragment<UserViewModel>() {

    override val viewModel by viewModels<UserViewModel>(ownerProducer = { requireActivity() })

    override fun initObserve() {
        super.initObserve()

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

}