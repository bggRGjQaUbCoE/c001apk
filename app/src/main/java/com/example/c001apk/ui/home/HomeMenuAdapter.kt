package com.example.c001apk.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeMenu
import com.example.c001apk.view.CheckableChipView

class HomeMenuAdapter(
    private var menuList: ArrayList<HomeMenu>
) : RecyclerView.Adapter<HomeMenuAdapter.ViewHolder>(),
    ItemTouchHelperCallback.OnItemTouchCallbackListener {

    class ViewHolder(view: View, checkedColor: Int, uncheckedColor: Int) :
        RecyclerView.ViewHolder(view) {
        val menu: CheckableChipView = view.findViewById<CheckableChipView?>(R.id.menu).also {
            it.textColorPair = checkedColor to uncheckedColor
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_menu, parent, false)
        val checkedColor = parent.context.getColor(R.color.advanced_menu_item_text_checked)
        val uncheckedColor = parent.context.getColor(R.color.advanced_menu_item_text_not_checked)
        val viewHolder = ViewHolder(view, checkedColor, uncheckedColor)
        viewHolder.itemView.setOnClickListener {
            menuList[viewHolder.bindingAdapterPosition].isEnable = viewHolder.menu.isChecked
            notifyItemChanged(viewHolder.bindingAdapterPosition)
        }
        return viewHolder
    }

    override fun getItemCount() = menuList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = menuList[position]
        holder.menu.text = menu.title
        holder.menu.isChecked = menu.isEnable
    }

    override fun onMove(fromPosition: Int, targetPosition: Int): Boolean {
        menuList = menuList.apply {
            add(targetPosition, removeAt(fromPosition))
        }
        notifyItemMoved(fromPosition, targetPosition)
        return true
    }

}