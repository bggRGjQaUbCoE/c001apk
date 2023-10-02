package com.example.c001apk.ui.fragment.feed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentFeedBinding
import com.example.c001apk.ui.fragment.feed.total.Reply2ReplyBottomSheetDialog
import com.example.c001apk.util.LinearItemDecoration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FeedFragment : Fragment(), IOnTotalReplyClickListener {

    private lateinit var binding: FragmentFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[FeedContentViewModel::class.java] }
    private var id: String = ""

    //private var uname: String = ""
    //private var device: String? = null
    private lateinit var mAdapter: FeedContentAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var firstCompletelyVisibleItemPosition = 0
    private var lastVisibleItemPosition = 0

    companion object {
        @JvmStatic
        fun newInstance(id: String) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    putString("ID", id)
                    //putString("UNAME", uname)
                    //putString("DEVICE", device)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString("ID")!!
            //uname = it.getString("UNAME")!!
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

        viewModel.feedData.observe(viewLifecycleOwner) { result ->
            val feed = result.getOrNull()
            if (feed != null) {
                if (viewModel.isRefreshing) {
                    viewModel.feedContentList.clear()
                    viewModel.getFeedReply()
                }
                if (viewModel.isRefreshing || viewModel.isLoadMore) {
                    viewModel.feedContentList.add(feed)
                }
                mAdapter.notifyDataSetChanged()
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
                    /*viewModel.feedReplyList.addAll(reply)
                    for (i in 0 until viewModel.feedReplyList.size) {
                        if (viewModel.feedReplyList[i].entityTemplate != "feed_reply") {
                            viewModel.feedReplyList.removeAt(i)
                            //mAdapter.notifyItemRemoved(i)
                            break
                        }
                    }*/
                    for (i in 0 until reply.size){
                        if (reply[i].entityType == "feed_reply")
                            viewModel.feedReplyList.add(reply[i])
                    }
                    mAdapter.notifyDataSetChanged()
                }
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
            } else {
                viewModel.isEnd = true
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                //Toast.makeText(activity, "没有更多了", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == viewModel.feedReplyList.size) {
                        if (!viewModel.isEnd) {
                            viewModel.isLoadMore = true
                            viewModel.page++
                            viewModel.getFeedReply()
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                firstCompletelyVisibleItemPosition =
                    mLayoutManager.findFirstCompletelyVisibleItemPosition()
            }
        })
    }

    private fun initRefresh() {
        //binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.black)
        /*binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                requireActivity(),
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )*/
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }

    private fun initData() {
        if (viewModel.feedContentList.isEmpty())
            refreshData()
    }

    private fun refreshData() {
        binding.swipeRefresh.isRefreshing = true
        viewModel.id = id //"49715174"
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        lifecycleScope.launch {
            delay(500)
            viewModel.getFeed()
        }
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

    override fun onShowTotalReply(id: String) {
        val mBottomSheetDialogFragment: Reply2ReplyBottomSheetDialog =
            Reply2ReplyBottomSheetDialog.newInstance(id)

        mBottomSheetDialogFragment.show(childFragmentManager, "Dialog")
    }

}