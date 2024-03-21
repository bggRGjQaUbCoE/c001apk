package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.c001apk.R
import com.google.android.material.progressindicator.CircularProgressIndicator

class FooterAdapter(private val listener: FooterListener) :
    RecyclerView.Adapter<FooterAdapter.FooterViewHolder>() {

    private var footerState: FooterState = FooterState.LoadingDone

    fun setLoadState(footerState: FooterState) {
        this.footerState = footerState
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
                if (footerState == FooterState.LoadingReply) {
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

        when (footerState) {
            FooterState.Loading, FooterState.LoadingReply -> {
                holder.indicator.isVisible = true
                holder.indicator.isIndeterminate = true
                holder.noMore.visibility = View.INVISIBLE
                holder.retry.isVisible = false

            }

            FooterState.LoadingDone -> {
                holder.indicator.visibility = View.INVISIBLE
                holder.indicator.isIndeterminate = false
                holder.noMore.visibility = View.INVISIBLE
                holder.retry.isVisible = false
            }

            FooterState.LoadingEnd -> {
                holder.indicator.visibility = View.INVISIBLE
                holder.indicator.isIndeterminate = false
                holder.noMore.isVisible = true
                holder.noMore.text = "没有更多了"
                holder.retry.isVisible = false
            }

            is FooterState.LoadingError -> {
                holder.indicator.visibility = View.INVISIBLE
                holder.indicator.isIndeterminate = false
                holder.noMore.text = (footerState as FooterState.LoadingError).errMsg
                holder.noMore.isVisible = true
                holder.retry.isVisible = true
            }

        }

    }

    override fun getItemCount() = 1

    interface FooterListener {
        fun onReLoad()
    }
}