package com.example.c001apk.ui.fragment.feed

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.constant.Constants
import com.example.c001apk.databinding.FragmentFeedBinding
import com.example.c001apk.logic.model.CheckResponse
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.fragment.feed.total.Reply2ReplyBottomSheetDialog
import com.example.c001apk.util.CookieUtil
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.util.PrefManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.net.URLDecoder
import kotlin.concurrent.thread


class FeedFragment : Fragment(), IOnTotalReplyClickListener, IOnReplyClickListener {

    private lateinit var binding: FragmentFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[FeedContentViewModel::class.java] }
    private var id: String = ""
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var type = ""
    private var uname = ""
    private var ruid = ""
    private var rid = ""
    private var rPosition = 0

    //private var device: String? = null
    private lateinit var mAdapter: FeedContentAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var firstCompletelyVisibleItemPosition = -1
    private var lastVisibleItemPosition = -1

    companion object {
        @JvmStatic
        fun newInstance(id: String, uname: String) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    putString("ID", id)
                    putString("UNAME", uname)
                    //putString("DEVICE", device)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString("ID")!!
            uname = it.getString("UNAME")!!
            //device = it.getString("DEVICE")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBar()
        initView()
        initData()
        initRefresh()
        initScroll()

        binding.reply.setOnClickListener {
            rid = arguments?.getString("ID")!!
            uname = arguments?.getString("UNAME")!!
            type = "feed"
            initReply()
        }

        viewModel.feedData.observe(viewLifecycleOwner) { result ->
            val feed = result.getOrNull()
            if (feed != null) {
                if (viewModel.isRefreshing) {
                    viewModel.feedContentList.clear()
                    mAdapter.setLoadState(mAdapter.LOADING)
                    viewModel.getFeedReply()
                }
                if (viewModel.isRefreshing || viewModel.isLoadMore) {
                    viewModel.feedContentList.add(feed)
                }
                binding.indicator.isIndeterminate = false
                if (PrefManager.isLogin)
                    binding.reply.visibility = View.VISIBLE
                else
                    binding.reply.visibility = View.GONE
            } else {
                viewModel.isEnd = true
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(activity, "加载失败", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        }

        viewModel.feedReplyData.observe(viewLifecycleOwner) { result ->
            val reply = result.getOrNull()
            if (!reply.isNullOrEmpty()) {
                if (viewModel.isRefreshing) {
                    viewModel.feedReplyList.clear()
                }
                if (viewModel.isRefreshing || viewModel.isLoadMore) {
                    for (element in reply) {
                        if (element.entityType == "feed_reply")
                            viewModel.feedReplyList.add(element)
                    }
                }
                mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
            } else {
                viewModel.isEnd = true
                mAdapter.setLoadState(mAdapter.LOADING_END)
                result.exceptionOrNull()?.printStackTrace()
            }
            mAdapter.notifyDataSetChanged()
            viewModel.isLoadMore = false
            viewModel.isRefreshing = false
            binding.swipeRefresh.isRefreshing = false
        }

    }

    @SuppressLint("InflateParams")
    private fun initReply() {

        bottomSheetDialog = BottomSheetDialog(requireActivity())
        val view = LayoutInflater.from(requireActivity())
            .inflate(R.layout.dialog_reply_bottom_sheet, null, false)
        val editText: EditText = view.findViewById(R.id.editText)
        val publish: TextView = view.findViewById(R.id.publish)

        bottomSheetDialog.apply {
            setContentView(view)
            setCancelable(false)
            setCanceledOnTouchOutside(true)
            show()
            window?.apply {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        editText.hint = "回复: $uname"
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, 0)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            @SuppressLint("RestrictedApi")
            override fun afterTextChanged(p0: Editable?) {
                if (editText.text.toString().replace("\n", "").isEmpty()) {
                    publish.isClickable = false
                    publish.setTextColor(requireActivity().getColor(R.color.gray_bd))
                } else {
                    publish.isClickable = true
                    publish.setTextColor(
                        ThemeUtils.getThemeAttrColor(
                            requireActivity(),
                            com.drakeet.about.R.attr.colorPrimary
                        )
                    )
                    publish.setOnClickListener {
                        publish(editText.text.toString())
                    }
                }
            }
        })
    }

    private fun publish(content: String) {
        thread {
            try {
                val httpClient = OkHttpClient()
                val formBody: RequestBody = FormBody.Builder()
                    .add("message", content)
                    .build()

                val getRequest: Request = Request.Builder()
                    .addHeader("User-Agent", Constants.USER_AGENT)
                    .addHeader("X-Requested-With", Constants.REQUEST_WIDTH)
                    .addHeader("X-Sdk-Int", "33")
                    .addHeader("X-Sdk-Locale", "zh-CN")
                    .addHeader("X-App-Id", Constants.APP_ID)
                    .addHeader("X-App-Token", CookieUtil.token)
                    .addHeader("X-App-Version", "13.3.1")
                    .addHeader("X-App-Code", "2307121")
                    .addHeader("X-Api-Version", "13")
                    .addHeader("X-App-Device", CookieUtil.deviceCode)
                    .addHeader("X-Dark-Mode", "0")
                    .addHeader("X-App-Channel", "coolapk")
                    .addHeader("X-App-Mode", "universal")
                    .addHeader(
                        "Cookie",
                        "uid=${PrefManager.uid}; username=${PrefManager.username}; token=${PrefManager.token}"
                    )
                    .url("https://api.coolapk.com/v6/feed/reply?id=$rid&type=$type")
                    .post(formBody)
                    .build()

                val call: Call = httpClient.newCall(getRequest)

                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("Reply", "onFailure: ${e.message}")
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(call: Call, response: Response) {
                        val reply: CheckResponse = Gson().fromJson(
                            response.body!!.string(),
                            CheckResponse::class.java
                        )
                        if (reply.data?.messageStatus == 1) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(activity, "回复成功", Toast.LENGTH_SHORT).show()
                                bottomSheetDialog.cancel()
                                if (type == "feed") {
                                    viewModel.feedReplyList.add(
                                        0, TotalReplyResponse.Data(
                                            "feed_reply",
                                            id,
                                            ruid,
                                            PrefManager.uid,
                                            URLDecoder.decode(PrefManager.username, "UTF-8"),
                                            uname,
                                            content,
                                            "",
                                            null,
                                            (System.currentTimeMillis() / 1000).toString(),
                                            "0",
                                            "0",
                                            PrefManager.userAvatar,
                                            null,
                                            0
                                        )
                                    )
                                    mAdapter.notifyItemInserted(1)
                                    binding.recyclerView.scrollToPosition(1)
                                } else {
                                    viewModel.feedReplyList[rPosition - 1].replyRows!!.add(
                                        viewModel.feedReplyList[rPosition - 1].replyRows!!.size,
                                        HomeFeedResponse.ReplyRows(
                                            rid,
                                            PrefManager.uid,
                                            URLDecoder.decode(PrefManager.username, "UTF-8"),
                                            content,
                                            ruid,
                                            uname,
                                            null,
                                            ""
                                        )
                                    )
                                    mAdapter.notifyDataSetChanged()
                                }
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                Toast.makeText(activity, reply.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == viewModel.feedReplyList.size + 1) {
                        if (!viewModel.isEnd) {
                            mAdapter.setLoadState(mAdapter.LOADING)
                            viewModel.isLoadMore = true
                            viewModel.page++
                            viewModel.getFeedReply()
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.feedReplyList.isNotEmpty()) {
                    lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                    firstCompletelyVisibleItemPosition =
                        mLayoutManager.findFirstCompletelyVisibleItemPosition()
                }

                /*if (dy > 0 && binding.reply.visibility == View.VISIBLE) {
                    binding.reply.hide()
                } else if (dy < 0 && binding.reply.visibility != View.VISIBLE) {
                    binding.reply.show()
                }*/


            }
        })
    }

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                requireActivity(),
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.isIndeterminate = false
            refreshData()
        }
    }

    private fun initData() {
        if (viewModel.feedContentList.isEmpty()) {
            viewModel.id = id
            refreshData()
        }
    }

    private fun refreshData() {
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.getFeed()
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = FeedContentAdapter(
            requireActivity(),
            viewModel.feedContentList,
            viewModel.feedReplyList
        )
        mAdapter.setIOnTotalReplyClickListener(this)
        mLayoutManager = LinearLayoutManager(activity)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    private fun initBar() {
        binding.toolBar.apply {
            title = "动态"
            //title = uname
            //subtitle = device
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                requireActivity().finish()
            }
        }
    }

    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    override fun onShowTotalReply(position: Int, uid: String, id: String) {
        val mBottomSheetDialogFragment = Reply2ReplyBottomSheetDialog.newInstance(position, uid, id)
        mBottomSheetDialogFragment.show(childFragmentManager, "Dialog")
    }

    override fun onResume() {
        super.onResume()

        IOnReplyClickContainer.controller = this

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
            this.rPosition = rPosition
            this.rid = id
            this.ruid = uid
            this.uname = uname
            this.type = type
            initReply()
        }
    }

}