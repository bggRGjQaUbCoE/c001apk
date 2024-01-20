package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.ThemeUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R

class BrandLabelAdapter(
    private val context: Context,
    private val list: List<String>
) : RecyclerView.Adapter<BrandLabelAdapter.ViewHolder>() {

    private var onLabelClickListener: OnLabelClickListener? = null

    fun setOnLabelClickListener(onLabelClickListener: OnLabelClickListener) {
        this.onLabelClickListener = onLabelClickListener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val indicator: View = view.findViewById(R.id.indicator)
    }

    private var selectedPosition = 0

    fun setCurrentPosition(selectedPosition: Int) {
        this.selectedPosition = selectedPosition
    }

    override fun getItemCount() = list.size

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.item_brand_label, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener { _: View ->
            if (viewHolder.bindingAdapterPosition != selectedPosition) {
                selectedPosition = viewHolder.bindingAdapterPosition
                onLabelClickListener?.onLabelClicked(viewHolder.bindingAdapterPosition)
            }
            notifyDataSetChanged()
        }
        return viewHolder
    }

    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val text = list[position]
        holder.title.text = text
        if (selectedPosition == position) {
            holder.title.setTextColor(
                ThemeUtils.getThemeAttrColor(
                    context,
                    rikka.preference.simplemenu.R.attr.colorPrimary
                )
            )
            holder.title.setBackgroundColor(
                context.getColor(R.color.home_card_background_color)
            )
            holder.indicator.setBackgroundColor(
                ThemeUtils.getThemeAttrColor(
                    context,
                    rikka.preference.simplemenu.R.attr.colorPrimary
                )
            )
        } else {
            holder.title.setTextColor(
                ThemeUtils.getThemeAttrColor(
                    context,
                    rikka.preference.simplemenu.R.attr.colorControlNormal
                )
            )
            holder.title.setBackgroundColor(
                ThemeUtils.getThemeAttrColor(
                    context,
                    android.R.attr.windowBackground
                )
            )
            holder.indicator.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    interface OnLabelClickListener {
        fun onLabelClicked(position: Int)
    }

}
