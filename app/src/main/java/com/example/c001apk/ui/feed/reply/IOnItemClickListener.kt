package com.example.c001apk.ui.feed.reply

interface IOnItemClickListener {
    fun onItemClick(keyword: String)
    fun onItemDeleteClick(position: Int, keyword: String)
}