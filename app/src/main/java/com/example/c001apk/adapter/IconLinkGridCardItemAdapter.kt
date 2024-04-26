package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.c001apk.BR
import com.example.c001apk.databinding.ItemHomeIconLinkGridCardItemBinding
import com.example.c001apk.logic.model.HomeFeedResponse

class IconLinkGridCardItemAdapter(
    private val dataList: List<HomeFeedResponse.Entities>,
    private val listener: ItemListener
) : BaseAdapter() {

    override fun getCount() = dataList.size

    override fun getItem(position: Int): Any = 0

    override fun getItemId(position: Int): Long = 0

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = ItemHomeIconLinkGridCardItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.setVariable(BR.data, dataList[position])
        binding.setVariable(BR.listener, listener)
        binding.executePendingBindings()
        return binding.root
    }

}