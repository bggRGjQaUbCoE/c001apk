package com.example.c001apk.ui.appupdate

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.c001apk.R
import com.example.c001apk.logic.model.UpdateCheckResponse
import com.example.c001apk.ui.app.AppActivity
import com.example.c001apk.util.AppUtils
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.http2https

class UpdateListAdapter(
    private val updateList: List<UpdateCheckResponse.Data>,
    private val viewModel: UpdateListViewModel,
) :
    RecyclerView.Adapter<UpdateListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var icon: ImageView = itemView.findViewById(R.id.appIcon)
        var appName: TextView = itemView.findViewById(R.id.appName)
        var codeName: TextView = itemView.findViewById(R.id.codeName)
        var size: TextView = itemView.findViewById(R.id.size)
        var updateLog: TextView = itemView.findViewById(R.id.updateLog)
        val bitType: TextView = itemView.findViewById(R.id.bitType)
        val btnUpdate: Button = itemView.findViewById(R.id.btnUpdate)
        var expand = false
        var packageName: String = ""
        var apkversioncode: String = ""
        var apkversionname: String = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_app_update, parent, false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            IntentUtil.startActivity<AppActivity>(holder.itemView.context) {
                putExtra("id", holder.packageName)
            }
        }
        holder.btnUpdate.setOnClickListener {
            viewModel.packageName = holder.packageName
            viewModel.versionCode = holder.apkversioncode
            viewModel.appName = holder.appName.text.toString()
            viewModel.versionName = holder.apkversionname
            viewModel.onGetDownloadLink()
        }
        holder.updateLog.apply {
            maxLines = if (holder.expand) Int.MAX_VALUE else 5
            setOnClickListener {
                holder.expand = !holder.expand
                maxLines = if (holder.expand) Int.MAX_VALUE else 5
            }
        }
        return holder
    }

    override fun getItemCount() = updateList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = updateList[position]
        Glide.with(holder.itemView.context).load(app.logo.http2https).into(holder.icon)
        holder.appName.text = app.title
        holder.appName.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT // 刷新宽度
        if (app.localVersionName == null || app.localVersionName == "null") {
            app.localVersionName =
                AppUtils.getAppVersionName(holder.itemView.context, app.packageName)
        }
        if (app.localVersionCode == null || app.localVersionCode == -1L) {
            app.localVersionCode =
                AppUtils.getAppVersionCode(holder.itemView.context, app.packageName)
        }
        holder.codeName.text =
            "${app.localVersionName}(${app.localVersionCode}) > ${app.apkversionname}(${app.apkversioncode})"
        holder.size.text = DateUtils.fromToday(app.lastupdate) + " " + app.apksize
        holder.updateLog.text = app.changelog
        holder.bitType.text = when (app.pkg_bit_type) {
            1 -> "32位"
            2, 3 -> "64位"
            else -> ""
        }
        holder.packageName = app.packageName
        holder.apkversioncode = app.apkversioncode.toString()
        holder.apkversionname = app.apkversionname
    }

}