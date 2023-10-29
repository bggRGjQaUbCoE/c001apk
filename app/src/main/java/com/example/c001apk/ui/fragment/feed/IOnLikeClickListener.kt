package com.example.c001apk.ui.fragment.feed

interface IOnLikeClickListener {

    fun onPostLike(type: String, isLike: Boolean, id: String, position: Int?)

}