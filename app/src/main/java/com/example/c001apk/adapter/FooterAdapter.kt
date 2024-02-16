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
        LOADING_ERROR,
        LOADING_REPLY
    }

    private var loadState = LoadState.LOADING_COMPLETE
    private var errorMessage: String? = null

    fun setLoadState(loadState: LoadState, errorMessage: String?) {
        this.loadState = loadState
        this.errorMessage = errorMessage
        notifyItemChanged(0)
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
        } else {
            holder.footerLayout.layoutParams =
                if (loadState == LoadState.LOADING_REPLY) {
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                } else
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
        }

        when (loadState) {

            LoadState.LOADING, LoadState.LOADING_REPLY -> {
                holder.indicator.visibility = View.VISIBLE
                holder.indicator.isIndeterminate = true
                holder.noMore.visibility = View.INVISIBLE
                holder.retry.visibility = View.GONE

            }

            LoadState.LOADING_COMPLETE -> {
                holder.indicator.visibility = View.INVISIBLE
                holder.indicator.isIndeterminate = false
                holder.noMore.visibility = View.INVISIBLE
                holder.retry.visibility = View.GONE
            }

            LoadState.LOADING_END -> {
                holder.indicator.visibility = View.INVISIBLE
                holder.indicator.isIndeterminate = false
                holder.noMore.visibility = View.VISIBLE
                holder.noMore.text = "没有更多了"
                holder.retry.visibility = View.GONE
            }

            LoadState.LOADING_ERROR -> {
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