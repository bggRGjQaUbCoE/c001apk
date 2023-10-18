package com.example.c001apk.ui.fragment.home.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R

class AppListAdapter(private val appList: List<AppItem>) :
    RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var icon: ImageView = itemView.findViewById(R.id.appIcon)
        var appName: TextView = itemView.findViewById(R.id.appName)
        var packageName: TextView = itemView.findViewById(R.id.packageName)
        var versionName: TextView = itemView.findViewById(R.id.appVersion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            Toast.makeText(parent.context, viewHolder.packageName.text, Toast.LENGTH_SHORT).show()
        }
        return viewHolder
    }

    override fun getItemCount() = appList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = appList[position]
        holder.icon.setImageDrawable(app.icon)
        holder.appName.text = app.appName
        holder.packageName.text = app.packageName
        holder.versionName.text = app.versionName

    }

}