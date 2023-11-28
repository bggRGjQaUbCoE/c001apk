package com.example.c001apk.ui.fragment.feed

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.adapter.Reply2ReplyTotalAdapter
import com.example.c001apk.databinding.DialogReplyToReplyBottomSheetBinding
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.fragment.ReplyBottomSheetDialog
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnPublishClickListener
import com.example.c001apk.ui.fragment.minterface.IOnReplyClickListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.net.URLDecoder

class Reply2ReplyBottomSheetDialog : BottomSheetDialogFragment(), IOnReplyClickListener,
    IOnLikeClickListener, OnImageItemClickListener, IOnPublishClickListener {

    private lateinit var binding: DialogReplyToReplyBottomSheetBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: Reply2ReplyTotalAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var bottomSheetDialog: ReplyBottomSheetDialog

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
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }*/

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
                    binding.indicator.isIndeterminate = false
                    binding.indicator.visibility = View.GONE
                    showReplyErrorMessage()
                    return@observe
                } else if (!reply?.data.isNullOrEmpty()) {
                    if (!viewModel.isLoadMore)
                        viewModel.replyTotalList.clear()
                    for (element in reply?.data!!)
                        if (element.entityType == "feed_reply")
                            if (!BlackListUtil.checkUid(element.uid))
                                viewModel.replyTotalList.add(element)
                    binding.indicator.isIndeterminate = false
                    binding.indicator.visibility = View.GONE
                    mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
                } else {
                    mAdapter.setLoadState(mAdapter.LOADING_END)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                mAdapter.notifyDataSetChanged()
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
                        mAdapter.notifyDataSetChanged()
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
                        mAdapter.notifyDataSetChanged()
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
                                    "feed_reply",
                                    viewModel.id,
                                    viewModel.ruid,
                                    PrefManager.uid,
                                    viewModel.id,
                                    URLDecoder.decode(PrefManager.username, "UTF-8"),
                                    viewModel.uname,
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
                    if (viewModel.lastVisibleItemPosition == viewModel.replyTotalList.size
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING)
                        mAdapter.notifyDataSetChanged()
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.isNew = true
                        viewModel.getReplyTotal()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.replyTotalList.isNotEmpty())
                    viewModel.lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
            }
        })
    }

    private fun initData() {
        if (viewModel.replyTotalList.isEmpty()) {
            binding.indicator.visibility = View.VISIBLE
            binding.indicator.isIndeterminate = true
            viewModel.isEnd = false
            viewModel.isLoadMore = false
            viewModel.isNew = true
            viewModel.getReplyTotal()
        }
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)

        mAdapter =
            Reply2ReplyTotalAdapter(
                requireContext(),
                viewModel.fuid,
                viewModel.uid,
                viewModel.position,
                viewModel.replyTotalList
            )
        mLayoutManager = LinearLayoutManager(activity)
        mAdapter.setIOnLikeReplyListener(this)
        mAdapter.setIOnReplyClickListener(this)
        mAdapter.setOnImageItemClickListener(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    override fun onStart() {
        super.onStart()
        val view: FrameLayout =
            dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!
        view.layoutParams.height = -1
        view.layoutParams.width = -1
        val behavior = BottomSheetBehavior.from(view)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            behavior.halfExpandedRatio = 0.75F
            behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

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
            rid = viewModel.rid
            ruid = viewModel.ruid
            uname = viewModel.uname
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

    override fun onClick(
        nineGridView: NineGridImageView,
        imageView: ImageView,
        urlList: List<String>,
        position: Int
    ) {
        ImageUtil.startBigImgView(
            nineGridView,
            imageView,
            urlList,
            position
        )
    }

    override fun onPublish(message: String, replyAndForward: String) {
        viewModel.replyData["message"] = message
        viewModel.replyData["replyAndForward"] = replyAndForward
        viewModel.isPostReply = true
        viewModel.postReply()
    }

}