package com.example.c001apk.ui.appupdate

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.logic.model.UpdateCheckResponse
import com.example.c001apk.ui.base.BaseViewFragment
import com.example.c001apk.util.ClipboardUtil
import com.example.c001apk.util.Utils.downloadApk
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateListFragment : BaseViewFragment<UpdateListViewModel>() {

    override val viewModel by viewModels<UpdateListViewModel>()
    private lateinit var updateAdapter: UpdateListAdapter
    private lateinit var appsUpdateList: List<UpdateCheckResponse.Data>

    companion object {
        @JvmStatic
        fun newInstance(appsUpdateList: ArrayList<UpdateCheckResponse.Data>) =
            UpdateListFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("list", appsUpdateList)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            appsUpdateList =
                if (SDK_INT >= 33)
                    it.getParcelableArrayList("list", UpdateCheckResponse.Data::class.java)
                        ?.toList() ?: emptyList()
                else
                    it.getParcelableArrayList<UpdateCheckResponse.Data>("list")
                        ?.toList() ?: emptyList()
        }
    }


    override fun initObserve() {
        super.initRefresh()

        viewModel.download.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (it) {
                    val url = viewModel.urlMap[viewModel.packageName.toString()].orEmpty()
                    try {
                        downloadApk(
                            requireContext(),
                            url,
                            "${viewModel.appName}-${viewModel.versionName}-${viewModel.versionCode}.apk"
                        )
                    } catch (e: Exception) {
                        try {
                            requireContext().startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(url)
                                )
                            )
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(requireContext(), "下载失败", Toast.LENGTH_SHORT).show()
                            ClipboardUtil.copyText(requireContext(), url)
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    override fun initRefresh() {
        binding.swipeRefresh.isEnabled = false
    }

    override fun initAdapter() {
        updateAdapter = UpdateListAdapter(appsUpdateList, viewModel)
        mAdapter = ConcatAdapter(HeaderAdapter(), updateAdapter)
    }

}