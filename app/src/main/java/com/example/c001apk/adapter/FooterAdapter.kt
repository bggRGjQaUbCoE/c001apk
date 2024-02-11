package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.c001apk.R
import com.google.android.material.progressindicator.CircularProgressIndicator

class FooterAdapter(private val listener: FooterListener) :
    RecyclerView.Adapter<FooterAdapter.FooterViewHolder>() {

    enum class LoadState {
        LOADING,
        LOADING_COMPLETE,
        LOADING_END,
        LOADING_ERROR
    }

    private var loadState = LoadState.LOADING_COMPLETE
    private var errorMessage: String? = null

    fun setLoadState(loadState: LoadState, errorMessage: String?) {
        this.loadState = loadState
        this.errorMessage = errorMessage
    }

    class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerLayout: FrameLayout = view.findViewById(R.id.footerLayout)
        val indicator: CircularProgressIndicator = view.findViewById(R.id.indicator)
        val noMore: TextView = view.findViewById(R.id.noMore)
        val retry: Button = view.findViewById(R.id.retry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FooterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rv_footer, parent, false)
        val viewHolder = FooterViewHolder(view)
        viewHolder.retry.setOnClickListener {
            listener.onReLoad()
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: FooterViewHolder, position: Int) {
        val lp = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = true
        }

        when (loadState) {
            LoadState.LOADING -> {
                holder.footerLayout.visibility = View.VISIBLE
                holder.indicator.visibility = View.VISIBLE
                holder.indicator.isIndeterminate = true
                holder.noMore.visibility = View.INVISIBLE
                holder.retry.visibility = View.GONE

            }

            LoadState.LOADING_COMPLETE -> {
                holder.footerLayout.visibility = View.INVISIBLE
                holder.indicator.visibility = View.INVISIBLE
                holder.indicator.isIndeterminate = false
                holder.noMore.visibility = View.INVISIBLE
                holder.retry.visibility = View.GONE
            }

            LoadState.LOADING_END -> {
                holder.footerLayout.visibility = View.VISIBLE
                holder.indicator.visibility = View.INVISIBLE
                holder.indicator.isIndeterminate = false
                holder.noMore.visibility = View.VISIBLE
                holder.noMore.text = "没有更多了"
                holder.retry.visibility = View.GONE
            }

            LoadState.LOADING_ERROR -> {
                holder.footerLayout.visibility = View.VISIBLE
                holder.indicator.visibility = View.INVISIBLE
                holder.indicator.isIndeterminate = false
                holder.noMore.text = errorMessage
                holder.noMore.visibility = View.VISIBLE
                holder.retry.visibility = View.VISIBLE
            }

        }

    }

    override fun getItemCount() = 1

    interface FooterListener {
        fun onReLoad()
    }
}