package com.example.c001apk.ui.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.c001apk.R
import com.example.c001apk.databinding.ItemMessageMineBinding
import com.example.c001apk.ui.collection.CollectionActivity
import com.example.c001apk.ui.follow.FFFListActivity
import com.example.c001apk.ui.history.HistoryActivity
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager


class MessageSecondAdapter :
    RecyclerView.Adapter<MessageSecondAdapter.SecondViewHolder>() {

    class SecondViewHolder(val binding: ItemMessageMineBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.apply {
                localFavLayout.setOnClickListener(this@SecondViewHolder)
                historyLayout.setOnClickListener(this@SecondViewHolder)
                if (PrefManager.isLogin) {
                    freqLayout.setOnClickListener(this@SecondViewHolder)
                    favLayout.setOnClickListener(this@SecondViewHolder)
                    likeLayout.setOnClickListener(this@SecondViewHolder)
                    replyLayout.setOnClickListener(this@SecondViewHolder)
                }
            }
        }

        fun bind() {
            binding.executePendingBindings()
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.localFavLayout ->
                    IntentUtil.startActivity<HistoryActivity>(itemView.context) {
                        putExtra("type", "favorite")
                    }

                R.id.historyLayout ->
                    IntentUtil.startActivity<HistoryActivity>(itemView.context) {
                        putExtra("type", "browse")
                    }

                R.id.freqLayout ->
                    if (PrefManager.isLogin) {
                        IntentUtil.startActivity<FFFListActivity>(itemView.context) {
                            putExtra("type", "recentHistory")
                            putExtra("uid", PrefManager.uid)
                        }
                    }

                R.id.favLayout ->
                    if (PrefManager.isLogin)
                        IntentUtil.startActivity<CollectionActivity>(itemView.context) {
                        }

                R.id.likeLayout ->
                    if (PrefManager.isLogin) {
                        IntentUtil.startActivity<FFFListActivity>(itemView.context) {
                            putExtra("type", "like")
                            putExtra("uid", PrefManager.uid)
                        }
                    }

                R.id.replyLayout ->
                    if (PrefManager.isLogin) {
                        IntentUtil.startActivity<FFFListActivity>(itemView.context) {
                            putExtra("type", "reply")
                            putExtra("uid", PrefManager.uid)
                        }
                    }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): SecondViewHolder {
        val binding = ItemMessageMineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val lp = binding.root.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = true
        }
        return SecondViewHolder(binding)
    }

    override fun getItemCount() = 1

    override fun onBindViewHolder(holder: SecondViewHolder, position: Int) {
        holder.bind()
    }

}