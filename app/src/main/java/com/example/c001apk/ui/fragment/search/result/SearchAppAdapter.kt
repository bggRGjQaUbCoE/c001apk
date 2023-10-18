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
import com.example.c001apk.ui.activity.app.AppActivity
import com.example.c001apk.util.CountUtil
import com.example.c001apk.util.ImageShowUtil
import com.google.android.material.imageview.ShapeableImageView

class SearchAppAdapter(
    private val searchList: List<SearchTopicResponse.Data>
) :
    RecyclerView.Adapter<SearchAppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val hotNum: TextView = view.findViewById(R.id.hotNum)
        val commentNum: TextView = view.findViewById(R.id.commentNum)
        val logo: ShapeableImageView = view.findViewById(R.id.logo)
        var entityType = ""
        var apkName = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_topic, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(parent.context, AppActivity::class.java)
            intent.putExtra("id", viewHolder.apkName)
            parent.context.startActivity(intent)
        }
        return viewHolder
    }

    override fun getItemCount() = searchList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = searchList[position]
        holder.apkName = app.apkname
        holder.title.text = app.title
        holder.commentNum.text = CountUtil.view(app.commentnum) + "讨论"
        holder.hotNum.text = CountUtil.view(app.downnum) + "下载"
        ImageShowUtil.showIMG(holder.logo, app.logo)
    }

}