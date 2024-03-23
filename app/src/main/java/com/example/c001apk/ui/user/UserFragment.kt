package com.example.c001apk.ui.user

import androidx.fragment.app.viewModels
import com.example.c001apk.ui.base.BaseAppFragment

class UserFragment : BaseAppFragment<UserViewModel>() {

    override val viewModel by viewModels<UserViewModel>(ownerProducer = { requireActivity() })

}