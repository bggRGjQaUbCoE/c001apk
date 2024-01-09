package com.example.c001apk.ui.fragment.feed

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.absinthe.libraries.utils.utils.UiUtils
import com.example.c001apk.R
import com.example.c001apk.adapter.Reply2ReplyTotalAdapter
import com.example.c001apk.databinding.DialogReplyToReplyBottomSheetBinding
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.fragment.ReplyBottomSheetDialog
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.ui.fragment.minterface.IOnPublishClickListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.RecyclerView.checkForGaps
import com.example.c001apk.util.RecyclerView.markItemDecorInsetsDirty
import com.example.c001apk.view.ReplyItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.lang.reflect.Method
import java.net.URLDecoder

class Reply2ReplyBottomSheetDialog : BottomSheetDialogFragment(), AppListener,
    IOnPublishClickListener {

    private lateinit var binding: DialogReplyToReplyBottomSheetBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: Reply2ReplyTotalAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var bottomSheetDialog: ReplyBottomSheetDialog
    var oriReply: ArrayList<TotalReplyResponse.Data> = ArrayList()
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private lateinit var mCheckForGapMethod: Method
    private lateinit var mMarkItemDecorInsetsDirtyMethod: Method

    companion object {
        fun newInstance(
            position: Int,
            fuid: String,
            uid: String,
            id: String
        ): Reply2ReplyBottomSheetDialog {
            val args = Bundle()
            args.putString("FUID", fuid)
            args.putString("UID", uid)
            args.putString("ID", id)
            args.putInt("POSITION", position)
            val fragment = Reply2ReplyBottomSheetDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private fun setData() {
        val args = arguments
        viewModel.id = args!!.getString("ID", "")
        viewModel.fuid = args.getString("FUID", "")
        viewModel.uid = args.getString("UID", "")
        viewModel.position = args.getInt("POSITION")
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog_Md3)
    }*/

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

    @SuppressLint("NotifyDataSetChanged", "InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setData()
        initView()
        initData()
        initScroll()

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

        viewModel.replyTotalLiveData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val reply = result.getOrNull()
                if (reply?.message != null) {
                    viewModel.errorMessage = reply.message
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    showReplyErrorMessage()
                    return@observe
                } else if (!reply?.data.isNullOrEmpty()) {
                    if (!viewModel.isLoadMore) {
                        viewModel.replyTotalList.clear()
                        viewModel.replyTotalList.addAll(oriReply)
                    }
                    viewModel.listSize = viewModel.replyTotalList.size
                    for (element in reply?.data!!)
                        if (element.entityType == "feed_reply")
                            if (!BlackListUtil.checkUid(element.uid))
                                viewModel.replyTotalList.add(element)
                    viewModel.loadState = mAdapter.LOADING_COMPLETE
                    mAdapter.setLoadState(viewModel.loadState, null)
                } else if (reply?.data?.isEmpty() == true) {
                    if (viewModel.replyTotalList.isEmpty())
                        viewModel.replyTotalList.addAll(oriReply)
                    viewModel.loadState = mAdapter.LOADING_END
                    mAdapter.setLoadState(viewModel.loadState, null)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                } else {
                    if (viewModel.replyTotalList.isEmpty())
                        viewModel.replyTotalList.addAll(oriReply)
                    viewModel.loadState = mAdapter.LOADING_ERROR
                    viewModel.errorMessage = getString(R.string.loading_failed)
                    mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                if (viewModel.isLoadMore)
                    if (viewModel.isEnd)
                        mAdapter.notifyItemChanged(viewModel.replyTotalList.size)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.listSize,
                            viewModel.replyTotalList.size - viewModel.listSize + 1
                        )
                else
                    mAdapter.notifyDataSetChanged()
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.visibility = View.GONE
                viewModel.isRefreshing = false
                viewModel.isLoadMore = false
            }
        }

        viewModel.likeReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostLikeReply) {
                viewModel.isPostLikeReply = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.replyTotalList[viewModel.likePosition].likenum = response.data
                        viewModel.replyTotalList[viewModel.likePosition].userAction?.like = 1
                        mAdapter.notifyItemChanged(viewModel.likePosition, "like")
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.unLikeReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostUnLikeReply) {
                viewModel.isPostUnLikeReply = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.replyTotalList[viewModel.likePosition].likenum = response.data
                        viewModel.replyTotalList[viewModel.likePosition].userAction?.like = 0
                        mAdapter.notifyItemChanged(viewModel.likePosition, "like")
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.postReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostReply) {
                viewModel.isPostReply = false

                val response = result.getOrNull()
                response?.let {
                    if (response.data != null) {
                        if (response.data.messageStatus == 1 || response.data.messageStatus == 2) {
                            bottomSheetDialog.editText.text = null
                            if (response.data.messageStatus == 1)
                                Toast.makeText(activity, "回复成功", Toast.LENGTH_SHORT).show()
                            bottomSheetDialog.cancel()
                            viewModel.replyTotalList.add(
                                viewModel.r2rPosition + 1,
                                TotalReplyResponse.Data(
                                    null,
                                    "feed_reply",
                                    viewModel.id.toString(),
                                    viewModel.ruid.toString(),
                                    PrefManager.uid,
                                    viewModel.id.toString(),
                                    URLDecoder.decode(PrefManager.username, "UTF-8"),
                                    viewModel.uname.toString(),
                                    viewModel.replyData["message"].toString(),
                                    "",
                                    null,
                                    System.currentTimeMillis() / 1000,
                                    "0",
                                    "0",
                                    PrefManager.userAvatar,
                                    ArrayList(),
                                    0,
                                    TotalReplyResponse.UserAction(0)
                                )
                            )
                            mAdapter.notifyItemInserted(viewModel.r2rPosition + 1)
                        }
                    } else {
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.postDeleteData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data == "删除成功") {
                        Toast.makeText(requireContext(), response.data, Toast.LENGTH_SHORT).show()
                        viewModel.replyTotalList.removeAt(viewModel.position)
                        mAdapter.notifyItemRemoved(viewModel.position)
                    } else if (!response.message.isNullOrEmpty()) {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun showReplyErrorMessage() {
        binding.replyErrorMessage.visibility = View.VISIBLE
        binding.replyErrorMessage.text = viewModel.errorMessage
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.replyTotalList.isNotEmpty() && !viewModel.isEnd)
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            viewModel.lastVisibleItemPosition =
                                mLayoutManager.findLastVisibleItemPosition()
                        } else {
                            val result =
                                mCheckForGapMethod.invoke(binding.recyclerView.layoutManager) as Boolean
                            if (result)
                                mMarkItemDecorInsetsDirtyMethod.invoke(binding.recyclerView)

                            val positions = sLayoutManager.findLastVisibleItemPositions(null)
                            for (pos in positions) {
                                if (pos > viewModel.lastVisibleItemPosition) {
                                    viewModel.lastVisibleItemPosition = pos
                                }
                            }
                        }

                    if (viewModel.lastVisibleItemPosition == viewModel.replyTotalList.size
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        viewModel.page++
                        loadMore()
                    }
                }
            }
        })
    }

    private fun loadMore() {
        viewModel.loadState = mAdapter.LOADING
        mAdapter.setLoadState(viewModel.loadState, null)
        mAdapter.notifyItemChanged(viewModel.replyTotalList.size)
        viewModel.isLoadMore = true
        viewModel.isNew = true
        viewModel.getReplyTotal()
    }

    private fun initData() {
        if (viewModel.replyTotalList.isEmpty()) {
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            viewModel.isEnd = false
            viewModel.isLoadMore = false
            viewModel.isNew = true
            viewModel.getReplyTotal()
        } else {
            mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
            mAdapter.notifyItemChanged(viewModel.replyTotalList.size)
        }
    }

    private fun initView() {
        mAdapter =
            Reply2ReplyTotalAdapter(
                requireContext(),
                viewModel.fuid.toString(),
                viewModel.uid.toString(),
                viewModel.position,
                viewModel.replyTotalList
            )
        mLayoutManager = LinearLayoutManager(activity)
        mAdapter.setAppListener(this)
        sLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // https://codeantenna.com/a/2NDTnG37Vg
            mCheckForGapMethod = checkForGaps
            mCheckForGapMethod.isAccessible = true
            mMarkItemDecorInsetsDirtyMethod = markItemDecorInsetsDirty
            mMarkItemDecorInsetsDirtyMethod.isAccessible = true
        }
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    mLayoutManager
                else sLayoutManager
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                addItemDecoration(ReplyItemDecoration(requireContext(), 1))
            else
                addItemDecoration(StaggerItemDecoration(10.dp))
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

    override fun onShowTotalReply(position: Int, uid: String, id: String, rPosition: Int?) {
        val mBottomSheetDialogFragment = newInstance(position, viewModel.fuid.toString(), uid, id)
        mBottomSheetDialogFragment.oriReply.add(viewModel.replyTotalList[position])
        mBottomSheetDialogFragment.show(childFragmentManager, "Dialog")
    }

    override fun onPostFollow(isFollow: Boolean, uid: String, position: Int) {}

    override fun onReply2Reply(
        rPosition: Int,
        r2rPosition: Int?,
        id: String,
        uid: String,
        uname: String,
        type: String
    ) {
        if (PrefManager.isLogin) {
            if (PrefManager.SZLMID == "") {
                Toast.makeText(activity, "数字联盟ID不能为空", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.rPosition = rPosition
                r2rPosition?.let { viewModel.r2rPosition = r2rPosition }
                viewModel.rid = id
                viewModel.ruid = uid
                viewModel.uname = uname
                viewModel.type = type
                initReply()
            }
        }
    }

    @SuppressLint("InflateParams", "RestrictedApi")
    private fun initReply() {
        bottomSheetDialog.apply {
            rid = viewModel.rid.toString()
            ruid = viewModel.ruid.toString()
            uname = viewModel.uname.toString()
            setData()
            show()
        }
    }

    override fun onPostLike(type: String?, isLike: Boolean, id: String, position: Int?) {
        viewModel.likeReplyId = id
        viewModel.likePosition = position!!
        if (isLike) {
            viewModel.isPostUnLikeReply = true
            viewModel.postUnLikeReply()
        } else {
            viewModel.isPostLikeReply = true
            viewModel.postLikeReply()
        }
    }

    override fun onRefreshReply(listType: String) {}

    override fun onPublish(message: String, replyAndForward: String) {
        viewModel.replyData["message"] = message
        viewModel.replyData["replyAndForward"] = replyAndForward
        viewModel.isPostReply = true
        viewModel.postReply()
    }

    override fun onDeleteFeedReply(id: String, position: Int, rPosition: Int?) {
        viewModel.rPosition = null
        viewModel.rPosition = rPosition
        viewModel.isNew = true
        viewModel.position = position
        viewModel.url = "/v6/feed/deleteReply"
        viewModel.deleteId = id
        viewModel.postDelete()
    }

    override fun onShowCollection(id: String, title: String) {}

    override fun onReload() {
        viewModel.isEnd = false
        loadMore()
    }

}