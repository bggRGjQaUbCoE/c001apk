package com.example.c001apk.ui.message

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.databinding.ItemMessageMessBinding
import com.example.c001apk.ui.messagedetail.MessageActivity
import com.example.c001apk.util.CookieUtil.atcommentme
import com.example.c001apk.util.CookieUtil.atme
import com.example.c001apk.util.CookieUtil.contacts_follow
import com.example.c001apk.util.CookieUtil.feedlike
import com.example.c001apk.util.CookieUtil.message
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager


class MessageThirdAdapter
    : RecyclerView.Adapter<MessageThirdAdapter.ThirdViewHolder>() {

    private var badgeList: List<Int>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setBadgeList(badgeList: List<Int>) {
        this.badgeList = badgeList
        notifyDataSetChanged()
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

    inner class ThirdViewHolder(val binding: ItemMessageMessBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            if (PrefManager.isLogin) {
                itemView.setOnClickListener {
                    binding.badge.visibility = View.GONE
                    IntentUtil.startActivity<MessageActivity>(itemView.context) {
                        when (binding.title.text) {
                            "@我的动态" -> {
                                atme = 0
                                putExtra("type", "atMe")
                            }

                            "@我的评论" -> {
                                atcommentme = 0
                                putExtra("type", "atCommentMe")
                            }

                            "我收到的赞" -> {
                                feedlike = 0
                                putExtra("type", "feedLike")
                            }

                            "好友关注" -> {
                                contacts_follow = 0
                                putExtra("type", "contactsFollow")
                            }

                            "私信" -> {
                                message = 0
                                putExtra("type", "list")
                            }
                        }
                    }

                }
            }
            binding.title.text = messTitle[bindingAdapterPosition]
            binding.logoCover.setBackgroundColor(Color.parseColor(logoColorList[bindingAdapterPosition]))
            binding.logo.setBackgroundDrawable(itemView.context.getDrawable(logoList[bindingAdapterPosition]))
            badgeList?.let {
                if (it[bindingAdapterPosition] != 0) {
                    binding.badge.visibility = View.VISIBLE
                    binding.badge.text =
                        if (it[bindingAdapterPosition] > 99) "99+"
                        else it[bindingAdapterPosition].toString()
                } else
                    binding.badge.visibility = View.GONE
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThirdViewHolder {
        return ThirdViewHolder(
            ItemMessageMessBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = 4

    override fun onBindViewHolder(holder: ThirdViewHolder, position: Int) {
        holder.bind()
    }
}