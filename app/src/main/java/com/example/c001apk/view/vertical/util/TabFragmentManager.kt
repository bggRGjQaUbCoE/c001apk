package com.example.c001apk.view.vertical.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.c001apk.view.vertical.VerticalTabLayout
import com.example.c001apk.view.vertical.widget.TabView

/**
 * Created by chqiu on 2017/1/16.
 */
class TabFragmentManager(
    private var mManager: FragmentManager?,
    private var mFragments: List<Fragment>?,
    private var mTabLayout: VerticalTabLayout?
) {
    private var mContainerResid = 0
    private var mListener: VerticalTabLayout.OnTabSelectedListener?

    init {
        mListener = OnFragmentTabSelectedListener()
        mTabLayout!!.addOnTabSelectedListener(mListener)
    }

    constructor(
        manager: FragmentManager?,
        containerResid: Int,
        fragments: List<Fragment>?,
        tabLayout: VerticalTabLayout?
    ) : this(manager, fragments, tabLayout) {
        mContainerResid = containerResid
        changeFragment()
    }

    fun changeFragment() {
        val ft = mManager!!.beginTransaction()
        val position = mTabLayout!!.selectedTabPosition
        val addedFragments = mManager!!.fragments
        for (i in mFragments!!.indices) {
            val fragment = mFragments!![i]
            if ((addedFragments == null || !addedFragments.contains(fragment)) && mContainerResid != 0) {
                ft.add(mContainerResid, fragment)
            }
            if (mFragments!!.size > position && i == position || mFragments!!.size <= position && i == mFragments!!.size - 1) {
                ft.show(fragment)
            } else {
                ft.hide(fragment)
            }
        }
        ft.commit()
        mManager!!.executePendingTransactions()
    }

    fun detach() {
        val ft = mManager!!.beginTransaction()
        for (fragment in mFragments!!) {
            ft.remove(fragment)
        }
        ft.commit()
        mManager!!.executePendingTransactions()
        mManager = null
        mFragments = null
        mTabLayout!!.removeOnTabSelectedListener(mListener)
        mListener = null
        mTabLayout = null
    }

    private inner class OnFragmentTabSelectedListener : VerticalTabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabView, position: Int) {
            changeFragment()
        }

        override fun onTabReselected(tab: TabView, position: Int) {}
    }
}