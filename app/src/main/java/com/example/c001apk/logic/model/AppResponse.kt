package com.example.c001apk.logic.model

data class AppResponse(val data: Data){

    data class Data(
        val title:String,
        val logo:String,
        val id:String,
        val version:String,
        val apkversioncode:String,
        val apksize:String,
        val lastupdate:String,
    )

}

