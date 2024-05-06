package com.example.c001apk.ui.message

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.databinding.ItemMessageMessBinding
import com.example.c001apk.ui.messagedetail.MessageActivity
import com.example.c001apk.util.CookieUtil.atcommentme
import com.example.c001apk.util.CookieUtil.atme
import com.example.c001apk.util.CookieUtil.contacts_follow
import com.example.c001apk.util.CookieUtil.feedlike
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager


class MessageThirdAdapter : RecyclerView.Adapter<MessageThirdAdapter.ThirdViewHolder>() {

    private val messTitle = listOf("@我的动态", "@我的评论", "我收到的赞", "好友关注", "私信")
    private val logoColorList = listOf("#2196f3", "#00bcd4", "#4caf50", "#f44336", "#ff9800")
    private val logoList = listOf(
        R.drawable.ic_at, R.drawable.ic_comment, R.drawable.ic_thumb,
        R.drawable.ic_add, R.drawable.ic_message1
    )
    private var badgeList: List<Int>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setBadgeList(badgeList: List<Int>) {
        if (badgeList.isNotEmpty()) {
            this.badgeList = badgeList
            notifyDataSetChanged()
        }
    }

    class ThirdViewHolder(val binding: ItemMessageMessBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            if (PrefManager.isLogin) {
                itemView.setOnClickListener {
                    binding.badge.isVisible = false
                    IntentUtil.startActivity<MessageActivity>(itemView.context) {
                        when (binding.title.text) {
                            "@我的动态" -> {
                                atme = null
                                putExtra("type", "atMe")
                            }

                            "@我的评论" -> {
                                atcommentme = null
                                putExtra("type", "atCommentMe")
                            }

                            "我收到的赞" -> {
                                feedlike = null
                                putExtra("type", "feedLike")
                            }

                            "好友关注" -> {
                                contacts_follow = null
                                putExtra("type", "contactsFollow")
                            }

                            "私信" -> putExtra("type", "list")
                        }
                    }
                }
            }
        }

        fun bind(badgeList: List<Int>?) {
            if (!badgeList.isNullOrEmpty())
                badgeList.let {
                    if (it[bindingAdapterPosition] > 0) {
                        binding.badge.isVisible = true
                        binding.badge.text = it[bindingAdapterPosition].toString()
                    } else
                        binding.badge.isVisible = false
                }
            binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int) = position

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ThirdViewHolder {
        val binding =
            ItemMessageMessBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.title.text = messTitle[position]
        binding.logoCover.setBackgroundColor(Color.parseColor(logoColorList[position]))
        binding.logo.setBackgroundDrawable(parent.context.getDrawable(logoList[position]))
        return ThirdViewHolder(binding)
    }

    override fun getItemCount() = 4

    override fun onBindViewHolder(holder: ThirdViewHolder, position: Int) {
        holder.bind(badgeList)
    }
}