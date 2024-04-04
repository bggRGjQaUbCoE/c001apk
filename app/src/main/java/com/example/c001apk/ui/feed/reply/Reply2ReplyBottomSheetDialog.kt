package com.example.c001apk.ui.feed.reply

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.absinthe.libraries.utils.utils.UiUtils
import com.example.c001apk.R
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.constant.Constants
import com.example.c001apk.databinding.DialogReplyToReplyBottomSheetBinding
import com.example.c001apk.databinding.ItemCaptchaBinding
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.util.PrefManager
import com.example.c001apk.view.ReplyItemDecoration
import com.example.c001apk.view.ReplyStaggerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Reply2ReplyBottomSheetDialog : BottomSheetDialogFragment(), IOnPublishClickListener {

    private lateinit var binding: DialogReplyToReplyBottomSheetBinding
    private val viewModel by viewModels<Reply2ReplyBottomSheetViewModel>()
    private lateinit var mAdapter: Reply2ReplyTotalAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var bottomSheetDialog: ReplyBottomSheetDialog
    var oriReply: ArrayList<TotalReplyResponse.Data> = ArrayList()
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private val isPortrait by lazy { resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }

    companion object {
        fun newInstance(
            position: Int,
            fuid: String,
            uid: String,
            id: String
        ): Reply2ReplyBottomSheetDialog {
            val args = Bundle()
            args.putString("fuid", fuid)
            args.putString("uid", uid)
            args.putString("id", id)
            args.putInt("position", position)
            val fragment = Reply2ReplyBottomSheetDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private fun setData() {
        arguments?.let {
            viewModel.id = it.getString("id", "")
            viewModel.fuid = it.getString("fuid", "")
            viewModel.uid = it.getString("uid", "")
            viewModel.position = it.getInt("position")
            viewModel.oriReply = oriReply
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        object : BottomSheetDialog(requireContext(), theme) {

            override fun onAttachedToWindow() {
                super.onAttachedToWindow()

                window?.let {
                    it.attributes?.windowAnimations =
                        com.absinthe.libraries.utils.R.style.DialogAnimation
                    WindowCompat.setDecorFitsSystemWindows(it, false)
                    UiUtils.setSystemBarStyle(it)
                    WindowInsetsControllerCompat(it, it.decorView)
                        .isAppearanceLightNavigationBars = !UiUtils.isDarkMode()
                }

                findViewById<View>(com.google.android.material.R.id.container)?.fitsSystemWindows =
                    false
                findViewById<View>(com.google.android.material.R.id.coordinator)?.fitsSystemWindows =
                    false

            }

        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogReplyToReplyBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setData()
        initView()
        initData()
        initScroll()
        initObserve()

        if (PrefManager.isLogin) {
            val view1 = LayoutInflater.from(context)
                .inflate(R.layout.dialog_reply_bottom_sheet, null, false)
            bottomSheetDialog = ReplyBottomSheetDialog(requireContext(), view1)
            bottomSheetDialog.setIOnPublishClickListener(this)
            bottomSheetDialog.apply {
                setContentView(view1)
                setCancelable(false)
                setCanceledOnTouchOutside(true)
                window?.apply {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                type = "reply"
            }
        }

    }

    private fun initObserve() {
        viewModel.createDialog.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                val binding = ItemCaptchaBinding.inflate(
                    LayoutInflater.from(requireContext()), null, false
                )
                binding.captchaImg.setImageBitmap(it)
                binding.captchaText.highlightColor = ColorUtils.setAlphaComponent(
                    MaterialColors.getColor(
                        requireContext(),
                        com.google.android.material.R.attr.colorPrimaryDark,
                        0
                    ), 128
                )
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setView(binding.root)
                    setTitle("captcha")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton("验证并继续") { _, _ ->
                        viewModel.requestValidateData = HashMap()
                        viewModel.requestValidateData["type"] = "err_request_captcha"
                        viewModel.requestValidateData["code"] = binding.captchaText.text.toString()
                        viewModel.requestValidateData["mobile"] = ""
                        viewModel.requestValidateData["idcard"] = ""
                        viewModel.requestValidateData["name"] = ""
                        viewModel.onPostRequestValidate()
                    }
                    show()
                }
            }
        }

        viewModel.closeSheet.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (it && ::bottomSheetDialog.isInitialized && bottomSheetDialog.isShowing) {
                    bottomSheetDialog.editText.text = null
                    bottomSheetDialog.dismiss()
                }
            }
        }

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.footerState.observe(viewLifecycleOwner) {
            footerAdapter.setLoadState(it)
            if (it !is FooterState.Loading) {
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }

        viewModel.totalReplyData.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            mAdapter.submitList(it)

            val adapter = binding.recyclerView.adapter as ConcatAdapter
            if (!adapter.adapters.contains(mAdapter)) {
                adapter.apply {
                    addAdapter(mAdapter)
                    addAdapter(footerAdapter)
                }
            }
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    viewModel.lastVisibleItemPosition = if (isPortrait)
                        mLayoutManager.findLastVisibleItemPosition()
                    else
                        sLayoutManager.findLastVisibleItemPositions(null).max()

                    if (viewModel.lastVisibleItemPosition == viewModel.listSize
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        loadMore()
                    }
                }
            }
        })
    }

    private fun loadMore() {
        viewModel.isLoadMore = true
        viewModel.fetchReplyTotal()
    }

    private fun initData() {
        if (viewModel.listSize == -1) {
            binding.indicator.parent.isIndeterminate = true
            binding.indicator.parent.isVisible = true
            viewModel.isEnd = false
            viewModel.isLoadMore = false
            viewModel.fetchReplyTotal()
        }
    }

    private fun initView() {
        mAdapter =
            Reply2ReplyTotalAdapter(
                ItemClickListener(),
                viewModel.fuid.toString(),
                viewModel.uid.toString(),
            )
        footerAdapter = FooterAdapter(ReloadListener())
        binding.recyclerView.apply {
            adapter = ConcatAdapter()
            layoutManager =
                if (isPortrait) {
                    mLayoutManager = LinearLayoutManager(requireContext())
                    mLayoutManager
                } else {
                    sLayoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    sLayoutManager
                }
            if (isPortrait)
                addItemDecoration(ReplyItemDecoration(requireContext(), 1))
            else
                addItemDecoration(ReplyStaggerItemDecoration(10.dp))
        }
    }

    override fun onStart() {
        super.onStart()
        val view: FrameLayout =
            dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!
        view.layoutParams.height = -1
        view.layoutParams.width = -1
        val behavior = BottomSheetBehavior.from(view)
        if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            behavior.halfExpandedRatio = 0.75F
            behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    private fun initReply() {
        bottomSheetDialog.apply {
            rid = viewModel.rid.toString()
            ruid = viewModel.ruid.toString()
            uname = viewModel.uname.toString()
            setData()
            show()
        }
    }

    override fun onPublish(message: String, replyAndForward: String) {
        viewModel.replyData["message"] = message
        viewModel.replyData["replyAndForward"] = replyAndForward
        viewModel.onPostReply()
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }

    inner class ItemClickListener : ItemListener {
        override fun onViewFeed(
            view: View,
            id: String?,
            uid: String?,
            username: String?,
            userAvatar: String?,
            deviceTitle: String?,
            message: String?,
            dateline: String?,
            rid: Any?,
            isViewReply: Any?
        ) {
            super.onViewFeed(
                view,
                id,
                uid,
                username,
                userAvatar,
                deviceTitle,
                message,
                dateline,
                rid,
                isViewReply
            )
            if (!uid.isNullOrEmpty() && PrefManager.isRecordHistory)
                viewModel.saveHistory(
                    id.toString(), uid.toString(), username.toString(), userAvatar.toString(),
                    deviceTitle.toString(), message.toString(), dateline.toString()
                )
        }

        override fun onLikeClick(type: String, id: String, isLike: Int) {
            if (PrefManager.isLogin)
                if (PrefManager.SZLMID.isEmpty())
                    Toast.makeText(requireContext(), Constants.SZLM_ID, Toast.LENGTH_SHORT).show()
                else
                    viewModel.onPostLikeReply(id, isLike)
        }

        override fun onReply(
            id: String,
            uid: String,
            username: String?,
            position: Int,
            rPosition: Int?
        ) {
            if (PrefManager.isLogin) {
                if (PrefManager.SZLMID == "") {
                    Toast.makeText(requireContext(), Constants.SZLM_ID, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    viewModel.rid = id
                    viewModel.ruid = uid
                    viewModel.uname = username
                    viewModel.position = position
                    initReply()
                }
            }
        }

        override fun onBlockUser(id: String, uid: String, position: Int) {
            viewModel.saveUid(uid)
            val currentList = viewModel.totalReplyData.value?.toMutableList() ?: ArrayList()
            currentList.removeAt(position)
            viewModel.totalReplyData.postValue(currentList)
        }

        override fun onDeleteClicked(entityType: String, id: String, position: Int) {
            viewModel.postDeleteFeedReply("/v6/feed/deleteReply", id, position)
        }

        override fun showTotalReply(id: String, uid: String, position: Int, rPosition: Int?) {
            val mBottomSheetDialogFragment =
                newInstance(
                    position,
                    viewModel.uid.toString(),
                    uid,
                    id
                )
            val feedReplyList = viewModel.totalReplyData.value ?: ArrayList()
            mBottomSheetDialogFragment.oriReply.add(feedReplyList[position])

            mBottomSheetDialogFragment.show(childFragmentManager, "Dialog")
        }
    }

}