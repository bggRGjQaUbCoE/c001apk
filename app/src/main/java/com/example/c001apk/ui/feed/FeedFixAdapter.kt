package com.example.c001apk.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.BR
import com.example.c001apk.R
import com.example.c001apk.databinding.ItemTopBinding
import com.example.c001apk.ui.feed.reply.ReplyRefreshListener

class FeedFixAdapter(
    private val replyNum: String,
    private val replyRefreshListener: ReplyRefreshListener
) :
    RecyclerView.Adapter<FeedFixAdapter.ViewHolder>() {

    private var listType: String = "lastupdate_desc"

    fun setListType(listType: String) {
        this.listType = listType
        notifyItemChanged(0)
    }

    class ViewHolder(
        val binding: ItemTopBinding,
        private val replyNum: String,
        private val replyRefreshListener: ReplyRefreshListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listType: String) {
            when (listType) {
                "lastupdate_desc" -> binding.buttonToggle.check(R.id.lastUpdate)
                "dateline_desc" -> binding.buttonToggle.check(R.id.dateLine)
                "popular" -> binding.buttonToggle.check(R.id.popular)
                "" -> binding.buttonToggle.check(R.id.author)
            }
            binding.setVariable(BR.replyNum, replyNum)
            binding.setVariable(BR.listener, replyRefreshListener)
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTopBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), replyNum, replyRefreshListener
        )
    }

    override fun getItemCount() = 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listType)
    }

}