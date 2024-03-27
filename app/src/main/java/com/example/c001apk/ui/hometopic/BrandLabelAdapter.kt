package com.example.c001apk.ui.hometopic

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.google.android.material.color.MaterialColors

class BrandLabelAdapter(
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_brand_label, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener { _: View ->
            if (viewHolder.bindingAdapterPosition != selectedPosition) {
                val previousPosition = selectedPosition
                selectedPosition = viewHolder.bindingAdapterPosition
                onLabelClickListener?.onLabelClicked(viewHolder.bindingAdapterPosition)
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val text = list[position]
        holder.title.text = text
        if (selectedPosition == position) {
            val color = MaterialColors.getColor(
                holder.itemView.context,
                com.google.android.material.R.attr.colorPrimary,
                0
            )
            holder.title.setTextColor(color)
            holder.title.setBackgroundColor(
                holder.itemView.context.getColor(R.color.home_card_background_color)
            )
            holder.indicator.setBackgroundColor(color)
        } else {
            holder.title.setTextColor(
                MaterialColors.getColor(
                    holder.itemView.context,
                    com.google.android.material.R.attr.colorControlNormal,
                    0
                )
            )
            holder.title.setBackgroundColor(
                MaterialColors.getColor(
                    holder.itemView.context,
                    android.R.attr.windowBackground,
                    0
                )
            )
            holder.indicator.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    interface OnLabelClickListener {
        fun onLabelClicked(position: Int)
    }

}
