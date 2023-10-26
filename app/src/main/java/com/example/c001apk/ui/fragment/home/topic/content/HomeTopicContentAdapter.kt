package com.example.c001apk.ui.fragment.home.topic.content

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.topic.TopicActivity
import com.example.c001apk.util.CountUtil
import com.example.c001apk.util.ImageShowUtil
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.CircularProgressIndicator

class HomeTopicContentAdapter(
    private val searchList: List<HomeFeedResponse.Data>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var loadState = 2
    val LOADING = 1
    val LOADING_COMPLETE = 2
    val LOADING_END = 3

    @SuppressLint("NotifyDataSetChanged")
    fun setLoadState(loadState: Int) {
        this.loadState = loadState
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val hotNum: TextView = view.findViewById(R.id.hotNum)
        val commentNum: TextView = view.findViewById(R.id.commentNum)
        val logo: ShapeableImageView = view.findViewById(R.id.logo)
        var entityType = ""
        var aliasTitle = ""
    }

    class FootViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerLayout: FrameLayout = view.findViewById(R.id.footerLayout)
        val indicator: CircularProgressIndicator = view.findViewById(R.id.indicator)
        val noMore: TextView = view.findViewById(R.id.noMore)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) -1
        else 0

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_home_topic, parent, false)
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
                viewHolder
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_rv_footer, parent, false)
                FootViewHolder(view)
            }
        }

    }

    override fun getItemCount() = searchList.size + 1


    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FootViewHolder -> {
                when (loadState) {
                    LOADING -> {
                        holder.footerLayout.visibility = View.VISIBLE
                        holder.indicator.visibility = View.VISIBLE
                        holder.indicator.isIndeterminate = true
                        holder.noMore.visibility = View.GONE

                    }

                    LOADING_COMPLETE -> {
                        holder.footerLayout.visibility = View.GONE
                        holder.indicator.visibility = View.GONE
                        holder.indicator.isIndeterminate = false
                        holder.noMore.visibility = View.GONE
                    }

                    LOADING_END -> {
                        holder.footerLayout.visibility = View.VISIBLE
                        holder.indicator.visibility = View.GONE
                        holder.indicator.isIndeterminate = false
                        holder.noMore.visibility = View.VISIBLE
                    }

                    else -> {}
                }
            }

            is ViewHolder -> {
                val topic = searchList[position]
                holder.title.text = topic.title
                holder.hotNum.text = topic.hotNumTxt + "热度"
                holder.commentNum.text = topic.commentnumTxt + "讨论"
                ImageShowUtil.showIMG(holder.logo, topic.logo)
                if (topic.entityType == "product")
                    holder.aliasTitle = topic.aliasTitle
                holder.entityType = topic.entityType
            }
        }
    }


}