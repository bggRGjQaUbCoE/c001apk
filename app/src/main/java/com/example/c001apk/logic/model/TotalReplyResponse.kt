package com.example.c001apk.logic.model

data class TotalReplyResponse(val data: List<Data>) {

    data class Data(
        val entityType:String,
        val id: String,
        val ruid: String,
        val uid: String,
        val username: String,
        val rusername: String,
        val message: String,
        val pic: String,
        val picArr: List<String>?,
        val dateline: String,
        val likenum: String,
        val replynum: String,
        val userAvatar: String,
        val replyRows: ArrayList<HomeFeedResponse.ReplyRows>,
        val replyRowsMore: Int,
    )

}

