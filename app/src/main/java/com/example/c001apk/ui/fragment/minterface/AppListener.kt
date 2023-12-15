package com.example.c001apk.ui.fragment.minterface

import android.widget.ImageView
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.view.ninegridimageview.NineGridImageView

interface AppListener {

    fun onShowTotalReply(position: Int, uid: String, id: String, rPosition: Int?)

    fun onPostFollow(isFollow: Boolean, uid: String, position: Int)

    fun onReply2Reply(
        rPosition: Int,
        r2rPosition: Int?,
        id: String,
        uid: String,
        uname: String,
        type: String
    )

    fun onPostLike(type: String?, isLike: Boolean, id: String, position: Int?)

    fun onRefreshReply(listType: String)

    fun onClick(
        nineGridView: NineGridImageView,
        imageView: ImageView,
        urlList: List<String>,
        position: Int
    ){
        ImageUtil.startBigImgView(
            nineGridView,
            imageView,
            urlList,
            position
        )
    }

    fun onDeleteFeedReply(id: String, position: Int, rPosition: Int?)

    fun onShowCollection(id: String, title: String)

}