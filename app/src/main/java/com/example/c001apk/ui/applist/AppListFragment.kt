package com.example.c001apk.ui.applist

import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.PlaceHolderAdapter
import com.example.c001apk.databinding.BaseRefreshRecyclerviewBinding
import com.example.c001apk.ui.appupdate.AppUpdateActivity
import com.example.c001apk.ui.base.BaseViewFragment
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.ui.main.INavViewContainer
import com.example.c001apk.ui.main.IOnBottomClickContainer
import com.example.c001apk.ui.main.IOnBottomClickListener
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.setSpaceFooterView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppListFragment : BaseViewFragment<AppListViewModel>(), IOnTabClickListener,
    IOnBottomClickListener {

    override val viewModel by viewModels<AppListViewModel>()
    private lateinit var appsAdapter: AppListAdapter
    private val placeHolderAdapter = PlaceHolderAdapter()
    private lateinit var fab: FloatingActionButton
    private val fabViewBehavior by lazy { HideBottomViewOnScrollBehavior<FloatingActionButton>() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BaseRefreshRecyclerviewBinding.inflate(inflater, container, false)
        fab = FloatingActionButton(requireContext())
        initFab()
        _binding?.root?.addView(fab)
        return binding.root
    }

    override fun initAdapter() {
        appsAdapter = AppListAdapter()
        mAdapter = ConcatAdapter(HeaderAdapter(), appsAdapter)
    }

    override fun fetchData() {
        viewModel.getItems(requireContext())
    }

    override fun initView() {
        super.initView()
        binding.vfContainer.setOnDisplayedChildChangedListener {
            binding.recyclerView.setSpaceFooterView(placeHolderAdapter)
        }
    }

    override fun initObserve() {
        super.initObserve()

        viewModel.items.observe(viewLifecycleOwner) {
            appsAdapter.submitList(it)
            binding.swipeRefresh.isRefreshing = false
            if (binding.vfContainer.displayedChild != it.size)
                binding.vfContainer.displayedChild = it.size
        }

        viewModel.setFab.observe(viewLifecycleOwner) {
            if (it)
                fab.isVisible = true
        }
    }

    private fun initFab() {
        fab.apply {
            setImageResource(R.drawable.ic_update)
            if (SDK_INT >= 26)
                tooltipText = getString(R.string.update)
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                behavior = fabViewBehavior
            }
            setOnClickListener {
                IntentUtil.startActivity<AppUpdateActivity>(requireContext()) {
                    putParcelableArrayListExtra("list", viewModel.appsUpdate)
                }
            }
            isVisible = false
        }
        ViewCompat.setOnApplyWindowInsetsListener(fab) { _, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            fab.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                rightMargin = 25.dp
                bottomMargin =
                    if (isPortrait)
                        navigationBars.bottom + 105.dp
                    else 25.dp
            }
            insets
        }
    }

    override fun onScrolled(dy: Int) {
        if (dy > 0) {
            (activity as? INavViewContainer)?.hideNavigationView()
        } else if (dy < 0) {
            (activity as? INavViewContainer)?.showNavigationView()
        }
    }


    override fun onResume() {
        super.onResume()
        (parentFragment as? IOnTabClickContainer)?.tabController = this
        (activity as? IOnBottomClickContainer)?.controller = this
    }

    override fun onPause() {
        super.onPause()
        (parentFragment as? IOnTabClickContainer)?.tabController = null
        (activity as? IOnBottomClickContainer)?.controller = null
    }

    override fun onReturnTop() {
        onReturnTop(true)
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        binding.swipeRefresh.isRefreshing = true
        binding.recyclerView.scrollToPosition(0)
        refreshData()
    }

}