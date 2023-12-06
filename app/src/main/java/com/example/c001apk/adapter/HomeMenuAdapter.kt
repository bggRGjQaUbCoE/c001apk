package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeMenu
import com.google.android.material.chip.Chip

class HomeMenuAdapter(
    private val menuList: ArrayList<HomeMenu>
) : RecyclerView.Adapter<HomeMenuAdapter.ViewHolder>(),
    ItemTouchHelperCallback.OnItemTouchCallbackListener {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val menu: Chip = view.findViewById(R.id.menu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_menu, parent, false)
        val viewHolder = ViewHolder(view)
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
        val position = menuList[fromPosition].position
        val title = menuList[fromPosition].title
        val isEnable = menuList[fromPosition].isEnable
        menuList.removeAt(fromPosition)
        menuList.add(targetPosition, HomeMenu(position, title, isEnable))
        notifyItemMoved(fromPosition, targetPosition)
        return true
    }

}