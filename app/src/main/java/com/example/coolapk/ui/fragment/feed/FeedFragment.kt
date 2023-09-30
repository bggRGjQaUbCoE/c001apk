package com.example.coolapk.ui.fragment.feed

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
import com.example.coolapk.R
import com.example.coolapk.databinding.FragmentFeedBinding
import com.example.coolapk.util.LinearItemDecoration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FeedFragment : Fragment() {

    private lateinit var binding: FragmentFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[FeedContentViewModel::class.java] }
    private var id: String = ""
    private var uname: String = ""
    private var device: String? = null
    private lateinit var mAdapter: FeedContentAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    companion object {
        @JvmStatic
        fun newInstance(id: String, uname: String, device: String?) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    putString("ID", id)
                    putString("UNAME", uname)
                    putString("DEVICE", device)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString("ID")!!
            uname = it.getString("UNAME")!!
            device = it.getString("DEVICE")
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

        viewModel.feedData.observe(viewLifecycleOwner) { result ->
            val feed = result.getOrNull()
            if (feed != null) {
                if (viewModel.isRefreshing)
                    viewModel.feedContentList.clear()
                if (viewModel.isRefreshing || viewModel.isLoadMore) {
                    viewModel.feedContentList.add(feed)
                }
                mAdapter.notifyDataSetChanged()
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
            } else {
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(activity, "null", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        }

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
        viewModel.id = id
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        lifecycleScope.launch {
            delay(500)
            viewModel.getFeed()
        }
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = FeedContentAdapter(requireActivity(), viewModel.feedContentList)
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

}