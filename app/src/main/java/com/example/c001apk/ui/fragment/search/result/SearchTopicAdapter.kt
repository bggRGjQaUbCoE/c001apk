package com.example.c001apk.ui.fragment.search.result

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.SearchTopicResponse
import com.example.c001apk.ui.activity.topic.TopicActivity
import com.example.c001apk.util.CountUtil
import com.example.c001apk.util.ImageShowUtil
import com.google.android.material.imageview.ShapeableImageView

class SearchTopicAdapter(
    private val searchList: List<SearchTopicResponse.Data>
) :
    RecyclerView.Adapter<SearchTopicAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val hotNum: TextView = view.findViewById(R.id.hotNum)
        val commentNum: TextView = view.findViewById(R.id.commentNum)
        val logo: ShapeableImageView = view.findViewById(R.id.logo)
        var entityType = ""
        var aliasTitle = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_topic, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(parent.context, TopicActivity::class.java)
            intent.putExtra(
                "title",
                if (viewHolder.entityType == "product")
                    viewHolder.aliasTitle
                else viewHolder.title.text
            )
            parent.context.startActivity(intent)
        }
        return viewHolder
    }

    override fun getItemCount() = searchList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val topic = searchList[position]
        holder.title.text = topic.title
        holder.hotNum.text = CountUtil.view(topic.hotNum) + "热度"
        holder.commentNum.text =
            if (topic.entityType == "topic") CountUtil.view(topic.commentnum) + "讨论"
            else CountUtil.view(topic.feedCommentNum) + "讨论"
        ImageShowUtil.showIMG(holder.logo, topic.logo)
        if (topic.entityType == "product")
            holder.aliasTitle = topic.aliasTitle
        holder.entityType = topic.entityType
    }

}