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


class MessageThirdAdapter
    : RecyclerView.Adapter<MessageThirdAdapter.ThirdViewHolder>() {

    private var badgeList: List<Int>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setBadgeList(badgeList: List<Int>) {
        if (badgeList.isNotEmpty()) {
            this.badgeList = badgeList
            notifyDataSetChanged()
        }
    }

    private val messTitle = ArrayList<String>()
    private val logoList = ArrayList<Int>()
    private val logoColorList = ArrayList<String>()

    init {
        messTitle.apply {
            add("@我的动态")
            add("@我的评论")
            add("我收到的赞")
            add("好友关注")
            add("私信")
        }

        logoColorList.apply {
            add("#2196f3")
            add("#00bcd4")
            add("#4caf50")
            add("#f44336")
            add("#ff9800")
        }

        logoList.apply {
            add(R.drawable.ic_at)
            add(R.drawable.ic_comment)
            add(R.drawable.ic_thumb)
            add(R.drawable.ic_add)
            add(R.drawable.ic_message1)
        }
    }

    class ThirdViewHolder(
        val binding: ItemMessageMessBinding,
        private val messTitle: List<String>,
        private val logoList: List<Int>,
        private val logoColorList: List<String>
    ) :
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
            binding.title.text = messTitle[bindingAdapterPosition]
            binding.logoCover.setBackgroundColor(Color.parseColor(logoColorList[bindingAdapterPosition]))
            binding.logo.setBackgroundDrawable(itemView.context.getDrawable(logoList[bindingAdapterPosition]))
            if (!badgeList.isNullOrEmpty())
                badgeList.let {
                    if (it[bindingAdapterPosition] > 0) {
                        binding.badge.isVisible = true
                        binding.badge.text =
                            if (it[bindingAdapterPosition] > 99) "99+"
                            else it[bindingAdapterPosition].toString()
                    } else
                        binding.badge.isVisible = false
                }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThirdViewHolder {
        return ThirdViewHolder(
            ItemMessageMessBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            messTitle, logoList, logoColorList
        )
    }

    override fun getItemCount() = 4

    override fun onBindViewHolder(holder: ThirdViewHolder, position: Int) {
        holder.bind(badgeList)
    }
}