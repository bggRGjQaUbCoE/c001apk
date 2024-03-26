package com.example.c001apk.ui.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.c001apk.R
import com.example.c001apk.databinding.ItemMessageFffBinding
import com.example.c001apk.ui.follow.FFFListActivity
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager


class MessageFirstAdapter
    : RecyclerView.Adapter<MessageFirstAdapter.FirstViewHolder>() {

    private var ffflist: List<String>? = null

    fun setFFFList(ffflist: List<String>) {
        if (ffflist.isNotEmpty()) {
            this.ffflist = ffflist
            notifyItemChanged(0)
        }
    }

    private val fffTitle = ArrayList<String>()

    init {
        fffTitle.apply {
            add("动态")
            add("关注")
            add("粉丝")
        }
    }

    class FirstViewHolder(val binding: ItemMessageFffBinding, private val fffTitle: List<String>) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            if (PrefManager.isLogin) {
                binding.feedLayout.setOnClickListener(this)
                binding.followLayout.setOnClickListener(this)
                binding.fansLayout.setOnClickListener(this)
            }
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.feedLayout ->
                    IntentUtil.startActivity<FFFListActivity>(itemView.context) {
                        putExtra("uid", PrefManager.uid)
                        putExtra("type", "feed")
                    }

                R.id.followLayout ->
                    IntentUtil.startActivity<FFFListActivity>(itemView.context) {
                        putExtra("uid", PrefManager.uid)
                        putExtra("type", "follow")
                    }

                R.id.fansLayout ->
                    IntentUtil.startActivity<FFFListActivity>(itemView.context) {
                        putExtra("uid", PrefManager.uid)
                        putExtra("type", "fans")
                    }
            }
        }

        fun bind(fffList: List<String>?) {
            if (!fffList.isNullOrEmpty()) {
                fffList.let {
                    binding.apply {
                        feedCount.text = it[0]
                        feedTitle.text = fffTitle[0]
                        followCount.text = it[1]
                        followTitle.text = fffTitle[1]
                        fansCount.text = it[2]
                        fansTitle.text = fffTitle[2]
                    }
                    binding.executePendingBindings()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirstViewHolder {
        val binding = ItemMessageFffBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val lp = binding.root.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = true
        }
        return FirstViewHolder(binding, fffTitle)
    }

    override fun getItemCount() = 1

    override fun onBindViewHolder(holder: FirstViewHolder, position: Int) {
        holder.bind(ffflist)
    }

}