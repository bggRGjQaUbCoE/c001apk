package com.example.c001apk.logic.model

data class SearchUserResponse(val data: List<Data>) {

    data class Data(
        val uid: String, val username: String, val follow: String, val level: String,
        val fans: String, val logintime: String, val userAvatar:String
    )

}

