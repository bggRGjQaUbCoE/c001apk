package com.example.c001apk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeMenu
import com.example.c001apk.ui.fragment.minterface.IOnHomeMenuChangedListener
import com.google.android.material.chip.Chip
import java.util.Collections

class HomeMenuAdapter(
    private val menuList: ArrayList<HomeMenu>
) : RecyclerView.Adapter<HomeMenuAdapter.ViewHolder>(),
    ItemTouchHelperCallback.OnItemTouchCallbackListener {

    private var iOnHomeMenuChangedListener: IOnHomeMenuChangedListener? = null

    fun setIOnHomeMenuChangedListener(iOnHomeMenuChangedListener: IOnHomeMenuChangedListener) {
        this.iOnHomeMenuChangedListener = iOnHomeMenuChangedListener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val menu: Chip = view.findViewById(R.id.menu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_menu, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            menuList[viewHolder.bindingAdapterPosition].isEnable = viewHolder.menu.isChecked
            iOnHomeMenuChangedListener?.onMenuChanged(menuList)
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
        Collections.swap(menuList, fromPosition, targetPosition)
        notifyItemMoved(fromPosition, targetPosition)
        val title = menuList[fromPosition].title
        val isEnable = menuList[fromPosition].isEnable
        menuList[fromPosition].title = menuList[targetPosition].title
        menuList[fromPosition].isEnable = menuList[targetPosition].isEnable
        menuList[targetPosition].title = title
        menuList[targetPosition].isEnable = isEnable
        iOnHomeMenuChangedListener?.onMenuChanged(menuList)
        return true
    }

}