package com.example.c001apk.ui.fragment.search.result

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.util.PubDateUtil
import com.example.c001apk.R
import com.example.c001apk.logic.model.SearchUserResponse
import com.example.c001apk.ui.activity.feed.FeedActivity
import com.example.c001apk.ui.activity.user.UserActivity
import com.example.c001apk.util.ImageShowUtil

class SearchUserAdapter(
    private val mContext: Context,
    private val searchList: List<SearchUserResponse.Data>
) :
    RecyclerView.Adapter<SearchUserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uname: TextView = view.findViewById(R.id.uname)
        val follow: TextView = view.findViewById(R.id.follow)
        val fans: TextView = view.findViewById(R.id.fans)
        val act: TextView = view.findViewById(R.id.act)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        var uid = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_user, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(parent.context, UserActivity::class.java)
            intent.putExtra("id", viewHolder.uname.text)
            parent.context.startActivity(intent)
        }
        return viewHolder
    }

    override fun getItemCount() = searchList.size

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = searchList[position]
        holder.uid = user.uid
        holder.uname.text = user.username
        holder.follow.text = "${user.follow}关注"
        holder.fans.text = "${user.fans}粉丝"
        holder.act.text = PubDateUtil.time(user.logintime) + "活跃"
        ImageShowUtil.showAvatar(holder.avatar, user.userAvatar)
    }

}