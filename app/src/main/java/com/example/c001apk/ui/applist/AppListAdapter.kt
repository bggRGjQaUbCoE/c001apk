package com.example.c001apk.ui.applist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.c001apk.R
import com.example.c001apk.logic.model.AppItem
import com.example.c001apk.ui.app.AppActivity
import com.example.c001apk.util.AppUtils
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.LocalAppIcon

class AppListAdapter :
    ListAdapter<AppItem, AppListAdapter.ViewHolder>(AppListDiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var icon: ImageView = itemView.findViewById(R.id.appIcon)
        var appName: TextView = itemView.findViewById(R.id.appName)
        var packageName: TextView = itemView.findViewById(R.id.packageName)
        var versionName: TextView = itemView.findViewById(R.id.appVersion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            IntentUtil.startActivity<AppActivity>(parent.context) {
                putExtra("id", viewHolder.packageName.text)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = currentList[position]
        Glide.with(holder.icon).load(LocalAppIcon(app.packageName)).into(holder.icon)
        if (app.appName.isEmpty())
            app.appName = AppUtils.getAppName(holder.itemView.context, app.packageName)
        holder.appName.text = app.appName
        holder.packageName.text = app.packageName
        holder.versionName.text = app.versionName
    }

}

class AppListDiffCallback : DiffUtil.ItemCallback<AppItem>() {
    override fun areItemsTheSame(
        oldItem: AppItem,
        newItem: AppItem
    ): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    override fun areContentsTheSame(
        oldItem: AppItem,
        newItem: AppItem
    ): Boolean {
        return oldItem.packageName == newItem.packageName
    }
}